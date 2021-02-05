package de.marcel.restaurant.ejb.model;

import javax.annotation.PostConstruct;
import javax.persistence.*;
import java.io.Serializable;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "restaurants")
@NamedQueries
				({
								@NamedQuery(name = "Restaurant.findAll", query = "SELECT u FROM Restaurant u")
				})
public class Restaurant extends BaseEntity implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "prim", nullable = false, columnDefinition = "INT")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer prim;

	@Column(name = "name", nullable = false, length = 70)
	private String name;

	@OneToOne(cascade = CascadeType.ALL)
	private Address addressRestaurant = new Address();

	@Column(name = "phoneNumber", nullable = true, length = 30)
	private String phoneNumber;
	@Column(name = "email", nullable = true, length = 100)
	private String email;


	@Column(name = "linkMenu", nullable = true, columnDefinition = "VARCHAR(500)")
	private String linkMenu;

	@Column(name = "openFrom", nullable = true, columnDefinition="TIME NULL")
	private LocalTime openFrom;
	@Column(name = "openTill", nullable = true, columnDefinition="TIME NULL")
	private LocalTime openTill;
	@Column(name = "holidayFrom", nullable = true, columnDefinition="TIMESTAMP NULL")
	private LocalDateTime holidayFrom;
	@Column(name = "holidayTill", nullable = true, columnDefinition="TIMESTAMP NULL")
	private LocalDateTime holidayTill;

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Column(name = "dayOfRest", nullable = true)
	private Set<DayOfWeek> daysOfRest;  // Impl Enum DayOfWeek

	@OneToOne
	private Culinary culinary;

	@Column(name = "averageRating", nullable = true, columnDefinition = "TINYINT")
	private byte averageRating;

	@OneToMany(mappedBy = "restaurantChosen")
	private Set<RestaurantVisit> visits;

	// Constructors
	public Restaurant(String name, Address a, Culinary culinary)
	{
		this.name = name;
		this.addressRestaurant = a;
		this.culinary = culinary;
	}

	@PostConstruct
	public void standardOffDays()
	{
		List<DayOfWeek> l = new ArrayList<>();
		l.add(DayOfWeek.MONDAY);
	}

	public Restaurant()
	{
	}

	// GETTER SETTER
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name.trim();
	}

	public Address getAddressRestaurant()
	{
		return addressRestaurant;
	}

	public void setAddressRestaurant(Address a)
	{
		this.addressRestaurant = a;
	}

	public String getPhoneNumber()
	{
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber)
	{
		this.phoneNumber = phoneNumber.trim();
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email.trim();
	}

	public String getLinkMenu()
	{
		return linkMenu;
	}

	public void setLinkMenu(String linkMenu)
	{
		this.linkMenu = linkMenu;
	}

	public LocalTime getOpenFrom()
	{
		return openFrom;
	}

	public void setOpenFrom(LocalTime openFrom)
	{
		this.openFrom = openFrom;
	}

	public LocalTime getOpenTill()
	{
		return openTill;
	}

	public void setOpenTill(LocalTime openTill)
	{
		this.openTill = openTill;
	}

	public LocalDateTime getHolidayFrom()
	{
		return holidayFrom;
	}

	public void setHolidayFrom(LocalDateTime holidayFrom)
	{
		this.holidayFrom = holidayFrom;
	}

	public LocalDateTime getHolidayTill()
	{
		return holidayTill;
	}

	public void setHolidayTill(LocalDateTime holidayTill)
	{
		this.holidayTill = holidayTill;
	}

	public Collection<DayOfWeek> getDaysOfRest()
	{
		return daysOfRest;
	}

	public void setDaysOfRest(DayOfWeek dayOfRest)
	{
		this.daysOfRest.add(dayOfRest);
	}

	public Culinary getCulinary()
	{
		return culinary;
	}

	public void setCulinary(Culinary culinary)
	{
		this.culinary = culinary;
	}

	public byte getAverageRating()
	{
		return averageRating;
	}

	public void setAverageRating(byte averageRating)
	{
		this.averageRating = averageRating;
	}

	public Set<RestaurantVisit> getVisits()
	{
		return visits;
	}

	public void setVisits(Set<RestaurantVisit> visits)
	{
		this.visits = visits;
	}

	public Integer getPrim()
	{
		return prim;
	}

	public void setPrim(Integer prim)
	{
		this.prim = prim;
	}
}
