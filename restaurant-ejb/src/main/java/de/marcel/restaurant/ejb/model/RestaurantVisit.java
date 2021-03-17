package de.marcel.restaurant.ejb.model;

import de.marcel.restaurant.ejb.interfaces.IRestaurantVisit;

import javax.annotation.PostConstruct;
import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "restaurantVisit")
@NamedQueries
				({
								@NamedQuery(name = "RestaurantVisit.findAll", query = "SELECT u FROM RestaurantVisit u"),
								@NamedQuery(name = "RestaurantVisit.findAllForUser", query = "SELECT u FROM RestaurantVisit u WHERE u.restaurantChosen = :attribute"),
								@NamedQuery(name = "RestaurantVisit.findAllForRestaurant", query = "SELECT u FROM RestaurantVisit u WHERE u.restaurantChosen = :attribute")
				})
public class RestaurantVisit extends BaseEntity implements IRestaurantVisit
{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "prim", nullable = false, columnDefinition = "INT")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer prim;

	@Column(name = "id", columnDefinition = "INT", unique = true)
	private Integer id;

	@Column(name = "visitingDateTime", nullable = true)
	private ZonedDateTime visitingDateTime; // Dinner DateTime
	@Column(name = "memo", nullable = true, length = 200)
	private String memo;

	@ManyToMany(cascade = CascadeType.MERGE)
//	@JoinTable(
//					name="users_restaurantvisit",
//					joinColumns=@JoinColumn(name="participants_prim", referencedColumnName="prim"),
//					inverseJoinColumns=@JoinColumn(name="visitedRestaurants_prim", referencedColumnName="prim"))
	private List<User> participants = new ArrayList<>();

	@OneToOne(cascade = CascadeType.ALL)
	private Address addressVisit = new Address();

	@OneToMany(mappedBy="visit", cascade = CascadeType.ALL)
	private Set<Rating> ratingsVisit;

	@Column(name = "averageRating", nullable = true, columnDefinition = "TINYINT")
	private byte averageRating;

	@ManyToOne
	private Restaurant restaurantChosen;

	// In die Bean ?
	@Transient private Set<Restaurant> restaurantSuggestions;
	@Transient private Set<Restaurant> restaurantSearchHits;
	@Transient private Set<Culinary> culinaryMatchingBucket;

	//@OneToOne(cascade = CascadeType.ALL) // gar keinen Cascade Type, weil keine Kulinarik gelöscht oder geupdatet werden soll
	@OneToOne
	private Culinary chosenCulinary;


	@Column(name = "stateVisit", nullable = true, columnDefinition = "TINYINT")
	private State stateVisit;

	// CONSTRUCTORS

	public RestaurantVisit()
	{
		stateVisit = State.OBJEKT_ERZ;
		//Logger.getLogger(getClass().getSimpleName()).log(Level.WARNING, "+# RestaurantVisit Entity: Konsturktor läuft und setzt " + stateVisit);
	}

	@Override @PostConstruct
	public void initializeState()
	{
		stateVisit = State.OBJEKT_ERZ;
		//Logger.getLogger(getClass().getSimpleName()).log(Level.WARNING, "+# RestaurantVisit Entity: PostConstruct läuft und setzt " + stateVisit);
	}
	// GETTER SETTER

	@Override public ZonedDateTime getVisitingDateTime()
	{
		return visitingDateTime;
	}

	@Override public void setVisitingDateTime(ZonedDateTime visitingDateTime)
	{
		this.visitingDateTime = visitingDateTime;
	}

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

	@Override public String getParticipantsAsString()
	{
		StringBuffer s = new StringBuffer(participants.stream().map(e -> e.getFirstname()).collect(Collectors.joining(", ")));

		return s.toString().trim();
	}

	@Override public void setParticipants(List<User> participants)
	{
		this.participants = participants;
	}

	@Override public Set<Rating> getRatingsVisit()
	{
		return ratingsVisit;
	}

	@Override public void setRatingsVisit(Set<Rating> ratingsVisit)
	{
		this.ratingsVisit = ratingsVisit;
	}

	@Override public byte getAverageRating()
	{
		return averageRating;
	}

	@Override public void setAverageRating(byte averageRating)
	{
		this.averageRating = averageRating;
	}

	@Override public Restaurant getRestaurantChosen()
	{
		return restaurantChosen;
	}

	@Override public void setRestaurantChosen(Restaurant restaurantChosen)
	{
		this.restaurantChosen = restaurantChosen;
	}

	@Override public Set<Restaurant> getRestaurantSuggestions()
	{
		return restaurantSuggestions;
	}

	@Override public void setRestaurantSuggestions(Set<Restaurant> restaurantSuggestions)
	{
		this.restaurantSuggestions = restaurantSuggestions;
	}

	@Override public Set<Restaurant> getRestaurantSearchHits()
	{
		return restaurantSearchHits;
	}

	@Override public void setRestaurantSearchHits(Set<Restaurant> restaurantSearchHits)
	{
		this.restaurantSearchHits = restaurantSearchHits;
	}

	@Override public Enum getStateVisit()
	{
		return stateVisit;
	}

	@Override public void setStateVisit(State stateVisit)
	{
		this.stateVisit = stateVisit;
	}

	@Override public Set<Culinary> getCulinaryMatchingBucket()
	{
		return culinaryMatchingBucket;
	}

	@Override public void setCulinaryMatchingBucket(Set<Culinary> culinaryMatchingBucket)
	{
		this.culinaryMatchingBucket = culinaryMatchingBucket;
	}

	@Override public Culinary getChosenCulinary()
	{
		return chosenCulinary;
	}

	@Override public void setChosenCulinary(Culinary chosenCulinary)
	{
		this.chosenCulinary = chosenCulinary;
	}

	@Override public Address getAddressVisit()
	{
		return addressVisit;
	}

	@Override public void setAddressVisit(Address addressVisit)
	{
		this.addressVisit = addressVisit;
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
		return "RestaurantVisit{" + "prim=" + prim + ", id=" + id + ", visitingDateTime=" + visitingDateTime + ", memo='" + memo + '\'' + ", participants=" + participants + ", addressVisit=" + addressVisit + ", ratingsVisit=" + ratingsVisit + ", averageRating=" + averageRating + ", restaurantChosen=" + restaurantChosen + ", restaurantSuggestions=" + restaurantSuggestions + ", restaurantSearchHits=" + restaurantSearchHits + ", culinaryMatchingBucket=" + culinaryMatchingBucket + ", chosenCulinary=" + chosenCulinary + ", stateVisit=" + stateVisit + '}';
	}
}
