package de.marcel.restaurant.web.converters;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import java.net.MalformedURLException;
import java.net.URL;

@FacesConverter("urlStringConverter")
public class UrlStringConverter implements Converter
{

	@Override public Object getAsObject(FacesContext context, UIComponent component, String stringValue)
	{

		if(stringValue.length() == 0)
			return "";

		stringValue = stringValue.trim();

		if(!stringValue.startsWith("http", 0))
		{
			stringValue = "https://" + stringValue;
		}

		return stringValue;
	}

	@Override public String getAsString(FacesContext context, UIComponent component, Object value)
	{
		return (String) value;
	}

}