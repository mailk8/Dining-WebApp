package de.marcel.restaurant.web.backingBeans;

import com.sun.jdi.Value;
import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.filter.authc.PassThruAuthenticationFilter;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.omnifaces.util.Faces;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.logging.Logger;

@Named
@SessionScoped
public class LoginController implements Serializable
{
	AuthenticatingFilter test;

	private static final long serialVersionUID = 1L;

	@Inject IRestaurantEJB appServer; // Hat den EntityManager für Passwörter
	@Inject BackingBeanUser backingBeanUser;



	// Members
	// boolean: isUser Authenticated oder BESSER immmer bei Shiro nachfragen

	// Methods

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
		Logger.getLogger(getClass().getSimpleName()).severe("+# passwordChanged valueChanged wird ausgeführt in Phase " + FacesContext.getCurrentInstance().getCurrentPhaseId().getName());
		Logger.getLogger(getClass().getSimpleName()).severe("+# passwordChanged BackingBeanUser ist " +backingBeanUser + " aktuelle email ist " + backingBeanUser.getCurrent().getEmail() + " aktueller User ist "
						+ backingBeanUser.getCurrent() + " aktuelle Phase ist " + e.getPhaseId().getName());

		// Nur bei tatsächlicher Neueingabe eines Passworts wird in die DB geschrieben
		if(e.getNewValue() == null || e.getNewValue().equals(""))
		{
			return;
		}
		byte[] salt = generateSalt();
		String[] pass = { encryptPassword(e.getNewValue().toString(), salt) };
		int result = appServer.persistCredentials(backingBeanUser.getCurrent().getPrim(), pass[0], Base64.getEncoder().encodeToString(salt));
		salt = null;
		pass = null;

		throwFacesMessage(result, e);

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

		throwFacesMessage(result, e);

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




	public void throwFacesMessage(int result, ValueChangeEvent e)
	{

		if(result == 1)
		{
			// worauf bezieht sich ClientID, den Browser?
			FacesContext.getCurrentInstance().addMessage("growlPassword", new FacesMessage(
							FacesMessage.SEVERITY_INFO, "Passwort wurde erfolgreich gespeichert!", ""));

			Logger.getLogger(getClass().getSimpleName()).severe("+# Versuche Growl INFO / Erfolg auszulösen");
		}
		else if(result == -1)
		{
			FacesContext.getCurrentInstance().addMessage("growlPassword", new FacesMessage(
							FacesMessage.SEVERITY_ERROR, "Fehler beim Speichern des Passworts!", "Bitte versuch es noch einmal."));
			Logger.getLogger(getClass().getSimpleName()).severe("+# Versuche Growl Error auszulösen");
		}
	}

	public void throwFacesMessage()
	{
		Logger.getLogger(getClass().getSimpleName()).severe("+# Versuche TEst Growl auszulösen in Phase " + FacesContext.getCurrentInstance().getCurrentPhaseId().getName());
		FacesContext.getCurrentInstance().addMessage("password", new FacesMessage(
						FacesMessage.SEVERITY_INFO, "Passwort wurde erfolgreich gespeichert!", ""));

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

		org.apache.shiro.web.filter.authc.AuthenticationFilter asdf;
		AuthenticatingFilter dgf;
		PassThruAuthenticationFilter sgfs;

		return "UserList?faces-redirect=true";
	}



}
