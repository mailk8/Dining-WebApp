package de.marcel.restaurant.web.backingBeans;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import de.marcel.restaurant.ejb.model.Culinary;
import de.marcel.restaurant.ejb.model.Restaurant;
import de.marcel.restaurant.ejb.model.User;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//@MangedBean // JSF managed die Bean -> deprecated
@Named // CDI managed die Bean
@SessionScoped
public class BackingBeanRestaurant implements Serializable
{
	private static final long serialVersionUID = 1L;
	private Restaurant current = new Restaurant();
	private String validateLon, validateLat;

	@Inject
	//@EJB
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
		//Logger.getLogger(getClass().getSimpleName()).log(Level.WARNING, "Aufruf SaveRestaurant - current ist " + current);

		if(current != null)
		{
			if(null == current.getPrim())
			{
				setCoordinatesEntity();
				insert(current);
			}

			else
			{
				setCoordinatesEntity();
				update(current);
			}
		}

		current = new Restaurant();
		return "RestaurantList?faces-redirect=true";
	}

	public void insert(Restaurant u)
	{
		appServer.persist(u);
	}

	public void update(Restaurant u)
	{
		appServer.update(u);
	}

	public String edit(Restaurant u)
	{
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
		current = new Restaurant();
		setCoordinatesBean(current);

		return "RestaurantCreate?faces-redirect=true";
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

	public void setCoordinatesEntity()
	{
		if(validateLat.equals(""))
			current.getAddressRestaurant().setWgs84Latitude(null);

		if(validateLon.equals(""))
			current.getAddressRestaurant().setWgs84Longitude(null);

		try
		{
			current.getAddressRestaurant().setWgs84Latitude(Double.parseDouble(validateLat));
			current.getAddressRestaurant().setWgs84Longitude(Double.parseDouble(validateLon));
		}
		catch (Exception e)
		{
			// null soll erlaubt sein wenn kein Wert gesetzt ist
			//throw new NullPointerException("Fehler in setCoordinatesEntity");
		}
	}

}
