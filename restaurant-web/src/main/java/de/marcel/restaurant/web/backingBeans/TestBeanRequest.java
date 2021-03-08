package de.marcel.restaurant.web.backingBeans;

import de.marcel.restaurant.web.UserMailController;
import org.slf4j.LoggerFactory;

import javax.annotation.ManagedBean;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.PhaseId;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.Serializable;
import java.util.logging.Logger;

// @ManagedBean  // braucht man nicht
@Named // wird benötigt
//@RequestScoped // funktioniert
//@SessionScoped // funktioniert
//@ApplicationScoped  // funktioniert
@Singleton // funktioniert
public class TestBeanRequest implements Serializable
{

	public void isEmailDuplicated(AjaxBehaviorEvent event)
	{
		Logger.getLogger(this.getClass().getSimpleName()).severe("+# isEmailDuplicated aufgerufen "  );

		FacesMessage facesMessage = new FacesMessage("The actionListener method was called ");
		//facesContext.addMessage(null, facesMessage);
		event.getFacesContext().addMessage(null, facesMessage);


	}
}
