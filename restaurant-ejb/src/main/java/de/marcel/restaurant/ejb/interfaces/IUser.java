package de.marcel.restaurant.ejb.interfaces;

import de.marcel.restaurant.ejb.model.Address;
import de.marcel.restaurant.ejb.model.Culinary;
import de.marcel.restaurant.ejb.model.Rating;

import java.io.Serializable;
import java.util.Set;

public interface IUser extends IBaseEntity, Serializable
{
	Address getAddressLiving();

	void setAddressLiving(Address addressLiving);

	Address getAddressActual();

	void setAddressActual(Address addressActual);

	Culinary getCulinaryLiking();

	void setCulinaryLiking(Culinary culinaryLiking);

	Set<Rating> getRatingsSubmitted();

	void setRatingsSubmitted(Set<Rating> ratingsSubmitted);

	String getPhoneNumber();

	void setPhoneNumber(String phoneNumber);

	String getEmail();

	void setEmail(String email);

	String getFirstname();

	void setFirstname(String firstname);

	String getLastname();

	void setLastname(String lastname);

	@Override String toString();
}
