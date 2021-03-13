package de.marcel.restaurant.ejb.interfaces;

import java.io.Serializable;
import java.net.URI;

public interface IAddress extends Serializable
{
	String getStreet();

	void setStreet(String street);

	String getHouseNumber();

	void setHouseNumber(String houseNumber);

	String getZipCode();

	void setZipCode(String zipCode);

	String getCity();

	void setCity(String city);

	String getGooglePlace();

	void setGooglePlace(String googlePlace);

	Double getWgs84Latitude();

	void setWgs84Latitude(Double wgs84Latitude);

	Double getWgs84Longitude();

	void setWgs84Longitude(Double wgs84Longitude);


	int getCounterApiCalls();

	void setCounterApiCalls(int counterApiCalls);

	URI getWgsRestApiCall();

	void setWgsRestApiCall(URI wgsRestApiCall);

	@Override String toString();
}
