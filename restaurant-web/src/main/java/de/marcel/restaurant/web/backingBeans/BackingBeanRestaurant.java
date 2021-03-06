package de.marcel.restaurant.web.backingBeans;

import javax.annotation.ManagedBean;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import de.marcel.restaurant.ejb.model.Address;
import de.marcel.restaurant.ejb.model.Culinary;
import de.marcel.restaurant.ejb.model.Restaurant;
import de.marcel.restaurant.web.httpClient.HttpClientWGS;
import de.marcel.restaurant.web.jsfFramework.WebSocketObserver;
import org.omnifaces.util.Faces;

import java.util.Comparator;
import java.util.List;
import java.util.Random;



@Named
@SessionScoped
@ManagedBean
public class BackingBeanRestaurant implements Serializable
{
	private static final long serialVersionUID = 1L;
	private Restaurant current;
	private List<Culinary> allCulinariesProxy;
	private List<Restaurant> allRestaurantsProxy;

	@Inject private IRestaurantEJB appServer;
	@Inject private WebSocketObserver websocket;
	@Inject private Instance<HttpClientWGS> client; // weld cdi workaround

	///////////////////////////// Methods for Performace Enhancement ////////////////////////////
	public List<Culinary> getAllCulinariesProxy()
	{
		return allCulinariesProxy;
	}

	public List<Restaurant> getAllRestaurantsProxy()
	{
		return allRestaurantsProxy;
	}

	///////////////////////////// Methods for Basic Crud /////////////////////////////////////////
	public List<Culinary> getAllCulinaries()
	{
		this.allCulinariesProxy = appServer.findAll(Culinary.class);
		return allCulinariesProxy;
	}

	public List<Restaurant> getAllRestaurants()
	{
		allRestaurantsProxy = appServer.findAll(Restaurant.class);
		allRestaurantsProxy.sort(Comparator.comparing(e -> e.getName()));
		return allRestaurantsProxy;
	}

	public void setCurrent(Restaurant u)
	{
		this.current = u;
		
	}

	public String saveRestaurantProxy() {
		String redirect = save();
		websocket.sendMessage(Restaurant.class);
		return redirect;
	}

	public String save()
	{
		if(null == current.getPrim())
		{
			insert(current);
		}

		else
		{
			update(current);
		}

		return "RestaurantList?faces-redirect=true";
	}

	public void insert(Restaurant u)
	{
		int result = appServer.persist(u);
		current.setPrim(result);
	}

	public void update(Restaurant u)
	{
		appServer.update(u);
	}

	public String edit(Restaurant u)
	{
		this.current = u;
		return "RestaurantCreate?faces-redirect=true";
	}

	public String delete(Restaurant u)
	{
		appServer.delete(u);
		websocket.sendMessage(Restaurant.class);
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
		return "RestaurantCreate?faces-redirect=true";
	}

	///////////////////////////// Methods for WGS Coordinates /////////////////////////////////
	public void requestWgsForAddress() {
		Address adr = current.getAddressRestaurant();
		adr.setSessionId(getSessionId());
		client.get().enqueueNewRequest(adr);
	}

	public String getSessionId() {
		return Faces.getSessionId();
	}
}
