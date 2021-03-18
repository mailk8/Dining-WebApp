package de.marcel.restaurant.ejb.interfaces;

import de.marcel.restaurant.ejb.interfaces.IBaseEntity;
import de.marcel.restaurant.ejb.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

public interface IRestaurantVisit extends IBaseEntity
{
	// GETTER SETTER
	LocalDate getVisitingDate();

	void setVisitingDate(LocalDate visitingDateTime);

	LocalDateTime getVisitingDateTime();

	String getMemo();

	void setMemo(String memo);

	List<User> getParticipants();

	String getParticipantsAsString();

	void setParticipants(List<User> users);

	Set<Rating> getRatingsVisit();

	void setRatingsVisit(Set<Rating> ratingsVisit);

	byte getAverageRating();

	void setAverageRating(byte averageRating);

	Restaurant getRestaurantChosen();

	void setRestaurantChosen(Restaurant restaurantChosen);

	Enum getStateVisit();

	void setStateVisit(State stateVisit);

	Culinary getChosenCulinary();

	void setChosenCulinary(Culinary chosenCulinary);

	Address getAddressVisit();

	void setAddressVisit(Address addressVisit);

	LocalTime getVisitingTime();

	void setVisitingTime(LocalTime visitingTime);

	String getTimezone();

	void setTimezone(String timezone);

	@Override Integer getPrim();

	@Override void setPrim(Integer prim);

	@Override Integer getId();

	@Override void setId(Integer id);

	@Override String toString();
}
