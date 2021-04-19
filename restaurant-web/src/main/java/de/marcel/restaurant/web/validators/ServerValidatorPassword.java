package de.marcel.restaurant.web.validators;

import de.marcel.restaurant.web.backingBeans.BackingBeanUser;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
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
	http://www.passay.org/ -> Passwortqualitäts-Checker
 */


@Named
@RequestScoped
@FacesValidator("serverValidatorPassword")
public class ServerValidatorPassword implements Validator {

	@Inject BackingBeanUser backingBeanUser;

	private int apropriateLength = 4;

	public void validate(FacesContext context, UIComponent component, Object password) throws ValidatorException
	{

		////// Neuanlage eines Users //////
		if(null == backingBeanUser.getCurrent().getPrim())
		{

			if (null == password || !isApropriateLength(password.toString()))
			{
				throwFacesErrorMessage(context, component);
				((UIInput)component).setValid(false);
				return;
			}
		}
		else ////// Bestehender User wird geändert //////
		{

			if(null == password || isApropriateLength(password.toString()) || password.toString().equals("") ||  password.toString().isEmpty() || password.toString().isBlank())
			{
				// Bei Änderung eines bestehenden Users soll das Passwort nicht zwangsläufig neu eingegeben werden müssen.
				// Daher ist null oder empty String erlaubt, aber kein zu kurzes PW < passwordLength.
				return;
			}
			else
			{
				((UIInput)component).setValid(false);
				throwFacesErrorMessage(context, component);
			}
		}
	}

	private void throwFacesErrorMessage(FacesContext context, UIComponent component)
	{
		((UIInput)component).setValid(false);
		context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Achtung", "Es muss ein Passwort mit mindestens " + apropriateLength + " Zeichen eingegeben werden."));
	}

	public boolean isApropriateLength(String pw)
	{
		if(null == pw || pw.length() < apropriateLength || pw.isBlank() || pw.isEmpty())
		{
			return false;
		}

		return true;
	}
}