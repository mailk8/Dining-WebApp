package de.marcel.restaurant.ejb.interfaces;

import de.marcel.restaurant.ejb.model.Culinary;

import java.io.Serializable;

public interface IDish extends IBaseEntity, Serializable
{

	String getDishName();

	void setDishName(String dishName);

}
