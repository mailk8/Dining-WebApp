package de.marcel.restaurant.web;

import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

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
		IRestaurantEJB ir = null;
		String[] jndiName = {"java:global/restaurant-ejb-1/RestaurantEJB!de.marcel.restaurant.ejb.interfaces.IRestaurantEJB"};
		try
		{
			InitialContext ic = new InitialContext();
			ir = (IRestaurantEJB) ic.doLookup(jndiName[0]);
		}
		catch (NamingException e)
		{
			Exception newEx = new Exception("Fehler beim Anfordern des AppServers mit " + jndiName[0] + " in " + this.getClass());
			newEx.printStackTrace();
		}
		return ir;

//		Weld weld = new Weld();
//		WeldContainer container = weld.initialize();
//		IRestaurantEJB myp = container.select(IRestaurantEJB.class).get();
//
//		return myp;
	}
}
