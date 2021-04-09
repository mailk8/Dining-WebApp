package de.marcel.restaurant.web.validators;

import javax.ejb.Local;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Named;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.logging.Logger;

@Named
@RequestScoped
@FacesValidator("ServerValidatorVorlage")
public class ServerValidatorVorlage implements Validator {

	private FacesMessage.Severity severity = FacesMessage.SEVERITY_ERROR;
	private String errorMessage = "xxxxxxxxxxxxxxxxxxxxxxx";
	private String detail = "xxxxxxxxxxxxxxxxxxxxxxx";

	public String getValidatorId() {
		return "ServerValidatorVorlage";
	}

	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException
	{
		Logger.getLogger(this.getClass().getSimpleName()).severe("+# value ist vom Typ: "+value.getClass() +" toString ergibt " + value.toString() );

		if( null != value ){

			String valueString = value.toString().trim();

			if (valueString.equals(""))
			{
				throwFacesErrorMessage(context, component, errorMessage, detail);
				return;
			}

			try
			{
				LocalDate.parse(valueString);
			}
			catch (DateTimeParseException e)
			{
				throwFacesErrorMessage(context, component, errorMessage, detail);
				return;
			}

		}
		else
		{
			throwFacesErrorMessage(context, component, errorMessage, detail);
			return;
		}
	}

	private void throwFacesErrorMessage(FacesContext context, UIComponent component, String message, String detail)
	{
		((UIInput)component).setValid(false);
		context.addMessage(null, new FacesMessage(severity, message, detail)); // null means Global Message: Multi View Support
	}


}