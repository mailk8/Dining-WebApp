package de.marcel.restaurant.web;

import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import de.marcel.restaurant.ejb.model.User;

import javax.ejb.*;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Named;
import javax.inject.Inject;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

@Singleton
@Named
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class UserMailController
{
	@Inject private IRestaurantEJB appServer;

	private TreeSet<String> emailTree = new TreeSet<>();

	@PostConstruct
	@Lock(LockType.WRITE)
	private void fetchAllUserEmails()
	{
		appServer.findAll(User.class).forEach(e -> emailTree.add(((User)e).getEmail()));
	}

	@Lock(LockType.WRITE)
	public boolean putNewUser(String newEmail)
	{
		return emailTree.add(newEmail);
	}

	@Lock(LockType.WRITE)
	public boolean removeDeleted(String newEmail)
	{
		return emailTree.remove(newEmail);
	}

	@Lock(LockType.READ)
	public void check(AjaxBehaviorEvent event)
	{
		UIComponent uc = event.getComponent();
		UIInput ui = (UIInput) event.getSource();
		String input = ui.getValue().toString();
		Logger.getLogger(getClass().getSimpleName()).severe("+# Habe als email erhalten: " + input  );

		if(emailTree.contains(input))
		{
			ui.setValid(false);
			((UIInput)uc).setValid(false);
		}
	}
}
