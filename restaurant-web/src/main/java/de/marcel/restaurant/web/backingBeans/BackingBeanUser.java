package de.marcel.restaurant.web.backingBeans;

import de.marcel.restaurant.ejb.interfaces.ICredentials;
import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import de.marcel.restaurant.ejb.model.Address;
import de.marcel.restaurant.ejb.model.Culinary;
import de.marcel.restaurant.ejb.model.User;
import de.marcel.restaurant.web.httpClient.*;
import de.marcel.restaurant.web.jsfFramework.WebSocketObserver;
import de.marcel.restaurant.web.security.LoginController;
import de.marcel.restaurant.web.security.UserMailController;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.omnifaces.util.Faces;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Instance;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Random;



@Named
@SessionScoped
@ManagedBean
public class BackingBeanUser implements Serializable
{
	private static final long serialVersionUID = 1L;

	private User current;
	@Inject private IRestaurantEJB appServer;
	@Inject private Instance<HttpClientWGS> client; // weld cdi workaround
	@Inject private LoginController loginController;
	@Inject private WebSocketObserver websocket;
	private String sessionId;
	private List<Culinary> allCulinariesProxy;
	private List<User> allUsersProxy;

	@PostConstruct
	private void fetchLoggedInUser() {
		sessionId = FacesContext.getCurrentInstance().getExternalContext().getSessionId(false);

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
		allUsersProxy.sort(Comparator.comparing(e -> e.getLastname()));
		return allUsersProxy;
	}

	public String edit(User u) {
		if(!loginController.isPermitted(u))
			return "Login.jsf?faces-redirect=true";

		this.current = u;
		//setCoordinatesBean(u);
		return "UserCreate?faces-redirect=true";
	}

	public String delete(User u) {
		if(!loginController.isPermitted(u))
		{
			return "Login.jsf?faces-redirect=true";
		}
		String mail = u.getEmail();
		int id = u.getId();
		if( appServer.delete(u) < 0 )
		{
			FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_FATAL, "Fehler beim Lösches dieses Elements. Bitte den Entwickler kontaktieren." , ""));
			return "UserList?faces-redirect=true";
		}
		appServer.deleteCredentials(id);
		UserMailController.deleteUserEmail(mail);
		loginController.logout();
		websocket.sendMessage(User.class);
		return "UserList?faces-redirect=true";
	}

	public String saveUserProxy() {
		loginController.checkAndPersist(current);
		websocket.sendMessage(User.class);
		return "UserList?faces-redirect=true";
	}

	public int saveUser(User u) {
		if(null == current.getPrim())
		{
			return insert(u);
		}
		else
		{
			return update(u);
		}
	}

	public int insert(User u) {
		//setCoordinatesEntity();
		int result = appServer.persist(u);
		if(result < 0)
			return -1; // -1 = fail
		current.setPrim(result);
		return 3; // 3  = success
	}

	public int update(User u) {
		User old = (User) appServer.findOneById(u.getId().toString(), User.class);
		//setCoordinatesEntity();
		int result = appServer.update(u); // -1 = fail 3 = success
		UserMailController.deleteUserEmail(old.getEmail());
	    UserMailController.putNewUserEmail(u);
		return result;
	}

	public User getCurrent()
	{
		return current;
	}

	public String createNew() {
		// Nächste freie id wird antizipiert und per Zufall erhöht
		int nextId = (appServer.findMaxId(User.class) + 1 + (new Random().nextInt(2)));
		current = new User();
		current.setId(nextId);

		return "UserCreate?faces-redirect=true";
	}

	public void setCurrent(User u) {
		this.current = u;
	}




	///////////////////////////// Methods for WGS Coordinates /////////////////////////////////

	public void requestWgsForAddress() {
		Address adr = current.getAddressLiving();
		adr.setSessionId(getSessionId());
		client.get().enqueueNewRequest(adr);
	}

	public String getSessionId() {
		return Faces.getSessionId();
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
