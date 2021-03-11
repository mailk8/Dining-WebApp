package de.marcel.restaurant.web.security;

import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import de.marcel.restaurant.ejb.model.User;
import org.omnifaces.cdi.Eager;

import javax.annotation.Priority;
import javax.ejb.*;
import javax.inject.Named;
import javax.inject.Inject;
import javax.annotation.PostConstruct;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

@Singleton
@Named
@Startup()
@Priority(200)
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class UserMailController
{
	@Inject
	private IRestaurantEJB appServer; // Hat @Priority 1, der Injector mit Produces-Method hat @Priority 100

	private static TreeMap<String, Integer> emailTree = new TreeMap<>();

	@PostConstruct
	@Lock(LockType.WRITE)
	private void fetchAllUserEmails()
	{
		Logger.getLogger(UserMailController.class.getSimpleName()).severe("+# EMailContainer PostConstruct. " );
		// Das Singleton wird NICHT automatisch erzeugt, erst beim Aufruf seines Namens.
		appServer.findAll(User.class).forEach(e -> {
			User u = (User) e;
			String s = u.getEmail();
			if(null!=s)
			{
				emailTree.put(s, u.getPrim());
			}
		});

		getMailContainer();
	}

	@Lock(LockType.WRITE)
	public static void putNewUserEmail(User newUser)
	{
		String s = newUser.getEmail();
		if(null != s){
			if (emailTree.containsKey(s))
				emailTree.remove(s);
			emailTree.put(s, newUser.getPrim());
		}
	}

	@Lock(LockType.WRITE)
	public static void deleteUserEmail(String email)
	{
		if(null != email)
			emailTree.remove(email);
	}

	@Lock(LockType.READ)
	public static boolean containsUserEmail(String input, Object hiddenId)
	{
		Integer id = (Integer) hiddenId;
		Logger.getLogger(UserMailController.class.getSimpleName()).severe("+# Duplikatscheck email bereits vorhanden? " + emailTree.containsKey(input) + " Value war "+ input );

		Integer value = null;

		if((value = emailTree.get(input)) == null) // EMail ist gar nicht enthalten
		{
			Logger.getLogger(UserMailController.class.getSimpleName()).severe("+# Duplikatscheck email ist gar nicht enthalten? Input war" + input + " Value war "+ value + " emailTree get(input)" + emailTree.get(input));
			return false;
		}
		else if(value.equals(hiddenId)) // EMail ist enthalten und gehört dem bearbeiteten User, die darf er wieder wählen, daher false
		{
			Logger.getLogger(UserMailController.class.getSimpleName()).severe("+# Duplikatscheck email ist enthalten mit id ? " + value + " dabei ist hiddenField " + hiddenId);
			return false;
		}

		Logger.getLogger(UserMailController.class.getSimpleName()).severe("+# Duplikatscheck email ist enthalten ");
		return true; // EMail ist enthalten, gehört aber nicht zum bearbeiteten User, er darf sie nicht wählen
	}

	public void getMailContainer()
	{
		Logger.getLogger(UserMailController.class.getSimpleName()).severe("----------------------------------------------------------"  );
		Logger.getLogger(UserMailController.class.getSimpleName()).severe("+# MailContainer Size: " + emailTree.size()  );

		emailTree.forEach((k,v)-> Logger.getLogger(UserMailController.class.getSimpleName()).severe("+# getMailContainer key: " + k  + " value " + v));
		Logger.getLogger(UserMailController.class.getSimpleName()).severe("----------------------------------------------------------"  );
	}
}
