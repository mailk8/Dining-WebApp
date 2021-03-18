package de.marcel.restaurant.ejb.interfaces;

import de.marcel.restaurant.ejb.model.Address;
import de.marcel.restaurant.ejb.model.Culinary;
import de.marcel.restaurant.ejb.model.RestaurantVisit;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Set;

public interface IRestaurant extends IBaseEntity, Serializable
{
	@PostConstruct void standardOffDays();

	// GETTER SETTER
	String getName();

	void setName(String name);

	Address getAddressRestaurant();

	void setAddressRestaurant(Address a);

	String getPhoneNumber();

	void setPhoneNumber(String phoneNumber);

	String getEmail();

	void setEmail(String email);

	String getLinkMenu();

	void setLinkMenu(String linkMenu);

	LocalTime getOpenFrom();

	void setOpenFrom(LocalTime openFrom);

	LocalTime getOpenTill();

	void setOpenTill(LocalTime openTill);

	LocalDate getHolidayFrom();

	void setHolidayFrom(LocalDate holidayFrom);

	LocalDate getHolidayTill();

	void setHolidayTill(LocalDate holidayTill);

	Collection<DayOfWeek> getDaysOfRest();

	void setDaysOfRest(DayOfWeek dayOfRest);

	Culinary getCulinary();

	void setCulinary(Culinary culinary);

	byte getAverageRating();

	void setAverageRating(byte averageRating);

	Set<RestaurantVisit> getVisits();

	void setVisits(Set<RestaurantVisit> visits);

}
