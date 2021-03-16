package de.marcel.restaurant.ejb.interfaces;

import de.marcel.restaurant.ejb.model.*;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Set;

public interface IRestaurantVisit extends IBaseEntity, Serializable
{
	@PostConstruct void initializeState();

	ZonedDateTime getVisitingDateTime();

	void setVisitingDateTime(ZonedDateTime visitingDateTime);

	String getMemo();

	void setMemo(String memo);

	Set<User> getParticipants();

	String getParticipantsAsString();

	void setParticipants(Set<User> participants);

	Set<Rating> getRatingsVisit();

	void setRatingsVisit(Set<Rating> ratingsVisit);

	byte getAverageRating();

	void setAverageRating(byte averageRating);

	Restaurant getRestaurantChosen();

	void setRestaurantChosen(Restaurant restaurantChosen);

	Set<Restaurant> getRestaurantSuggestions();

	void setRestaurantSuggestions(Set<Restaurant> restaurantSuggestions);

	Set<Restaurant> getRestaurantSearchHits();

	void setRestaurantSearchHits(Set<Restaurant> restaurantSearchHits);

	Enum getStateVisit();

	void setStateVisit(State stateVisit);


	Set<Culinary> getCulinaryMatchingBucket();

	void setCulinaryMatchingBucket(Set<Culinary> culinaryMatchingBucket);

	Culinary getChosenCulinary();

	void setChosenCulinary(Culinary chosenCulinary);


	Address getAddressVisit();

	void setAddressVisit(Address addressVisit);
}
