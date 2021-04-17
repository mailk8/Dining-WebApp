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

	Double getWgs84Latitude();

	void setWgs84Latitude(Double wgs84Latitude);

	Double getWgs84Longitude();

	void setWgs84Longitude(Double wgs84Longitude);

	void setWgs84Longitude(String wgs84Longitude);

	void setWgs84Latitude(String wgs84Latitude);

	int getCounterApiCalls();

	void setCounterApiCalls(int counterApiCalls);

	URI getWgsRestApiCall();

	void setWgsRestApiCall(URI wgsRestApiCall);

	@Override String toString();
}
