package de.marcel.restaurant.web.security;


import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import de.marcel.restaurant.ejb.model.User;
import de.marcel.restaurant.web.backingBeans.BackingBeanUser;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/*
Stellt Logoutfunktion bereit und
steuert Änderung sowie Insert von Credentials (Email Passwort) sowie
User Objekten in die normale DB und die AuthDB
 */
@Named
@SessionScoped
public class LoginController implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Inject BackingBeanUser backingBeanUser;
	@Inject IRestaurantEJB appServer;
	private Credentials cred;

	public void login() {
		/*
			Befindet sich in ModifiedAuthenticationFilter
		 */
	}

	public synchronized String logout() {
		// logout soll Session invalidieren und dafür sorgen, dass der Current User aus BackingBeanUser verschwindet
		// Rechte aus Shiro Ant-Matcher entfernen
		SecurityUtils.getSubject().logout();

		Logger.getLogger(getClass().getSimpleName()).severe("+# LogoutController logout entfernt User " + backingBeanUser.getCurrent() + " aus der BackingBean");

		// Eigenen actual User entfernen
		backingBeanUser.setCurrent(null);

		// Sessionbeans zurücksetzen
		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();

		return "UserList?faces-redirect=true";
	}

	/////////////// Änderung Passwort und UserDaten koordinieren /////////////////

	public synchronized void passwordChanged(ValueChangeEvent e){
		// checkPasswordQuality: Passwort Qualitätsprüfung übernimmt PrimeFaces Popup
		Logger.getLogger(getClass().getSimpleName()).severe("+# passwordChanged aufgerufen. PhaseId des Events ist " + e.getPhaseId().getName());
		Logger.getLogger(getClass().getSimpleName()).severe("+# passwordChanged aufgerufen. ValueNew " + e.getNewValue() + " OldValue " + e.getOldValue());

		// Nur bei tatsächlicher Neueingabe eines Passworts wird in die DB geschrieben
		if(e.getNewValue() == null || e.getNewValue().equals(""))
		{
			Logger.getLogger(getClass().getSimpleName()).severe("+# passwordChanged aufgerufen. Keine Änderung, exiting");
			this.cred = null;
			return;
		}

		byte[] salt = generateSalt();
		String[] passClear = { e.getNewValue().toString() };
		String[] passEncrypted = { encryptPassword(passClear, salt) };
		cred = new Credentials();
		cred.setPassword(passEncrypted[0]);
		cred.setSalt(Base64.getEncoder().encodeToString(salt));
		cred.setId_prod_db(backingBeanUser.getCurrent().getPrim());
		passClear = null; salt = null; passEncrypted = null;

		Logger.getLogger(getClass().getSimpleName()).severe("+# passwordChanged ist durchgelaufen");
	}

	public synchronized void checkAndPersist(User user) {
		Logger.getLogger(getClass().getSimpleName()).severe("+# checkAndPersist aufgerufen " + this);

		int errorLevel = 0;

		if(null == user.getPrim()) // User invalid
		{
			errorLevel = -1;
		}
		else if(null == cred) // Ist null wenn kein Passwort eingegeben wurde
		{
			// Nur User speichern, aber Email dennoch in authDB schreiben
			Logger.getLogger(getClass().getSimpleName()).severe("+# null == cred Nur User speichern, aber Email dennoch in authDB schreiben    " + this);

			if(null != user.getEmail()) // Speichern wenn eine E-Mail im User gesetzt wurde
			{
				try
				{
					errorLevel += backingBeanUser.update(user);
					errorLevel += appServer.persistEmail(user.getEmail(), user.getPrim());

				}
				catch (Exception e)
				{
					e.printStackTrace();
					errorLevel = -2;
				}

			}
			else
			{
				errorLevel = -3;
			}
		}
		else // User und Credentials (PW + Email) speichern
		{
			Logger.getLogger(getClass().getSimpleName()).severe("+# User und Credentials speichern    " + this);

			if(user.getPrim().equals(cred.getId_prod_db())) // Gehören User und Credentials zusammen?
			{
				try
				{
					cred.setEmail(user.getEmail());
					errorLevel += appServer.persistCredentials((ICredentials)cred); // 5
					errorLevel += backingBeanUser.update(user); // 3
				}
				catch (Exception e)
				{
					e.printStackTrace();
					errorLevel = -4;
				}
				finally
				{
					cred = null;
				}
			}
			else
			{
				errorLevel = -5;
			}
		}

		Logger.getLogger(getClass().getSimpleName()).severe("+# Errorlevel " + errorLevel);
		UserMailController.putNewUserEmail(user);
		throwFacesMessage(errorLevel);
	}


	public void throwFacesMessage(int errorLevel) {

		if (errorLevel >= 3 & errorLevel < 5)
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Userdaten wurden erfolgreich gespeichert!", ""));
		}
		else if(errorLevel > 5)
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Passwort und Userdaten wurden erfolgreich gespeichert!", ""));
		}
		else if (errorLevel <= 0)
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Fehler beim Speichern der Userdaten!", "Bitte versuch es noch einmal."));
		}

	}

	public void removeMessages(AjaxBehaviorEvent event) {
		// Entfernt FacesMessages (E-Mail Feld onblur)
		FacesContext fc = FacesContext.getCurrentInstance();
		String id = event.getComponent().getClientId();
		Iterator<FacesMessage> it = fc.getMessages("email");
		while ( it.hasNext() )
		{
			FacesMessage fm = it.next();
			it.remove();

			// Errors müssen erneut eingefügt werden, damit sie stehen bleiben
			// Messages gehen schon durch .next verloren
			if(!fm.getSeverity().equals(FacesMessage.SEVERITY_INFO))
			{
				fc.addMessage("email", new FacesMessage(fm.getSeverity(), fm.getSummary(), fm.getDetail()));
			}
		}
		// unsupported
		// FacesContext.getCurrentInstance().getMessageList().removeIf(e -> e.getSeverity().equals(FacesMessage.SEVERITY_INFO));

		// Verhindert weitere JSF Phasen mit Validierung und ModelUpdates
		FacesContext.getCurrentInstance().renderResponse();
	}

	///////////////////// Encryption ////////////////////////////////////////////

	private synchronized byte[] generateSalt() {
		RandomNumberGenerator rng = new SecureRandomNumberGenerator();
		// Salz wird als Byte Array erstellt und in ein Object verpackt
		ByteSource bs = rng.nextBytes(64); // soll (mindestens) so lang sein wie der PW Hash, hier vorsorglich ausgelegt auf SHA-512
		byte[] salt = bs.getBytes();
		return salt;
	}

	private synchronized String encryptPassword(String[] clearText, byte[] salt) {
		// Iterationen von Shiro holen? Die Liegen im (Hashed) CredentialsMatcher und der liegt als Member im Realm.
		return new Sha256Hash(clearText[0], salt, 1).toBase64();
	}



	///////////////////////// Für Debugging Zwecke /////////////////////////
	public void testSubject() {
		Logger.getLogger(getClass().getSimpleName()).severe("+# testSubject aufgerufen");
		Subject sub = SecurityUtils.getSubject();
		Logger.getLogger(getClass().getSimpleName()).severe("+# Subject " + sub.getPrincipal() + " ist angemeldet.");
		if(sub.isAuthenticated())
		{
			Logger.getLogger(getClass().getSimpleName()).severe("+# BackingBeanUser enthält als current " + backingBeanUser.getCurrent());
			sub = null;
		}

	}

	// Für Debugging Zwecke
	public void showContexts() {
		Logger.getLogger(getClass().getSimpleName()).severe("+# ------------------------------------ External Context -------------------------------------");
		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
		Logger.getLogger(getClass().getSimpleName()).severe("+# APPLICATIONS");
		ec.getApplicationMap().entrySet().forEach(e ->{
			Logger.getLogger(getClass().getSimpleName()).severe(String.format("%-30s%-30s", e.getKey(), e.getValue().toString()));
		});

		Logger.getLogger(getClass().getSimpleName()).severe("+# SESSIONS");
		ec.getSessionMap().entrySet().forEach(e ->{
			Logger.getLogger(getClass().getSimpleName()).severe(String.format("%-30s%-30s", e.getKey(), e.getValue().toString()));
		});
		Logger.getLogger(getClass().getSimpleName()).severe("+# ------------------------------------- Servlet Context -------------------------------------");
		ServletContext sc = (ServletContext) ec.getContext();
		Logger.getLogger(getClass().getSimpleName()).severe("+# ATTRIBUTES");
		Map<String, String> map = new LinkedHashMap();
		Enumeration<String> enums = sc.getAttributeNames();
		while(enums.hasMoreElements())
		{
			String key = enums.nextElement();
			map.put(key, sc.getAttribute(key).toString());
		}
		map.entrySet().forEach(e ->{
			Logger.getLogger(getClass().getSimpleName()).severe(String.format("%-30s%-30s", e.getKey(), e.getValue().toString()));
		});

		Logger.getLogger(getClass().getSimpleName()).severe("+# SERVLET REGISTRATIONS");
		sc.getServletRegistrations().entrySet().forEach(e ->{
			Logger.getLogger(getClass().getSimpleName()).severe(String.format("%-30s%-30s", e.getKey(), e.getValue().toString()));
		});

	}

	// Für Debugging Zwecke
	public void showCurrentUser() {
		Logger.getLogger(getClass().getSimpleName()).severe("+# showCurrentUser aufgerufen");
		{
			Logger.getLogger(getClass().getSimpleName()).severe("+# BackingBeanUser enthält als current " + backingBeanUser.getCurrent());
		}

	}

}

