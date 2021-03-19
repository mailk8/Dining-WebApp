package de.marcel.restaurant.ejb.model;

import de.marcel.restaurant.ejb.interfaces.IAddress;

import javax.persistence.*;
import java.net.URI;

@Entity
@Table(name = "addresses")
@NamedQueries
				({
								@NamedQuery(name = "Address.findAll", query = "SELECT u FROM Address u"),
								@NamedQuery(name = "Address.findMaxId", query = "SELECT MAX(u.id) FROM Address u")
				})
public class Address extends BaseEntity implements IAddress
{
	private static final long serialVersionUID = 1L;

	@Transient private int counterApiCalls;
	@Transient private URI wgsRestApiCall;

	@Id
	@Column(name = "prim", nullable = false, columnDefinition = "INT")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer prim;

	@Column(name = "id", columnDefinition = "INT", unique = true)
	private Integer id;

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

	@Override public String getStreet()
	{
		return street;
	}

	@Override public void setStreet(String street)
	{
		this.street = street.trim();
	}

	@Override public String getHouseNumber()
	{
		return houseNumber;
	}

	@Override public void setHouseNumber(String houseNumber)
	{
		this.houseNumber = houseNumber.trim();
	}

	@Override public String getZipCode()
	{
		return zipCode;
	}

	@Override public void setZipCode(String zipCode)
	{
		this.zipCode = zipCode.trim();
	}

	@Override public String getCity()
	{
		return city;
	}

	@Override public void setCity(String city)
	{
		this.city = city.trim();
	}

	@Override public String getGooglePlace()
	{
		return googlePlace;
	}

	@Override public void setGooglePlace(String googlePlace)
	{
		this.googlePlace = googlePlace.trim();
	}

	@Override public Double getWgs84Latitude()
	{
		return wgs84Latitude;
	}

	@Override public void setWgs84Latitude(Double wgs84Latitude)
	{
		this.wgs84Latitude = wgs84Latitude;
	}

	@Override public Double getWgs84Longitude()
	{
		return wgs84Longitude;
	}

	@Override public void setWgs84Longitude(Double wgs84Longitude)
	{
		this.wgs84Longitude = wgs84Longitude;
	}

	@Override public int getCounterApiCalls()
	{
		return counterApiCalls;
	}

	@Override public void setCounterApiCalls(int counterApiCalls)
	{
		this.counterApiCalls = counterApiCalls;
	}

	@Override public URI getWgsRestApiCall()
	{
		return wgsRestApiCall;
	}

	@Override public void setWgsRestApiCall(URI wgsRestApiCall)
	{
		this.wgsRestApiCall = wgsRestApiCall;
	}

	@Override public String toString()
	{
		return "Address{" + "counterApiCalls=" + counterApiCalls + ", wgsRestApiCall=" + wgsRestApiCall + ", prim=" + prim + ", Id=" + id + ", street='" + street + '\'' + ", houseNumber='" + houseNumber + '\'' + ", zipCode='" + zipCode + '\'' + ", city='" + city + '\'' + ", googlePlace='" + googlePlace + '\'' + ", wgs84Latitude=" + wgs84Latitude + ", wgs84Longitude=" + wgs84Longitude + '}';
	}
}
