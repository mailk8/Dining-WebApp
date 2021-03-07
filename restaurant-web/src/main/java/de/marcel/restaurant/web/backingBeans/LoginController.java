package de.marcel.restaurant.web.backingBeans;

import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.filter.authc.PassThruAuthenticationFilter;
import de.marcel.restaurant.ejb.model.User;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import java.io.Serializable;
import java.util.Base64;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;


@Named
@SessionScoped
public class LoginController implements Serializable
{

	private static final long serialVersionUID = 1L;

	private boolean pwChanged = false;

	@Inject IRestaurantEJB appServer; // Hat den EntityManager für Passwörter
	@Inject BackingBeanUser backingBeanUser;


	// isUserDuplicated: Prüfung, gibt es den User schon

	// passwordChanged: Als EventListener Methode, nur bei Änderung Passwort neu wegschreiben.
	// https://stackoverflow.com/questions/5698371/valuechangelistener-and-ajax-execution-order-problem-on-selectonemenu
	// Die ValueChanged Events werden wohl vor Modelupdates ausgewertet. Das ist wichtig, falls der User Email und Passwort ändert.
	/*
		PhaseId.ANY_PHASE 0
		PhaseId.RESTORE_VIEW 1
		PhaseId.APPLY_REQUEST_VALUES 2
		PhaseId.PROCESS_VALIDATIONS 3
		PhaseId.UPDATE_MODEL_VALUES 4
		PhaseId.INVOKE_APPLICATION 5
		PhaseId.RENDER_RESPONSE 6

		Events in andere Phase schieben:
		if (e.getPhaseId().getOrdinal() < 5) {
			e.setPhaseId(PhaseId.INVOKE_APPLICATION);
			e.queue();
			return;
		}
	 */

	public synchronized void passwordChanged(ValueChangeEvent e){

		Logger.getLogger(getClass().getSimpleName()).severe("+# passwordChanged aufgerufen. PhaseId des Events ist " + e.getPhaseId().getName());
		Logger.getLogger(getClass().getSimpleName()).severe("+# passwordChanged aufgerufen. ValueNew " + e.getNewValue() + " OldValue " + e.getOldValue());


		// Nur bei tatsächlicher Neueingabe eines Passworts wird in die DB geschrieben
		if(e.getNewValue() == null || e.getNewValue().equals(""))
		{
			Logger.getLogger(getClass().getSimpleName()).severe("+# passwordChanged aufgerufen. Keine Änderung, exiting");
			pwChanged = false;
			return;
		}
		byte[] salt = generateSalt();
		String[] pass = { encryptPassword(e.getNewValue().toString(), salt) };
		int result = appServer.persistCredentials(backingBeanUser.getCurrent().getPrim(), pass[0], Base64.getEncoder().encodeToString(salt));
		salt = null;
		pass = null;
		pwChanged = true;

		throwFacesMessage(result);

	}

	// EMail Änderung in letzter JSF Phase
	public synchronized void emailChanged(ValueChangeEvent e){

		Logger.getLogger(getClass().getSimpleName()).severe("+# Email change aufgerufen in Phase" + FacesContext.getCurrentInstance().getCurrentPhaseId().getName());
		Logger.getLogger(getClass().getSimpleName()).severe("+# Email change old value " + e.getOldValue() + " new Value" + e.getNewValue());

		String emailOld = (String) e.getOldValue();
		String emailNew = (String) e.getNewValue();

		if( null == emailOld || emailOld.equals(""))
		{
			Logger.getLogger(getClass().getSimpleName()).severe("+# Email Neueingabe ");
		}

		if( null == emailNew || emailNew.equals(""))
		{
			Logger.getLogger(getClass().getSimpleName()).severe("+# Email change value war null, exiting... ");
			return;
		}

		int result = appServer.persistEmail(backingBeanUser.getCurrent().getPrim(), emailNew);

		throwFacesMessage(result);

	}

	private byte[] generateSalt()
	{
		RandomNumberGenerator rng = new SecureRandomNumberGenerator();
		// Salz wird als Byte Array erstellt und in ein Object verpackt
		ByteSource bs = rng.nextBytes(64); // soll (mindestens) so lang sein wie der PW Hash, hier vorsorglich ausgelegt auf SHA-512
		byte[] salt = bs.getBytes();
		return salt;
	}

	// encryptPassword zum wegschreiben
	private String encryptPassword(String clearText, byte[] salt)
	{
		// Iterationen von Shiro holen! Die Liegen im (Hashed) CredentialsMatcher und der liegt als Member im Realm.
		return new Sha256Hash(clearText, salt, 1).toBase64();
	}

	// checkPasswordQuality: Passwort Qualitätsprüfung (oder doch in einem speziellen Validator?)


	// decryptPassword: KEIN Holen des Passworts zur Darstellung in der "Ändern Maske". Holen übernimt Security Framework bei Login. Es gibt nur die Funktion "Passwort Überschreiben"
	// Falls doch: getPassword: Hole Passwort, gib es ins UI ab und vergiss es gleich wieder




	public void throwFacesMessage(int result)
	{
		if(pwChanged)
		{
			if (result == 1)
			{
				// worauf bezieht sich ClientID, den Browser?
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Passwort wurde erfolgreich gespeichert!", ""));

				Logger.getLogger(getClass().getSimpleName()).severe("+# Versuche Growl INFO / Erfolg auszulösen");
			}
			else if (result == -1)
			{
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Fehler beim Speichern des Passworts!", "Bitte versuch es noch einmal."));
				Logger.getLogger(getClass().getSimpleName()).severe("+# Versuche Growl Error auszulösen");
			}
		}
	}



	// logout soll Session invalidieren und dafür sorgen, dass der Current User aus BackingBeanUser verschwindet
	public String logout()
	{
		// Rechte aus Shiro Ant-Matcher entfernen
		SecurityUtils.getSubject().logout();

		Logger.getLogger(getClass().getSimpleName()).severe("+# LogoutController logout entfernt User " + backingBeanUser.getCurrent() + " aus der BackingBean");

		// Eigenen actual User entfernen
		backingBeanUser.setCurrent(null);

		// Sessionbeans zurücksetzen
		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();

		return "UserList?faces-redirect=true";
	}

//	public void login(Subject sub)
//	{
//		Logger.getLogger(getClass().getSimpleName()).severe("+# Login aufgerufen");
//		//Subject sub = SecurityUtils.getSubject();
//		Logger.getLogger(getClass().getSimpleName()).severe("+# Subject " + sub.getPrincipal() + " ist angemeldet.");
//		if(sub.isAuthenticated())
//		{
//			User u = (User) appServer.findOne(sub.getPrincipal(), String.class, User.class);
//			Logger.getLogger(getClass().getSimpleName()).severe("+# Gefunden: " + u.getEmail());
//			backingBeanUser.setCurrent(u);
//			Logger.getLogger(getClass().getSimpleName()).severe("+# BackingBeanUser enthält als current " + backingBeanUser.getCurrent());
//			sub = null;
//		}
//
//	}

	public void testSubject()
	{
		Logger.getLogger(getClass().getSimpleName()).severe("+# test aufgerufen");
		Subject sub = SecurityUtils.getSubject();
		Logger.getLogger(getClass().getSimpleName()).severe("+# Subject " + sub.getPrincipal() + " ist angemeldet.");
		if(sub.isAuthenticated())
		{
			Logger.getLogger(getClass().getSimpleName()).severe("+# BackingBeanUser enthält als current " + backingBeanUser.getCurrent());
			sub = null;
		}

	}

	public void showContexts()
	{
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

}
