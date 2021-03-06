package de.marcel.restaurant.web.validators;

import de.marcel.restaurant.web.backingBeans.BackingBeanUser;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.inject.Named;

/*
	https://beanvalidation.org/resources/
	http://www.passay.org/ dort gibt es anscheinend einen ausgereiften Passwortqualitäts-Checker
 */


@Named
@RequestScoped
@FacesValidator("serverValidatorPassword")
public class ServerValidatorPassword implements Validator {

	@Inject BackingBeanUser backingBeanUser;

	private int passwordLength = 4;

	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException
	{
		String email = backingBeanUser.getCurrent().getEmail();

		////// Neuanlage eines Users //////
		if(null == email || email.equals(""))
		{
			if (!isApropriateLength(value.toString()))
			{
				throwFacesErrorMessage(context, component);
				return;
			}
		}
		else ////// Bestehender User wird geändert //////
		{
			if(null == value || value.toString().equals("") || isApropriateLength(value.toString()))
			{
				// Bei Änderung eines bestehenden Users soll das Passwort nicht zwangsläufig neu eingegeben werden müssen.
				// Daher ist null oder empty String erlaubt, aber kein zu kurzes PW < passwordLength.
				return;
			}
			else
			{
				throwFacesErrorMessage(context, component);
			}
		}
	}

	private void throwFacesErrorMessage(FacesContext context, UIComponent component)
	{
		((UIInput)component).setValid(false);
		context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Achtung", "Es muss ein Passwort mit mindestens " + passwordLength + " Zeichen eingegeben werden."));
	}

	public boolean isApropriateLength(String pw)
	{
		if(pw.length() < 5)
		{
			return false;
		}

		return true;
	}

	public String getValidatorId() {
		return "serverValidatorPassword";
	}
}