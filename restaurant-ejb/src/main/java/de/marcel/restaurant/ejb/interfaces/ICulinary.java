package de.marcel.restaurant.ejb.interfaces;

import de.marcel.restaurant.ejb.model.Dish;

import java.io.Serializable;
import java.util.Set;

public interface ICulinary extends IBaseEntity, Serializable
{
	String getCategory();

	void setCategory(String category);

	String toString();
}
