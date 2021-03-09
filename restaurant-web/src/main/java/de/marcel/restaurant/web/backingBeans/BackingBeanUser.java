package de.marcel.restaurant.web.backingBeans;


import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import de.marcel.restaurant.ejb.model.Address;
import de.marcel.restaurant.ejb.model.Culinary;
import de.marcel.restaurant.ejb.model.User;
import de.marcel.restaurant.web.httpClient.*;
import de.marcel.restaurant.web.security.UserMailController;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Named
@SessionScoped
public class BackingBeanUser implements Serializable
{
	private static final long serialVersionUID = 1L;

	private User current;
	@Inject private IRestaurantEJB appServer;
	@Inject private HttpClientWGS client;
	private String validateLon, validateLat;

	@PostConstruct
	// holt eingeloggte User und legt sie auf current
	private void fetchLoggedInUser() {
		Subject s = SecurityUtils.getSubject();
		if(s.isAuthenticated())
		{
			Session sess = s.getSession();
			User u = (User) sess.getAttribute("loggedInUser");
			if(u.getEmail().equals(s.getPrincipal()))
			{
				setCurrent(u);
			}
			sess.removeAttribute("loggedInUser");
		}
		else
		{
			setCurrent(new User());
		}
	}

	public List<Culinary> getAllCulinaries()
	{
		return appServer.findAll(Culinary.class);
	}

	public List<User> getAllUsers(){return appServer.findAll(User.class);}

	// Kontrolliert das Persistieren
	public String saveUser() {
		String s="empty";
		if(null != current)
		{
			if(null == current.getPrim())
			{
				setCoordinatesEntity();
				insert(current);
				s = "insert";
			}

			else
			{
				setCoordinatesEntity();
				update(current);
				s = "update";
			}

		}

		Logger.getLogger(getClass().getSimpleName()).severe("+# Es wurde " + s + " aufgerufen in Phase " + FacesContext.getCurrentInstance().getCurrentPhaseId().getName() +
						" Current User firstname ist " + current.getFirstname() + " mit id " + current.getPrim());

		//current = new User(); // Nur in createNew !
		return "UserList?faces-redirect=true";
	}

	public String edit(User u) {
		this.current = u;
		setCoordinatesBean(u);
		return "UserCreate?faces-redirect=true";
	}

	// Delegiert Persist new
	public void insert(User u) {
		// EmailContainer bekommt neuen Wert
		UserMailController.putNewUserEmail(u.getEmail());
		Integer i = appServer.persist(u);
		u.setPrim(i);
	}

	// Delegiert Persist update
	public void update(User u) {
		User old = (User) appServer.findOneByPrim(u.getPrim().toString(), User.class);
		UserMailController.deleteUserEmail(old.getEmail());
		UserMailController.putNewUserEmail(u.getEmail());
		Integer i = appServer.update(u);
		u.setPrim(i);
	}

	public String delete(User u) {
		UserMailController.deleteUserEmail(u.getEmail());
		appServer.deleteCredentials(u.getPrim());
		appServer.delete(u);
		return "UserList?faces-redirect=true";
	}

	public User getCurrent()
	{
		return current;
	}

	public String createNew() {
		current = new User();
		setCoordinatesBean(current);
		insert(current);

		return "UserCreate?faces-redirect=true";
	}

	public String getValidateLat()
	{
		return validateLat;
	}

	public String getValidateLon()
	{
		return validateLon;
	}

	public void setCurrent(User u) {
		this.current = u;
		setCoordinatesBean(u);
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
			List<Address> ballern = appServer.findAll(Address.class).stream()
							.map( e -> (Address) e)
							.filter((Address d) -> (d.getPrim().intValue() >= 810))//.limit(1)
							.collect(Collectors.toList());
		System.out.println("+# Testliste mit Elementen " + ballern.size() + "\n" + ballern);
		ballern.forEach(e->{
			System.out.println(e + " trys: " + e.getCounterApiCalls());
		});
//
//		ballern.forEach(e->{
//			client.enqueueNewRequest(e, appServer);
//		});
	}






}
