//package de.marcel.restaurant.web.validators;
//
//import org.omnifaces.util.Faces;
//
//import javax.faces.application.FacesMessage;
//import javax.faces.component.UIComponent;
//import javax.faces.component.UIInput;
//import javax.faces.context.FacesContext;
//import javax.faces.validator.FacesValidator;
//import javax.faces.validator.Validator;
//import javax.faces.validator.ValidatorException;
//import java.util.logging.Logger;
//import java.util.regex.Pattern;
//
//@FacesValidator("currencyValidator")
//public class CurrencyValidator implements Validator
//{
//
//	private Pattern patternNumber;
//	private static final String CURRENCY_PATTERN = "^((?!((,|\\.)(.*)(,|\\.))|((\\+)(.*)(\\+))|([a-z])|([\\-])|(([0-9,\\.]\\+)|([0-9,\\.]\\-)|((\\+)$)|(.\\s.))).)*$";
//	private Pattern patternDecimalPoints;
//	private static final String DECIMAL_PATTERN = "^((?!(,(\\d\\d\\d))|(\\.(\\d\\d\\d))).)*$";
//	private FacesMessage.Severity severity = FacesMessage.SEVERITY_ERROR;
//
//	public CurrencyValidator() {
//
//		patternNumber = Pattern.compile(CURRENCY_PATTERN);
//		patternDecimalPoints = Pattern.compile(DECIMAL_PATTERN);
//	}
//
//	@Override public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException
//	{
//
//		Logger.getLogger(this.getClass().getSimpleName()).severe("+# validate mit: " + value + " object ist vom typ " +value.getClass() +  " in Phase " + Faces.getCurrentPhaseId());
//
//		String valueString = value.toString();
//
//
////		if(!patternNumber.matcher(valueString).matches())
////		{
////			Logger.getLogger(this.getClass().getSimpleName()).severe("+# validate: unerlaubtes Zeichen ");
////			throwFacesErrorMessage(context, component, "Unerlaubtes Zeichen!", null);
////			return;
////		}
////
////		if(!patternDecimalPoints.matcher(valueString).matches())
////		{
////			Logger.getLogger(this.getClass().getSimpleName()).severe("+# validate:nur zwei Nachkommastellen  ");
////			throwFacesErrorMessage(context, component, "Nur zwei Nachkommastellen!", null);
////			return;
////		}
////
////
////		if( value.equals(Double.NaN) || value.equals(Double.POSITIVE_INFINITY) || value.equals(Double.NEGATIVE_INFINITY) )
////		{
////			Logger.getLogger(this.getClass().getSimpleName()).severe("+# validate: Error ");
////			throwFacesErrorMessage(context, component, "Error!", null);
////			return;
////		}
//
//	}
//
//	private void throwFacesErrorMessage(FacesContext context, UIComponent component, String message, String detail)
//	{
//		((UIInput)component).setValid(false);
//		context.addMessage("price", new FacesMessage(severity, message, detail)); // null means Global Message: Multi View Support
//		throw new ValidatorException(new FacesMessage(severity, message, detail));
//	}
//}
