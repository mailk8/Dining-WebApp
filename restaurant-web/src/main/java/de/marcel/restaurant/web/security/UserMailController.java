package de.marcel.restaurant.web.security;

import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import de.marcel.restaurant.ejb.model.User;

import javax.ejb.*;
import javax.inject.Named;
import javax.inject.Inject;
import javax.annotation.PostConstruct;
import java.util.TreeSet;
import java.util.logging.Logger;

@Singleton
@Named
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class UserMailController
{
	@Inject private IRestaurantEJB appServer;

	private static TreeSet<String> emailTree = new TreeSet<>();

	@PostConstruct
	@Lock(LockType.WRITE)
	private void fetchAllUserEmails()
	{
		appServer.findAll(User.class).forEach(e -> {
			String s = ((User) e).getEmail();
			if(null!=s)
			{
				emailTree.add(s);
			}
		});
	}

	@Lock(LockType.WRITE)
	@Asynchronous // Erwerben des Locks kann async. geschehen ?
	public static void putNewUserEmail(String newEmail)
	{
		if(null != newEmail){
			if (emailTree.contains(newEmail))
				emailTree.remove(newEmail);
			emailTree.add(newEmail);
		}
	}

	@Lock(LockType.WRITE)
	@Asynchronous // Erwerben des Locks kann async. geschehen ?
	public static void deleteUserEmail(String email)
	{
		if(null != email)
			emailTree.remove(email);
	}

	@Lock(LockType.READ)
	public static boolean containsUserEmail(String input)
	{
		Logger.getLogger(UserMailController.class.getSimpleName()).severe("+# Duplikatscheck email bereits vorhanden? " + emailTree.contains(input) + " Value war "+input );
		return emailTree.contains(input);
	}

	public void getMailContainer()
	{
		Logger.getLogger(UserMailController.class.getSimpleName()).severe("----------------------------------------------------------"  );
		Logger.getLogger(UserMailController.class.getSimpleName()).severe("+# MailContainer Size: " + emailTree.size()  );

		emailTree.forEach(e-> Logger.getLogger(UserMailController.class.getSimpleName()).severe("+# getMailContainer: " + e  ));
		Logger.getLogger(UserMailController.class.getSimpleName()).severe("----------------------------------------------------------"  );
	}
}
