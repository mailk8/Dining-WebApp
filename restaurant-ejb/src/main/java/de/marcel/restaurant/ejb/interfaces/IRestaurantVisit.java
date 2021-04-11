package de.marcel.restaurant.ejb.interfaces;

import de.marcel.restaurant.ejb.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
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

	String getParticipantsAsString(User u);

	void setParticipants(List<User> users);

	Set<Rating> getRatings();

	void setRatings(Set<Rating> ratings);

	Restaurant getRestaurantChosen();

	void setRestaurantChosen(Restaurant restaurantChosen);

	Enum getStateVisit();

	void setStateVisit(State stateVisit);

	List<Culinary> getChosenCulinaries();

	void setChosenCulinaries(List<Culinary> chosenCulinaries);

	Address getAddressVisit();

	void setAddressVisit(Address addressVisit);

	LocalTime getVisitingTime();

	void setVisitingTime(LocalTime visitingTime);

	String getTimezoneString();

	void setTimezoneString(String timezone);

	ZonedDateTime getVisitingZonedDateTime();

	LocalDateTime getVisitingLocalDateTime();

	@Override Integer getPrim();

	@Override void setPrim(Integer prim);

	@Override Integer getId();

	@Override void setId(Integer id);

	@Override String toString();
}
