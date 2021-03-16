package de.marcel.restaurant.ejb.interfaces;

import de.marcel.restaurant.ejb.model.Culinary;
import de.marcel.restaurant.ejb.model.Restaurant;
import de.marcel.restaurant.ejb.model.RestaurantVisit;
import de.marcel.restaurant.ejb.model.User;

import java.io.Serializable;
import java.time.ZonedDateTime;

public interface IRating extends IBaseEntity, Serializable
{
	Restaurant getRestaurantRated();

	void setRestaurantRated(Restaurant restaurantRated);

	RestaurantVisit getVisit();

	void setVisit(RestaurantVisit visit);

	User getRatingUser();

	void setRatingUser(User ratingUser);

	ZonedDateTime getRatingDateTime();

	void setRatingDateTime(ZonedDateTime ratingDateTime);

	byte getStars();

	void setStars(byte stars);

	double getPrice();

	void setPrice(double price);

	Culinary getMeal();

	void setMeal(Culinary meal);

	String getMealDescription();

	void setMealDescription(String mealDescription);

}
