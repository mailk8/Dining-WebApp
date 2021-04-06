package de.marcel.restaurant.web.converters;

import de.marcel.restaurant.ejb.model.Dish;
import de.marcel.restaurant.web.backingBeans.RatingBean;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.logging.Logger;

@Named
@ViewScoped
@FacesConverter("dishConverter")
public class DishConverter implements Converter<Dish>, Serializable
{
	@Inject RatingBean ratingBean;

	@Override public Dish getAsObject(FacesContext context, UIComponent component, String stringValue)
	{
		Logger.getLogger(getClass().getSimpleName()).severe("+# getAsObject stringValue " + stringValue);
		return ratingBean.getAllDishesProxy().stream().filter(e -> e.toString().equals(stringValue)).findAny().get();
	}

	@Override public String getAsString(FacesContext context, UIComponent component, Dish value)
	{
		return value.toString();
	}

}