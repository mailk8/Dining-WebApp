package de.marcel.restaurant.web.backingBeans;

import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import de.marcel.restaurant.ejb.model.Address;
import de.marcel.restaurant.ejb.model.Culinary;
import de.marcel.restaurant.ejb.model.User;
import de.marcel.restaurant.web.httpClient.*;
import de.marcel.restaurant.web.security.ICredentials;
import de.marcel.restaurant.web.security.LoginController;
import de.marcel.restaurant.web.security.UserMailController;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/*
Was passiert wenn ein eingeloggter User seinen Account löscht?
	-> Er ist weiterhin angemeldet. Sollte nicht sein.
 */

@Named
@SessionScoped
@ManagedBean
public class BackingBeanUser implements Serializable
{
	private static final long serialVersionUID = 1L;

	private User current;
	@Inject private IRestaurantEJB appServer;
	@Inject private HttpClientWGS client;
	@Inject private LoginController loginController;
	private String validateLon, validateLat;
	private Logger logger = Logger.getLogger(getClass().getSimpleName());
	private String sessionId;
	private List<Culinary> allCulinariesProxy;
	private List<User> allUsersProxy;

	@PostConstruct
	private void fetchLoggedInUser() {
		sessionId = FacesContext.getCurrentInstance().getExternalContext().getSessionId(false);
		//logger.severe("+# fetchLoggedInUser läuft. BackingBeanUser unter SessionID " + sessionId + " erstellt.");

		Subject s = SecurityUtils.getSubject();
		if(s.isAuthenticated())
		{
			Session sess = s.getSession();
			User u = (User) sess.getAttribute("loggedInUser");
			if(u.getEmail().equals(s.getPrincipal()))
			{
				setCurrent(u);
			}
			//sess.removeAttribute("loggedInUser");
		}
		else
		{
			setCurrent(new User());
		}

	}

	///////////////////////////// Methods for Performace Enhancement ////////////////////////////

	public List<Culinary> getAllCulinariesProxy()
	{
		return allCulinariesProxy;
	}

	public List<User> getAllUsersProxy()
	{
		return allUsersProxy;
	}

	///////////////////////////// Methods for Basic Crud /////////////////////////////////////////

	public List<Culinary> getAllCulinaries() {
		allCulinariesProxy = appServer.findAll(Culinary.class);
		return allCulinariesProxy;
	}

	public List<User> getAllUsers() {
		allUsersProxy = appServer.findAll(User.class);
		return allUsersProxy;
	}

	public String edit(User u) {
		if(!loginController.isPermitted(u))
			return "Login.jsf?faces-redirect=true";

		this.current = u;
		setCoordinatesBean(u);
		return "UserCreate?faces-redirect=true";
	}

	public String delete(User u) {
		if(!loginController.isPermitted(u))
			return "Login.jsf?faces-redirect=true";

		UserMailController.deleteUserEmail(u.getEmail());
		appServer.deleteCredentials(u.getId());
		appServer.delete(u);
		loginController.logout();
		return "UserList?faces-redirect=true";
	}

	public String saveUserProxy()
	{
		loginController.checkAndPersist(current);
		return "UserList?faces-redirect=true";
	}

	public int saveUser(User u)
	{
		if(null == current.getPrim())
		{
			return insert(u);
		}
		else
		{
			return update(u);
		}
	}

	// Delegiert Persist new
	public int insert(User u) {
		setCoordinatesEntity();
		int result = appServer.persist(u);
		if(result < 0)
			return -1; // -1 = fail
		current.setPrim(result);
		//Logger.getLogger(getClass().getSimpleName()).severe("+# Es wurde insert(User u) aufgerufen, result von proxyPersistUser " + result);
		return 3; // 3  = success
	}

	// Delegiert Persist update
	public int update(User u) {
		User old = (User) appServer.findOneById(u.getId().toString(), User.class);
		setCoordinatesEntity();
		int result = appServer.update(u); // -1 = fail 3 = success
		UserMailController.deleteUserEmail(old.getEmail());
	    UserMailController.putNewUserEmail(u);
		//Logger.getLogger(getClass().getSimpleName()).severe("+# Es wurde update(User u) aufgerufen, result von appServer.update(u) " + result);
		return result;
	}

	public User getCurrent()
	{
		return current;
	}

	public String createNew() {
		// Nächste freie id wird antizipiert und per Zufall erhöht, falls die Aktion parallel erfolgt
		int nextId = (appServer.findMaxId(User.class) + 1 + (new Random().nextInt(2)));
		current = new User();
		current.setId(nextId);
		setCoordinatesBean(current);
		return "UserCreate?faces-redirect=true";
	}

	public void setCurrent(User u) {
		this.current = u;
		setCoordinatesBean(u);
	}

	///////////////////////////// Methods for WGS Coordinates /////////////////////////////////

	public String getValidateLat()
	{
		return validateLat;
	}

	public String getValidateLon()
	{
		return validateLon;
	}

	public void setValidateLat(String validateLat) {
		validateLat = validateLat.replace(",", ".");
		this.validateLat = validateLat;
	}

	public void setValidateLon(String validateLon) {
		validateLon = validateLon.replace(",", ".");
		this.validateLon = validateLon;
	}

	public void setCoordinatesBean(User u) {
		try
		{
			// Ist der Wert null, also nicht gesetzt, soll ein empty String dargestellt werden
			this.validateLat = u.getAddressActual().getWgs84Latitude().toString();
			this.validateLon = u.getAddressActual().getWgs84Longitude().toString();
		}
		catch (NullPointerException e)
		{
			this.validateLat = "";
			this.validateLon = "";
		}
	}

	public void setCoordinatesEntity() {
		// null soll erlaubt sein wenn kein Wert gesetzt ist
		if(null == validateLat || validateLat.equals("") || validateLat.isEmpty() ||validateLat.isBlank())
		{
			current.getAddressActual().setWgs84Latitude(null);
		}
		else
		{
			current.getAddressActual().setWgs84Latitude(Double.parseDouble(validateLat));
		}

		if(null == validateLon || validateLon.equals("") || validateLon.isEmpty() ||validateLon.isBlank())
		{
			current.getAddressActual().setWgs84Longitude(null);
		}
		else
		{
			current.getAddressActual().setWgs84Longitude(Double.parseDouble(validateLon));
		}
	}

	public void requestWgsForAddress() {
		//client.enqueueNewRequest(current.getAddressLiving(), appServer);
			List<Address> list = appServer.findAll(Address.class).stream()
							.map( e -> (Address) e)
							.filter((Address d) -> (d.getPrim().intValue() >= 810))//.limit(1)
							.collect(Collectors.toList());
		System.out.println("+# Testliste mit Elementen " + list.size() + "\n" + list);
		list.forEach(e->{
			System.out.println(e + " trys: " + e.getCounterApiCalls());
		});
//
//		list.forEach(e->{
//			client.enqueueNewRequest(e, appServer);
//		});
	}

	////////////////////////////// Methods for Credentials Persist //////////////////////////
	// Proxy Methode dient dem Zweck, dass beim AppServer das selbe Objekt erreicht wird
	public int proxyPersistEmail(String email, int id)
	{
		return appServer.persistEmail(email, id);
	}

	public int proxyPersistCredentials(ICredentials cred)
	{
		return appServer.persistCredentials(cred);
	}

}
