package de.marcel.restaurant.ejb.interfaces;

import de.marcel.restaurant.ejb.model.Address;
import de.marcel.restaurant.ejb.model.Culinary;
import de.marcel.restaurant.ejb.model.Rating;
import de.marcel.restaurant.ejb.model.RestaurantVisit;

import java.util.Set;

public interface IUser extends IBaseEntity
{
	Address getAddressLiving();

	void setAddressLiving(Address addressLiving);

	Address getAddressActual();

	void setAddressActual(Address addressActual);

	Culinary getCulinaryLiking();

	void setCulinaryLiking(Culinary culinaryLiking);

	Set<Rating> getRatings();

	void setRatings(Set<Rating> ratings);

	String getPhoneNumber();

	void setPhoneNumber(String phoneNumber);

	String getEmail();

	void setEmail(String email);

	String getFirstname();

	void setFirstname(String firstname);

	String getLastname();

	void setLastname(String lastname);

	@Override Integer getPrim();

	@Override void setPrim(Integer prim);

	@Override Integer getId();

	@Override void setId(Integer id);

	@Override String toString();
}
