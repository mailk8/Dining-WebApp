package de.marcel.restaurant.web.jsfFramework;

import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;

import javax.annotation.Priority;
import javax.ejb.Startup;
import javax.enterprise.inject.Produces;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.Serializable;

@Startup()
@Priority(100)
public class Injector implements Serializable
{
	@Produces
	private IRestaurantEJB getAppServer() throws Exception
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
			throw newEx;
		}
		return appServer;
	}

}
