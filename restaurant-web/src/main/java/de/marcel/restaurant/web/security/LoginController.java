package de.marcel.restaurant.web.security;


import de.marcel.restaurant.ejb.interfaces.ICredentials;
import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import de.marcel.restaurant.ejb.model.Credentials;
import de.marcel.restaurant.ejb.model.User;
import de.marcel.restaurant.web.backingBeans.BackingBeanRestaurant;
import de.marcel.restaurant.web.backingBeans.BackingBeanUser;
import de.marcel.restaurant.web.backingBeans.BackingBeanVisit;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.*;
import java.util.*;

/*
Stellt Logoutfunktion bereit und
steuert Änderung sowie Insert von Credentials (Email, Passwort) sowie
User Objekten in die normale DB und die AuthDB

User Neuanlage mit Passworteingabe sollte automatisch login Zustand herbeiführen
 */
@Named
@SessionScoped
public class LoginController implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Inject BackingBeanUser backingBeanUser;
	@Inject BackingBeanRestaurant backingBeanRestaurant;
	@Inject BackingBeanVisit backingBeanVisit;
	@Inject IRestaurantEJB appServer;
	private de.marcel.restaurant.ejb.model.Credentials cred;

	public void login() {
			// Befindet sich in ModifiedAuthenticationFilter
	}

	public synchronized String logout() {
		// logout soll Session invalidieren und dafür sorgen, dass der Current User aus der BackingBeanUser verschwindet
		clearSession();
		return "UserList?faces-redirect=true";
	}

	public boolean isPermitted(User u) {
		Subject sub = SecurityUtils.getSubject();
		Session sessionAuth = sub.getSession();
		String sessionIdActual = FacesContext.getCurrentInstance().getExternalContext().getSessionId(false);
		User loggedInUser = (User) sessionAuth.getAttribute("loggedInUser");

		if(!sessionAuth.getId().toString().equals(sessionIdActual) || !sub.isAuthenticated() || !u.getPrim().equals(loggedInUser.getPrim()))
		{
			return false;
		}

		return true;
	}

	public boolean isLoggedInUser(User u) {
		// light Version für UserList, um Buttons nur für den eingeloggten User anzuzeigen
		// https://stackoverflow.com/questions/4281261/why-is-the-getter-called-so-many-times-by-the-rendered-attribute

		if( u == null )
		{
			return false;
		}
		Subject sub = SecurityUtils.getSubject();
		if( !sub.isAuthenticated() )
		{
			return false;
		}
		return ((User) SecurityUtils.getSubject().getSession().getAttribute("loggedInUser")).getPrim().equals(u.getPrim());
	}

	public synchronized void clearSession() {
		SecurityUtils.getSubject().getSession().removeAttribute("loggedInUser");
		SecurityUtils.getSubject().logout();
		backingBeanUser.setCurrent(null);
		String id = FacesContext.getCurrentInstance().getExternalContext().getSessionId(false);
		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
		String newId = FacesContext.getCurrentInstance().getExternalContext().getSessionId(true);
		// JPA-Objekt Cache löschen
		appServer.clearCacheAllObjects();

	}

	/////////////// Änderung Passwort und UserDaten koordinieren /////////////////
	public synchronized void passwordChanged(ValueChangeEvent e){
		// als ValueChanged Listener, weil ein Validator kein Zugriff auf old und new Value hat
		// checkPasswordQuality: Passwort Qualitätsprüfung übernimmt PrimeFaces Popup

		// Nur bei tatsächlicher Neueingabe eines Passworts wird in die DB geschrieben
		if(e.getNewValue() == null || e.getNewValue().equals(""))
		{
			this.cred = null;
			return;
//			if(null != backingBeanUser.getCurrent().getPrim()) // Änderung User
//			{
//				this.cred = null;
//				return;
//			}
//			else if(null == backingBeanUser.getCurrent().getPrim()) // Neuanlage
//			{
//				//this.cred = null;
//				return;
//			}
		}

		byte[] salt = generateSalt();
		String[] passClear = { e.getNewValue().toString() };
		String[] passEncrypted = { encryptPassword(passClear, salt) };
		cred = new Credentials();
		cred.setPassword(passEncrypted[0]);
		cred.setSalt(Base64.getEncoder().encodeToString(salt));
		cred.setId_prod_db(backingBeanUser.getCurrent().getId());
		passClear = null; salt = null; passEncrypted = null;

	}

	public synchronized void checkAndPersist(User user) {

		int errorLevel = 0;

		if(null == user.getId()) // User invalid
		{
			errorLevel = -1;
		}
		else if(null == cred) // Ist null, wenn das Passwort nicht geändert wurde
		{
			// Nur User speichern, Email dennoch in authDB aktualisieren

			if(null != user.getEmail()) // Speichern wenn eine E-Mail im User gesetzt wurde
			{
				try
				{
					errorLevel += backingBeanUser.saveUser(user);
					errorLevel += backingBeanUser.proxyPersistEmail(user.getEmail(), user.getId());
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

			if(user.getId().equals(cred.getId_prod_db())) // Gehören User und Credentials zusammen?
			{
				try
				{
					cred.setEmail(user.getEmail());
					errorLevel += backingBeanUser.proxyPersistCredentials((ICredentials)cred); // 5
					errorLevel += backingBeanUser.saveUser(user); // 3
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
			// Errors müssen erneut eingefügt werden, damit sie stehen bleiben:
			// Messages gehen schon durch .next verloren
			if(!fm.getSeverity().equals(FacesMessage.SEVERITY_INFO))
			{
				fc.addMessage("email", new FacesMessage(fm.getSeverity(), fm.getSummary(), fm.getDetail()));
			}
		}
		// unsupported: FacesContext.getCurrentInstance().getMessageList().removeIf(e -> e.getSeverity().equals(FacesMessage.SEVERITY_INFO));

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

}

