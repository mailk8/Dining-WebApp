package de.marcel.restaurant.web.validators;

import de.marcel.restaurant.web.security.UserMailController;
import org.omnifaces.util.Faces;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.faces.event.PostValidateEvent;
import javax.faces.event.SystemEventListener;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

@FacesValidator("serverValidatorEmail")
public class ServerValidatorEmail implements Validator
{
	@Override public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException
	{
		Logger.getLogger(this.getClass().getSimpleName()).severe("+# Validator Anfang ######################### validation failed ? " + context.isValidationFailed());

		Iterator<FacesMessage> it = context.getMessages();
		while ( it.hasNext() ) {
			it.next();
			it.remove();
		}

		String input = value.toString().trim();

		if(input.isEmpty() || input.isEmpty())
		{
			((UIInput)component).setValid(false);
			context.addMessage("email", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Eine E-Mail ist erforderlich",  ""));
			FacesContext.getCurrentInstance().renderResponse();
			return;
		}

		if(UserMailController.containsUserEmail(input)){
			context.addMessage("email", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Diese E-Mail wird bereits benutzt",  ""));
			((UIInput)component).setValid(false);
			FacesContext.getCurrentInstance().renderResponse();
			return;

		}


		context.addMessage("email", new FacesMessage(FacesMessage.SEVERITY_INFO, "OK, diese E-Mail wird noch nicht benutzt", ""));
		((UIInput)component).setValid(true);




		Logger.getLogger(this.getClass().getSimpleName()).severe("+# Validator vor Render ############### validation failed ? " + context.isValidationFailed() );

//		context.setProcessingEvents(false);
//		context.release();
//		context.renderResponse();
		context.setCurrentPhaseId(PhaseId.INVOKE_APPLICATION);


		Logger.getLogger(this.getClass().getSimpleName()).severe("+# Validator läuft bis Ende ################ validation failed ? " + context.isValidationFailed());



	}
}
