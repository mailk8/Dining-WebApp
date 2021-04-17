package de.marcel.restaurant.web.converters;

import org.omnifaces.util.Faces;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.logging.Logger;

@FacesConverter("commaConverterCurrency")
public class CommaConverterCurrency implements Converter<Double>
{

	@Override public Double getAsObject(FacesContext context, UIComponent component, String stringValue)
	{
		try
		{
			Logger.getLogger(this.getClass().getSimpleName()).severe("+# getAsObject mit: " + stringValue + " in Phase " + Faces.getCurrentPhaseId());

			if(null == stringValue || stringValue.isEmpty())
			{
				return 0.0;
			}

			stringValue = stringValue.replace("€", "");
			stringValue = stringValue.replace(",", ".");
			stringValue = stringValue.trim();

			BigDecimal bd = new BigDecimal(Double.parseDouble(stringValue)).setScale(2, RoundingMode.HALF_UP);
			return  bd.doubleValue();
		}
		catch (Exception e)
		{
			Logger.getLogger(this.getClass().getSimpleName()).severe("+# catch (Exception e) mit: " + stringValue + " in Phase " + Faces.getCurrentPhaseId());
			throwFacesErrorMessage(context, component, "Kein valider Betrag!", null, "price" );
			//e.printStackTrace();
			return 0.0;
		}
	}

	@Override public String getAsString(FacesContext context, UIComponent component, Double value)
	{
		if(value <= 0.001 || null == value || value.isInfinite() || value.isNaN())
		{
			return "";
		}

//		NumberFormat currFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
		NumberFormat currFormatter = NumberFormat.getNumberInstance(Locale.GERMANY);
		currFormatter.setMaximumFractionDigits(2);
		currFormatter.setMinimumFractionDigits(2);
		Logger.getLogger(this.getClass().getSimpleName()).severe("+# getAsString mit: " + value + " in Phase " + Faces.getCurrentPhaseId());

		return currFormatter.format(value);

	}

	private void throwFacesErrorMessage(FacesContext context, UIComponent component, String message, String detail, String clientID)
	{
		FacesMessage.Severity severity = FacesMessage.SEVERITY_ERROR;

		((UIInput)component).setValid(false);
		context.addMessage(clientID, new FacesMessage(severity, message, detail)); // null means Global Message: Multi View Support
//		throw new ValidatorException(new FacesMessage(severity, message, detail));
	}
}