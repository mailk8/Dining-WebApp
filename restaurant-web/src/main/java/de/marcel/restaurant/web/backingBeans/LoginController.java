package de.marcel.restaurant.web.backingBeans;

import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.web.mgt.WebSecurityManager;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletContext;
import java.io.Serializable;
import java.util.Base64;
import java.util.logging.Logger;

@Named
@SessionScoped
public class LoginController implements Serializable
{
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
	 */

	private String emailOld, emailNew, emailPass;

	public synchronized void passwordChanged(ValueChangeEvent e){
		Logger.getLogger(getClass().getSimpleName()).severe("+# passwordChanged aufgerufen. PhaseId des Events ist " + e.getPhaseId().getName());
//		// Event muss Modelupdate abwarten
//		if (e.getPhaseId().getOrdinal() > 5)
//		{
//			emailOld = backingBeanUser.getCurrent().getEmail();
//			Logger.getLogger(getClass().getSimpleName()).severe("+# passwordChanged PhaseId < 5 aufgerufen. PhaseId des Events ist " + e.getPhaseId().getName());
//			e.setPhaseId(PhaseId.INVOKE_APPLICATION);
//			Logger.getLogger(getClass().getSimpleName()).severe("+# passwordChanged PhaseId < 5 aufgerufen. PhaseId nach Change " + e.getPhaseId().getName());
//			e.queue();
//			Logger.getLogger(getClass().getSimpleName()).severe("+# passwordChanged PhaseId < 5 aufgerufen. PhaseId nach Queue " + e.getPhaseId().getName());
//			return;
//		}

		Logger.getLogger(getClass().getSimpleName()).severe("+# passwordChanged valueChanged wird ausgeführt in Phase " + FacesContext.getCurrentInstance().getCurrentPhaseId().getName());

		Logger.getLogger(getClass().getSimpleName()).severe("+# passwordChanged BackingBeanUser ist " +backingBeanUser + " aktuelle email ist " + backingBeanUser.getCurrent().getEmail() + " aktueller User ist "
						+ backingBeanUser.getCurrent() + " aktuelle Phase ist " + e.getPhaseId().getName());

		// Nur bei tatsächlicher Neueingabe eines Passworts wird in die DB geschrieben
		if(e.getNewValue().equals("") || e.getNewValue() == null)
		{
			return;
		}
		byte[] salt = generateSalt();
		String[] pass = { encryptPassword(e.getNewValue().toString(), salt) };
		appServer.persistCredentials(emailOld, pass[0], Base64.getEncoder().encodeToString(salt));
		salt = null;
		pass = null;

	}

	// EMail Änderung in letzter JSF Phase
	public synchronized void emailChanged(ValueChangeEvent e){

		Logger.getLogger(getClass().getSimpleName()).severe("+# Email change aufgerufen in Phase" + FacesContext.getCurrentInstance().getCurrentPhaseId().getName());
		Logger.getLogger(getClass().getSimpleName()).severe("+# Email change old value " + e.getOldValue() + " new Value" + e.getNewValue());

		emailOld = (String) e.getOldValue(); // läuft früh in Phase 3 "Validation"
		emailNew = (String) e.getNewValue(); // läuft früh in Phase 3 "Validation"

		if (e.getPhaseId().getOrdinal() < 5)
		{
			Logger.getLogger(getClass().getSimpleName()).severe("+# Email change < 6");
			e.setPhaseId(PhaseId.INVOKE_APPLICATION);
			e.queue();
			return;
		}

		if(emailOld.equals("") || emailOld == null)
		{
			Logger.getLogger(getClass().getSimpleName()).severe("+# Email Neueingabe, exiting... ");
			return;
		}

		if(emailNew.equals("") || emailNew == null)
		{
			Logger.getLogger(getClass().getSimpleName()).severe("+# Email change value war null, exiting... ");
			return;
		}

		appServer.persistEmail(emailOld, emailNew);

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





}
