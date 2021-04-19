package de.marcel.restaurant.ejb.model;

import de.marcel.restaurant.ejb.interfaces.IRestaurantVisit;

import javax.persistence.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "restaurantVisit")
@NamedQueries({

				@NamedQuery(name = "RestaurantVisit.findAllForUser", query = "SELECT u FROM RestaurantVisit u WHERE u.restaurantChosen = :attribute"),
				@NamedQuery(name = "RestaurantVisit.findAllForRestaurant", query = "SELECT u FROM RestaurantVisit u WHERE u.restaurantChosen = :attribute"),
					
				@NamedQuery(name = "RestaurantVisit.findAll", query = "SELECT u FROM RestaurantVisit u"),
				@NamedQuery(name = "RestaurantVisit.findMaxId", query = "SELECT MAX(u.id) FROM RestaurantVisit u"),
				@NamedQuery(name = "RestaurantVisit.findOneById", query = "SELECT u FROM RestaurantVisit u WHERE u.id = :attribute"),
				@NamedQuery(name = "RestaurantVisit.findOneByPrim", query = "SELECT u FROM RestaurantVisit u WHERE u.prim = :attribute")
				})

@NamedNativeQueries({
		@NamedNativeQuery(name = "RestaurantVisit.findAllVisitsForUser", query = "SELECT visitedRestaurants_prim FROM restaurantvisit_users WHERE participants_prim = ?;")
})

public class RestaurantVisit extends BaseEntity implements IRestaurantVisit
{
	private static final long serialVersionUID = 1L;

	@Transient
	private static byte numberOfStars = 6;

	@Id
	@Column(name = "prim", nullable = false, columnDefinition = "INT")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer prim;

	@Column(name = "id", columnDefinition = "INT", unique = true)
	private Integer id;

	@Column(name = "visitingDate", nullable = true)
	private LocalDate visitingDate;

	@Column(name = "visitingTime", nullable = true)
	private LocalTime visitingTime;

	@Column(name = "timezoneString", columnDefinition = "VARCHAR(35)")
	private String timezoneString;

	@Column(name = "memo", nullable = true, length = 200)
	private String memo;

	@ManyToMany(cascade = CascadeType.DETACH, fetch = FetchType.EAGER)
	private List<User> participants = new ArrayList<>();

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Address addressVisit = new Address();

	@OneToMany(mappedBy="visit", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Set<Rating> ratings;

	@ManyToOne private Restaurant restaurantChosen;

	@ManyToMany(fetch = FetchType.EAGER)
	private List<Culinary> chosenCulinaries = new ArrayList<>();

	@Column(name = "stateVisit", nullable = true, columnDefinition = "TINYINT")
	private State stateVisit;

	// CONSTRUCTORS
	public RestaurantVisit()
	{
		stateVisit = State.UNVOLLSTÄNDIG;
	}



	// GETTER SETTER
	@Override public LocalDate getVisitingDate()
	{
		return visitingDate;
	}

	@Override public void setVisitingDate(LocalDate visitingDateTime)
	{
		this.visitingDate = visitingDateTime;
	}

	@Override public LocalDateTime getVisitingDateTime(){ return LocalDateTime.of(visitingDate, visitingTime);}

	@Override public String getMemo()
	{
		return memo;
	}

	@Override public void setMemo(String memo)
	{
		this.memo = memo.trim();
	}

	@Override public List<User> getParticipants()
	{
		return participants;
	}

	@Override public String getParticipantsAsString(User u)
	{
		List<User> list = participants;
		if(u != null)
		{
			list = new ArrayList<>();
			list.addAll(participants);
			list.remove(u);
		}
		StringBuffer s = new StringBuffer(list.stream().map(e -> e.getFirstname()).collect(Collectors.joining(", ")));

		return s.toString().trim();
	}

	@Override public void setParticipants(List<User> users)
	{
		this.participants = users;
	}

	public Set<Rating> getRatings()
	{
		return ratings;
	}

	public void setRatings(Set<Rating> ratings)
	{
		this.ratings = ratings;
	}

	@Override public Restaurant getRestaurantChosen()
	{
		return restaurantChosen;
	}

	@Override public void setRestaurantChosen(Restaurant restaurantChosen)
	{
		this.restaurantChosen = restaurantChosen;
	}

	@Override public Enum getStateVisit()
	{
		return stateVisit;
	}

	@Override public void setStateVisit(State stateVisit)
	{
		this.stateVisit = stateVisit;
	}

	@Override public List<Culinary> getChosenCulinaries()
	{
		return chosenCulinaries;
	}

	@Override public void setChosenCulinaries(List<Culinary> chosenCulinaries)
	{
		this.chosenCulinaries = chosenCulinaries;
	}

	@Override public Address getAddressVisit()
	{
		return addressVisit;
	}

	@Override public void setAddressVisit(Address addressVisit)
	{
		this.addressVisit = addressVisit;
	}

	@Override public LocalTime getVisitingTime()
	{
		return visitingTime;
	}

	@Override public void setVisitingTime(LocalTime visitingTime)
	{
		this.visitingTime = visitingTime;
	}

	@Override public String getTimezoneString()
	{
		return timezoneString;
	}

	@Override public void setTimezoneString(String timezone)
	{
		this.timezoneString = timezone;
	}

	@Override public ZonedDateTime getVisitingZonedDateTime() {
		return ZonedDateTime.of(visitingDate, visitingTime, ZoneId.of(timezoneString));
	}

	@Override public LocalDateTime getVisitingLocalDateTime() {
		return LocalDateTime.of(visitingDate, visitingTime);
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

	@Override public String toString()
	{
		return "RestaurantVisit{" + "prim=" + prim + ", id=" + id + ", visitingDateTime=" + visitingDate + ", memo='" + memo +  ", addressVisit=" + addressVisit + ", restaurantChosen=" + (null != restaurantChosen ? restaurantChosen.getLinkMenu() : "leer") + "ChosenCulinaries =" + chosenCulinaries + ", stateVisit=" + stateVisit + '}';
	}
}
