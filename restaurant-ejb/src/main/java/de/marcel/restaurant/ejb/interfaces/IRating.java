package de.marcel.restaurant.ejb.interfaces;

import de.marcel.restaurant.ejb.model.*;

import java.io.Serializable;
import java.time.LocalDate;

public interface IRating extends IBaseEntity, Serializable
{
	Restaurant getRestaurantRated();

	void setRestaurantRated(Restaurant restaurantRated);

	RestaurantVisit getVisit();

	void setVisit(RestaurantVisit visit);

	User getRatingUser();

	void setRatingUser(User ratingUser);

	LocalDate getRatingDateTime();

	void setRatingDateTime(LocalDate ratingDateTime);

	byte getStars();

	void setStars(byte stars);

	double getPrice();

	void setPrice(double price);

	Dish getDish();

	void setDish(Dish dish);

	String getDishMemo();

	void setDishMemo(String dishDescription);

}
