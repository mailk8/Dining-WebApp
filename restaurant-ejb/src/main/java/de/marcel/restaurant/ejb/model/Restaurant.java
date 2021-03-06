package de.marcel.restaurant.ejb.model;

import de.marcel.restaurant.ejb.interfaces.IRestaurant;

import javax.annotation.PostConstruct;
import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Entity
@Table(name = "restaurants")
@NamedQueries
				({
								@NamedQuery(name = "Restaurant.findAll", query = "SELECT u FROM Restaurant u"),
								@NamedQuery(name = "Restaurant.findMaxId", query = "SELECT MAX(u.id) FROM Restaurant u"),
								@NamedQuery(name = "Restaurant.findOneById", query = "SELECT u FROM Restaurant u WHERE u.id = :attribute"),
								@NamedQuery(name = "Restaurant.findOneByPrim", query = "SELECT u FROM Restaurant u WHERE u.prim = :attribute")
				})
public class Restaurant extends BaseEntity implements IRestaurant
{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "prim", nullable = false, columnDefinition = "INT")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer prim;
	
	@Column(name = "id", columnDefinition = "INT", unique = true)
	private Integer id;

	@Column(name = "name", nullable = false, length = 70)
	private String name;

	@OneToOne(cascade = CascadeType.ALL)//, fetch = FetchType.EAGER)
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
	private LocalDate holidayFrom;
	
	@Column(name = "holidayTill", nullable = true, columnDefinition="TIMESTAMP NULL")
	private LocalDate holidayTill;
	
	@Lob @Basic//(fetch=FetchType.EAGER)
	@Column(name = "dayOfRest", nullable = true)
	private Set<DayOfWeek> daysOfRest;  // Impl Enum DayOfWeek

	@OneToOne
	private Culinary culinary;

	@Column(name = "avgRating", nullable = true, columnDefinition = "FLOAT")
	private float avgRating;
	
	@OneToMany(mappedBy = "restaurantChosen", fetch = FetchType.EAGER)
	private Set<RestaurantVisit> visits;

	@OneToMany(mappedBy = "restaurantRated", fetch = FetchType.EAGER)
	private Set<Rating> ratings = new HashSet<Rating>();

	@Transient
	private double distanceMeetingPoint, distanceUser;

	// Constructors
	public Restaurant(String name, Address a, Culinary culinary)
	{
		this.name = name;
		this.addressRestaurant = a;
		this.culinary = culinary;
	}

	public Restaurant()
	{
	}



	// GETTER SETTER
	@Override public String getName()
	{
		return name;
	}

	@Override public void setName(String name)
	{
		this.name = name.trim();
	}

	@Override public Address getAddressRestaurant()
	{
		return addressRestaurant;
	}

	@Override public void setAddressRestaurant(Address a)
	{
		this.addressRestaurant = a;
	}

	@Override public String getPhoneNumber()
	{
		return phoneNumber;
	}

	@Override public void setPhoneNumber(String phoneNumber)
	{
		this.phoneNumber = phoneNumber.trim();
	}

	@Override public String getEmail()
	{
		return email;
	}

	@Override public void setEmail(String email)
	{
		this.email = email.trim();
	}

	@Override public String getLinkMenu()
	{
		return linkMenu;
	}

	@Override public void setLinkMenu(String linkMenu)
	{
		this.linkMenu = linkMenu;
	}

	@Override public LocalTime getOpenFrom()
	{
		return openFrom;
	}

	@Override public void setOpenFrom(LocalTime openFrom)
	{
		this.openFrom = openFrom;
	}

	@Override public LocalTime getOpenTill()
	{
		return openTill;
	}

	@Override public void setOpenTill(LocalTime openTill)
	{
		this.openTill = openTill;
	}

	@Override public LocalDate getHolidayFrom()
	{
		return holidayFrom;
	}

	@Override public void setHolidayFrom(LocalDate holidayFrom)
	{
		this.holidayFrom = holidayFrom;
	}

	@Override public LocalDate getHolidayTill()
	{
		return holidayTill;
	}

	@Override public void setHolidayTill(LocalDate holidayTill)
	{
		this.holidayTill = holidayTill;
	}

	@Override public Collection<DayOfWeek> getDaysOfRest()
	{
		return daysOfRest;
	}

	@Override public void setDaysOfRest(DayOfWeek dayOfRest)
	{
		this.daysOfRest.add(dayOfRest);
	}

	@Override public Culinary getCulinary()
	{
		return culinary;
	}

	@Override public void setCulinary(Culinary culinary)
	{
		this.culinary = culinary;
	}

	@Override public float getAvgRating()
	{
		return avgRating;
	}

	@Override public void setAvgRating(float avgRating)
	{
		this.avgRating = avgRating;
	}

	@Override public Set<Rating> getRatings()
	{
		return ratings;
	}

	@Override public void setRatings(Set<Rating> ratings)
	{
		this.ratings = ratings;
	}

	@Override public Set<RestaurantVisit> getVisits()
	{
		return visits;
	}

	@Override public void setVisits(Set<RestaurantVisit> visits)
	{
		this.visits = visits;
	}

	@Override public Integer getPrim()
	{
		return prim;
	}

	@Override public void setPrim(Integer prim)
	{
		this.prim = prim;
	}

	@Override public Integer getId()
	{
		return id;
	}

	@Override public void setId(Integer id)
	{
		this.id = id;
	}

	@Override public double getDistanceMeetingPoint()
	{
		return distanceMeetingPoint;
	}

	@Override public void setDistanceMeetingPoint(double distanceMeetingPoint)
	{
		this.distanceMeetingPoint = distanceMeetingPoint;
	}

	@Override public double getDistanceUser()
	{
		return distanceUser;
	}

	@Override public void setDistanceUser(double distanceUser)
	{
		this.distanceUser = distanceUser;
	}

	@Override public String toString()
	{
		return "Restaurant{" + "prim=" + prim + ", id=" + id + ", name='" + name + '\'' + ", addressRestaurant=" + addressRestaurant + ", phoneNumber='" + phoneNumber + '\'' + ", email='" + email + '\'' + ", linkMenu='" + linkMenu + '\'' + ", openFrom=" + openFrom + ", openTill=" + openTill + ", holidayFrom=" + holidayFrom + ", holidayTill=" + holidayTill + ", daysOfRest=" + daysOfRest + ", culinary=" + culinary + ", avgRating=" + avgRating + ", visits=" + visits + '}';
	}
}
