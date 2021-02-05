package de.marcel.restaurant.ejb.model;

import javax.persistence.*;
import java.io.Serializable;
import java.net.URI;
import java.util.List;


@Entity
@Table(name = "addresses")
@NamedQueries
				({
								@NamedQuery(name = "Address.findAll", query = "SELECT u FROM Address u")
				})
public class Address extends BaseEntity implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Transient private int counterApiCalls;
	@Transient private URI wgsRestApiCall;

	@Id
	@Column(name = "prim", nullable = false, columnDefinition = "INT")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer prim;

	@Column(name = "street", nullable = true, length = 70)
	private String street;
	@Column(name = "houseNumber", nullable = true, length = 10)
	private String houseNumber;
	@Column(name = "zipCode", nullable = true, length = 10)
	private String zipCode;
	@Column(name = "city", nullable = true, length = 70)
	private String city;

	@Transient private String googlePlace;

	@Column(name = "wgs84Latitude", nullable = true, columnDefinition = "DOUBLE")
	private Double wgs84Latitude; // +North - South
	@Column(name = "wgs84Longitude", nullable = true, columnDefinition = "DOUBLE")
	private Double wgs84Longitude; // +East - West

	// Constructors

	public Address(String street, String houseNumber, String zipCode, String city, double wgs84Latitude, double wgs84Longitude)
	{
		this.street = street;
		this.houseNumber = houseNumber;
		this.zipCode = zipCode;
		this.city = city;
		this.wgs84Latitude = wgs84Latitude;
		this.wgs84Longitude = wgs84Longitude;
	}

	public Address(String street, String houseNumber, String zipCode, String city)
	{
		this.street = street;
		this.houseNumber = houseNumber;
		this.zipCode = zipCode;
		this.city = city;
	}

	public Address(double wgs84Latitude, double wgs84Longitude)
	{
		this.wgs84Latitude = wgs84Latitude;
		this.wgs84Longitude = wgs84Longitude;
	}

	public Address()
	{
		// NPE: insertInIndex(this);
	}

	// GETTER SETTER

	public String getStreet()
	{
		return street;
	}

	public void setStreet(String street)
	{
		this.street = street.trim();
	}

	public String getHouseNumber()
	{
		return houseNumber;
	}

	public void setHouseNumber(String houseNumber)
	{
		this.houseNumber = houseNumber.trim();
	}

	public String getZipCode()
	{
		return zipCode;
	}

	public void setZipCode(String zipCode)
	{
		this.zipCode = zipCode.trim();
	}

	public String getCity()
	{
		return city;
	}

	public void setCity(String city)
	{
		this.city = city.trim();
	}

	public String getGooglePlace()
	{
		return googlePlace;
	}

	public void setGooglePlace(String googlePlace)
	{
		this.googlePlace = googlePlace.trim();
	}

	public Double getWgs84Latitude()
	{
		return wgs84Latitude;
	}

	public void setWgs84Latitude(Double wgs84Latitude)
	{
		this.wgs84Latitude = wgs84Latitude;
	}

	public Double getWgs84Longitude()
	{
		return wgs84Longitude;
	}

	public void setWgs84Longitude(Double wgs84Longitude)
	{
		this.wgs84Longitude = wgs84Longitude;
	}

	public Integer getPrim()
	{
		return prim;
	}

	public void setPrim(Integer prim)
	{
		this.prim = prim;
	}

	public int getCounterApiCalls()
	{
		return counterApiCalls;
	}

	public void setCounterApiCalls(int counterApiCalls)
	{
		this.counterApiCalls = counterApiCalls;
	}

	public URI getWgsRestApiCall()
	{
		return wgsRestApiCall;
	}

	public void setWgsRestApiCall(URI wgsRestApiCall)
	{
		this.wgsRestApiCall = wgsRestApiCall;
	}

	@Override public String toString()
	{
		return "Address{" + "counterApiCalls=" + counterApiCalls + ", wgsRestApiCall=" + wgsRestApiCall + ", prim=" + prim + ", street='" + street + '\'' + ", houseNumber='" + houseNumber + '\'' + ", zipCode='" + zipCode + '\'' + ", city='" + city + '\'' + ", googlePlace='" + googlePlace + '\'' + ", wgs84Latitude=" + wgs84Latitude + ", wgs84Longitude=" + wgs84Longitude + '}';
	}
}
