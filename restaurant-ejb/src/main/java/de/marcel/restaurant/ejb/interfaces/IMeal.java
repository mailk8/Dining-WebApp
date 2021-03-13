package de.marcel.restaurant.ejb.interfaces;

import de.marcel.restaurant.ejb.model.Culinary;

import java.io.Serializable;

public interface IMeal extends IBaseEntity, Serializable
{
	Culinary getMealCategory();

	void setMealCategory(Culinary mealCategory);

	String getMealName();

	void setMealName(String mealName);

}
