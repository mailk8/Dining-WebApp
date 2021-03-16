package de.marcel.restaurant.web.backingBeans;

import javax.annotation.ManagedBean;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import de.marcel.restaurant.ejb.model.Culinary;
import de.marcel.restaurant.ejb.model.Restaurant;
import de.marcel.restaurant.ejb.model.User;

import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

@Named
@SessionScoped
@ManagedBean
public class BackingBeanRestaurant implements Serializable
{
	private static final long serialVersionUID = 1L;
	private Restaurant current;
	private String validateLon, validateLat;

	@Inject
	private IRestaurantEJB appServer;

	// findAll im "appServer" public <T> List<T> findAll(Class entitiyClass)
	public List<Culinary> getAllCulinaries()
	{
		return appServer.findAll(Culinary.class);
	}
	public List<Restaurant> getAllRestaurants()
	{
		return appServer.findAll(Restaurant.class);
	}

	public void setCurrent(Restaurant u)
	{
		this.current = u;
		setCoordinatesBean(u);
	}

	public String saveRestaurant()
	{


		if(null == current.getPrim())
		{
			Logger.getLogger(getClass().getSimpleName()).severe("+# Aufruf insert - current ist " + current);
			insert(current);
		}

		else
		{
			Logger.getLogger(getClass().getSimpleName()).severe("+# Aufruf update - current ist " + current);
			update(current);
		}

		//current = new Restaurant();
		return "RestaurantList?faces-redirect=true";
	}

	public void insert(Restaurant u)
	{
		setCoordinatesEntity();
		int result = appServer.persist(u);
		current.setPrim(result);
	}

	public void update(Restaurant u)
	{
		setCoordinatesEntity();
		appServer.update(u);
	}

	public String edit(Restaurant u)
	{
		Logger.getLogger(getClass().getSimpleName()).severe("+# Aufruf edit - current ist " + current +" zu editieren " + u);
		this.current = u;
		setCoordinatesBean(u);
		return "RestaurantCreate?faces-redirect=true";
	}

	public String delete(Restaurant u)
	{
		appServer.delete(u);
		return "RestaurantList?faces-redirect=true";
	}

	public Restaurant getCurrent()
	{
		return current;
	}

	public String createNew()
	{
		int nextId = (appServer.findMaxId(Restaurant.class) + 1 + (new Random().nextInt(2)));
		current = new Restaurant();
		current.setId(nextId);
		setCoordinatesBean(current);
		return "RestaurantCreate?faces-redirect=true";
	}

	public String getValidateLon()
	{
		return validateLon;
	}

	public void setValidateLon(String validateLon)
	{
		validateLon = validateLon.replace(",", ".");
		this.validateLon = validateLon;
	}

	public String getValidateLat()
	{
		return validateLat;
	}

	public void setValidateLat(String validateLat)
	{
		validateLon = validateLon.replace(",", ".");
		this.validateLat = validateLat;
	}

	public void setCoordinatesBean(Restaurant u)
	{
		try
		{
			this.validateLat = u.getAddressRestaurant().getWgs84Latitude().toString();
			this.validateLon = u.getAddressRestaurant().getWgs84Longitude().toString();
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
			current.getAddressRestaurant().setWgs84Latitude(null);
		}
		else
		{
			current.getAddressRestaurant().setWgs84Latitude(Double.parseDouble(validateLat));
		}

		if(null == validateLon || validateLon.equals("") || validateLon.isEmpty() ||validateLon.isBlank())
		{
			current.getAddressRestaurant().setWgs84Longitude(null);
		}
		else
		{
			current.getAddressRestaurant().setWgs84Longitude(Double.parseDouble(validateLon));
		}
	}

}
