package de.marcel.restaurant.web.converters;


import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.logging.Logger;


@FacesConverter("commaConverterCoordinates")
public class CommaConverterCoordinates implements Converter
{
	NumberFormat numFormatter = NumberFormat.getNumberInstance(Locale.GERMANY);

	@Override public Object getAsObject(FacesContext context, UIComponent component, String stringValue)
	{
		try
		{
			if(null == stringValue)
			{
				return "";
			}
			stringValue = stringValue.replace(",", ".");
			stringValue = stringValue.trim();

			return  stringValue;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "";
		}

	}

	@Override public String getAsString(FacesContext context, UIComponent component, Object value)
	{
		Logger.getLogger(this.getClass().getSimpleName()).severe("+# getAsString begin mit " + value + " of class " + value.getClass());

		if( null == value )
		{
			return "";
		}

		Double d = (Double) value;

		if(d == 0.0d || d.isInfinite() || d.isNaN())
		{
			return "";
		}

		numFormatter.setMaximumFractionDigits(14);
		numFormatter.setMinimumFractionDigits(1);
		return numFormatter.format(value);

	}
}