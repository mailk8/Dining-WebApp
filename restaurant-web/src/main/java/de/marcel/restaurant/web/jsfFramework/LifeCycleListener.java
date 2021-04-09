package de.marcel.restaurant.web.jsfFramework;

import org.apache.shiro.crypto.hash.Hash;
import org.omnifaces.util.Faces;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import java.util.*;
import java.util.logging.Logger;

/*
	https://www.ocpsoft.org/java/persist-and-pass-facesmessages-over-page-redirects/
	https://balusc.omnifaces.org/search/label/JSF2?max-results=100
 */
@SessionScoped
public class LifeCycleListener implements PhaseListener
{
	private static final String sessionToken = "MULTI_PAGE_MESSAGES_SUPPORT";

	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}

	public void beforePhase(PhaseEvent event) {
		//Logger.getLogger(getClass().getSimpleName()).severe("+# Start der JSF Phase " + event.getPhaseId() + " Validation failed? " + FacesContext.getCurrentInstance().isValidationFailed());

		FacesContext facesContext = event.getFacesContext();
		this.saveMessages(facesContext);

		if (PhaseId.RENDER_RESPONSE.equals(event.getPhaseId()))
		{
			if (!facesContext.getResponseComplete())
			{
				this.restoreMessages(facesContext);
			}
		}


	}

	public void afterPhase(PhaseEvent event) {
		//Logger.getLogger(getClass().getSimpleName()).severe("+# Ende der JSF Phase " + event.getPhaseId() + " Validation failed? " + FacesContext.getCurrentInstance().isValidationFailed());

		if (!PhaseId.RENDER_RESPONSE.equals(event.getPhaseId()))
		{
			FacesContext facesContext = event.getFacesContext();
			this.saveMessages(facesContext);
		}

	}

	private int saveMessages(final FacesContext facesContext)
	{
		Iterator<FacesMessage> iter = facesContext.getMessages(null); // globale
		//Iterator<FacesMessage> iter = facesContext.getMessages(); // jegliche
		if (!iter.hasNext())
		{
			return 0;
		}

		Set<FacesMessage> messages = new HashSet<FacesMessage>();

		while (iter.hasNext())
		{
			FacesMessage m = iter.next();
			messages.add(m);
			Logger.getLogger(getClass().getSimpleName()).severe("+# Message " + m.getSummary() + " " + m.getDetail() + " gefangen in Phase: " + Faces.getCurrentPhaseId() );
			iter.remove();
		}

		if (messages.size() == 0)
		{
			return 0;
		}

		Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
		Set<FacesMessage> existingMessages = (Set<FacesMessage>) sessionMap.get(sessionToken);
		if (existingMessages != null)
		{
			existingMessages.addAll(messages);
		}
		else
		{
			sessionMap.put(sessionToken, messages);
		}
		return messages.size();
	}

	private int restoreMessages(final FacesContext facesContext)
	{
		Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
		Set<FacesMessage> messages = (Set<FacesMessage>) sessionMap.remove(sessionToken);

		if (messages == null)
		{
			return 0;
		}

		int restoredCount = messages.size();
		for (Object entry : messages)
		{
			// fügt alle gefangenen Messages als globale (null) wieder in den Kontext ein !!
			// Folge: Hatte die Message vorher eine ID mit Bezug auf den Darstell-Ort,
			// hat sie den jetzt nicht mehr.

			facesContext.addMessage("price", (FacesMessage) entry);
//			FacesMessage m = (FacesMessage) entry;
//			Logger.getLogger(getClass().getSimpleName()).severe("+# Message " + m.getSummary() + " " + m.getDetail() + " restored in Phase: " + Faces.getCurrentPhaseId() );
		}
		return restoredCount;
	}

}