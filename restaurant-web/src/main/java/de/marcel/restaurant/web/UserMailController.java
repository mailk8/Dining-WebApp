package de.marcel.restaurant.web;

import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import de.marcel.restaurant.ejb.model.User;
import org.omnifaces.util.Faces;

import javax.ejb.*;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.PhaseId;
import javax.inject.Named;
import javax.inject.Inject;
import javax.annotation.PostConstruct;
import java.util.Iterator;
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

	private static TreeSet<String> emailTree = new TreeSet<>();

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
	public static boolean check(String input)
	{

		//event.getFacesContext().setProcessingEvents(false);

//		UIComponent uc = event.getComponent();
//		UIInput ui = (UIInput) event.getSource();
//
//		String input = ui.getSubmittedValue().toString(); //////////////// °!!!!!!!!!!
//
//		Logger.getLogger(getClass().getSimpleName()).severe("+# Habe als email erhalten: " + input  );
//
//		Logger.getLogger(getClass().getSimpleName()).severe("+# UIComponent renderType" + uc.getRendererType() + " clientId " + uc.getClientId()  );
//		Logger.getLogger(getClass().getSimpleName()).severe("+# UIInput renderType" + ui.getRendererType() + " clientId " + ui.getClientId()  );
//
//		Iterator<String> it = event.getFacesContext().getClientIdsWithMessages();
//		while(it.hasNext())
//		{
//			Logger.getLogger(getClass().getSimpleName()).severe("+# getFacesContext().getClientIdsWithMessages" + it.next());
//		}
//
//
//		if(emailTree.contains(input))
//		{
//			Logger.getLogger(getClass().getSimpleName()).severe("+# Check email bereits vorhanden " + emailTree.contains(input)  );
//
//			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Bereits vorhanden sum", "Bereits vorhanden det"));
//			event.getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Bereits vorhanden sum", "Bereits vorhanden det"));
//
//			FacesContext.getCurrentInstance().addMessage("messageForEmailP", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Bereits vorhanden sum", "Bereits vorhanden det"));
//			event.getFacesContext().addMessage("messageForEmailP", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Bereits vorhanden sum", "Bereits vorhanden det"));
//
//			FacesContext.getCurrentInstance().addMessage("messageForEmailH", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Bereits vorhanden sum", "Bereits vorhanden det"));
//			event.getFacesContext().addMessage("messageForEmailH", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Bereits vorhanden sum", "Bereits vorhanden det"));
//
//			FacesContext.getCurrentInstance().addMessage("email", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Bereits vorhanden sum", "Bereits vorhanden det"));
//			event.getFacesContext().addMessage("email", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Bereits vorhanden sum", "Bereits vorhanden det"));
//
//
//
//
//			ui.setValid(false);
//			((UIInput)uc).setValid(false);
//		}
//
//		FacesContext.getCurrentInstance().renderResponse();

		Logger.getLogger(UserMailController.class.getSimpleName()).severe("+# Check email bereits vorhanden " + emailTree.contains(input)  );

		return emailTree.contains(input);


	}
}
