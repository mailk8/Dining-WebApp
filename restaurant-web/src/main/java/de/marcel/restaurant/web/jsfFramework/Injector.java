package de.marcel.restaurant.web.jsfFramework;

import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.Serializable;

@SessionScoped
public class Injector implements Serializable
{
	@Produces
	private IRestaurantEJB getAppServer()
	{
		IRestaurantEJB appServer = null;
		String[] jndiName = {"java:global/restaurant-ejb-1/RestaurantEJB!de.marcel.restaurant.ejb.interfaces.IRestaurantEJB"};
		try
		{
			InitialContext ic = new InitialContext();
			appServer = (IRestaurantEJB) ic.doLookup(jndiName[0]);
		}
		catch (NamingException e)
		{
			Exception newEx = new Exception("Fehler beim Anfordern des AppServers mit " + jndiName[0] + " in " + this.getClass());
			newEx.printStackTrace();
		}
		return appServer;

//		Weld weld = new Weld();
//		WeldContainer container = weld.initialize();
//		IRestaurantEJB myp = container.select(IRestaurantEJB.class).get();
//		return myp;
	}


//	@Produces
//	private LoginController getLoginController()
//	{
//		return new LoginController();
//	}
}
