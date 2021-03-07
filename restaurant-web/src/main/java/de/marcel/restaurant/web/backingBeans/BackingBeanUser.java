package de.marcel.restaurant.web.backingBeans;


import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import de.marcel.restaurant.ejb.model.Address;
import de.marcel.restaurant.ejb.model.Culinary;
import de.marcel.restaurant.ejb.model.User;
import de.marcel.restaurant.web.httpClient.*;
import org.apache.shiro.SecurityUtils;
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

	// findAll im "appServer" public <T> List<T> findAll(Class entitiyClass)

	@PostConstruct
	private void fetchLoggedInUser()
	{
		Subject s = SecurityUtils.getSubject();
		if(s.isAuthenticated())
		{
			User u = (User) s.getSession().getAttribute("loggedInUser");
			if(u.getEmail().equals(s.getPrincipal()))
			{
				setCurrent(u);
			}
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

	public void setCurrent(User u)
	{
		this.current = u;
		setCoordinatesBean(u);
	}

	public String saveUser()
	{
		String s="empty";
		if(current != null)
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

	public void insert(User u)
	{
		Integer i = appServer.persist(u);
		u.setPrim(i);
	}

	public void update(User u)
	{
		Integer i = appServer.update(u);
		u.setPrim(i);
	}

	public String edit(User u)
	{
		this.current = u;
		setCoordinatesBean(u);
		return "UserCreate?faces-redirect=true";
	}

	public String delete(User u)
	{
		appServer.deleteCredentials(u.getPrim());
		appServer.delete(u);
		return "UserList?faces-redirect=true";
	}

	public User getCurrent()
	{
		return current;
	}

	public String createNew()
	{
		current = new User();
		setCoordinatesBean(current);
		insert(current);

		return "UserCreate?faces-redirect=true";
	}

	public String getValidateLon()
	{
		return validateLon;
	}

	public void setValidateLon(String validateLon)
	{
		this.validateLon = validateLon;
	}

	public String getValidateLat()
	{
		return validateLat;
	}

	public void setValidateLat(String validateLat)
	{
		this.validateLat = validateLat;
	}

	public void setCoordinatesBean(User u)
	{
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

	public void setCoordinatesEntity()
	{
		if(validateLat.equals(""))
			current.getAddressActual().setWgs84Latitude(null);

		if(validateLon.equals(""))
			current.getAddressActual().setWgs84Longitude(null);

		try
		{
			current.getAddressActual().setWgs84Latitude(Double.parseDouble(validateLat));
			current.getAddressActual().setWgs84Longitude(Double.parseDouble(validateLon));
		}
		catch (Exception e)
		{
			// null soll erlaubt sein wenn kein Wert gesetzt ist
			//throw new NullPointerException("Fehler in setCoordinatesEntity");
		}
	}

	public void requestWgsForAddress()
	{
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
