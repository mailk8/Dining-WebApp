package de.marcel.restaurant.ejb.interfaces;

import de.marcel.restaurant.ejb.model.Meal;

import java.io.Serializable;
import java.util.Set;

public interface ICulinary extends IBaseEntity, Serializable
{
	Set<Meal> getMeals();

	void setMeals(Set<Meal> meals);

	String getCategory();

	void setCategory(String category);


	String toString();
}
