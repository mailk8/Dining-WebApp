package de.marcel.restaurant.web;

import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;

import javax.enterprise.inject.Produces;
import javax.naming.NamingException;

public class Injector
{
	@Produces
	public IRestaurantEJB getAppServer() throws NamingException
	{

	}
}
