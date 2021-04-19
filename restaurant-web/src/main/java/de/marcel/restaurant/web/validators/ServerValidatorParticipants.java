package de.marcel.restaurant.web.validators;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("serverValidatorParticipants")
public class ServerValidatorParticipants implements Validator {

	private FacesMessage.Severity severity = FacesMessage.SEVERITY_ERROR;
	private String errorMessage = "Bitte mindestens einen Teilnehmer wählen";

	public String getValidatorId() {
		return "serverValidatorParticipants";
	}

	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException
	{
		// Validiert wird die aktuelle Anzahl der als Teilnehmer eingetragenen User.
		if( null != value )
		{
			Integer amount = Integer.parseInt(value.toString());
			// ist mindestens ein Teilnehmer gewählt, ist alles okay, sonst: Validierung

			if (amount <= 0)
			{
				String initialValue = ((UIInput)(component.getParent().getChildren().get(2))).getSubmittedValue().toString(); // hier wird das hiddenField 'sizeParticipants_Initial' ausgelesen
				if(Integer.parseInt(initialValue) <= 0)
				{
					// war die initiale Anzahl 0: vermutlich Neuanlage, dann soll mindestens ein Teilnehmer gewählt werden
					throwFacesErrorMessage(context, component);
					return;
				}
				// war die initiale Anzahl > 0 und new Value = 0 bedeutet wohl, letzter Teilnehmer möchte sich aus Visit löschen. Das soll möglich sein.
			}
		}
		else
		{
			throwFacesErrorMessage(context, component);
			return;
		}
	}

	private void throwFacesErrorMessage(FacesContext context, UIComponent component)
	{
		((UIInput)component).setValid(false);
		context.addMessage(null, new FacesMessage(severity, errorMessage, null)); // null means Global Message: Multi View Support
	}
}