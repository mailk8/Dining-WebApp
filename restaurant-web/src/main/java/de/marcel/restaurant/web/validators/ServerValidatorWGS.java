package de.marcel.restaurant.web.validators;

import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Named;


@Named
@RequestScoped
@FacesValidator("serverValidatorWGS")
public class ServerValidatorWGS implements Validator {

	private Pattern pattern;
	private static final String WGS84_PATTERN = "^((?!((,|\\.)(.*)(,|\\.))|((\\+|\\-)(.*)(\\+|\\-))|([^0-9,\\.+-])|(([0-9,\\.]\\+)|([0-9,\\.]\\-)|((\\+|\\-)$)|(.\\s.))).)*$";

	public ServerValidatorWGS() {
		pattern = Pattern.compile(WGS84_PATTERN);
	}

	public void validate(FacesContext context, UIComponent component, Object val) throws ValidatorException
	{
		Logger.getLogger(this.getClass().getSimpleName()).severe("+# validate anfang mit ");

		String valueString = val.toString();

		////// Bei keinem Eintrag nichts prüfen //////
		if(valueString.equals(""))
			return;

//		String valueString = value.toString().trim();
//		valueString = valueString.replace(",", ".");


		////// Keine fehlerhaften Zeichen oder mehrere Punkte bzw Kommata //////
		if(!pattern.matcher(valueString).matches())
		{
			((UIInput)component).setValid(false);
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fail", valueString + "\nist keine valide WGS84-Koordinate."));
			return;
		}



		////// Lässt sich der Wert in einen Double umwandeln //////
		Double valueDouble = null;
		try
		{
			valueDouble = Double.parseDouble(valueString);
		}
		catch (NumberFormatException e)
		{
			((UIInput)component).setValid(false);
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", valueString + "\nist kein valider Dezimalwert."));
			return;
		}

		////// Wertprüfung Latitude //////
		if(component.getId().equals("wgs84Lat") && ((valueDouble > 90) || (valueDouble < -90)))
		{
			((UIInput)component).setValid(false);
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Breitengrad muss zwischen +90° und -90° liegen.\n" + valueString + " ist nicht valide."));
			return;
		}

		////// Wertprüfung Longitude //////
		if(component.getId().equals("wgs84Lon") && ((valueDouble > 180) || (valueDouble < -180)))
		{
			((UIInput)component).setValid(false);
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Längengrad muss zwischen +180° und -180° liegen.\n" + valueString + " ist nicht valide."));
			return;
		}

		Logger.getLogger(this.getClass().getSimpleName()).severe("+# validate ende");



	}


}