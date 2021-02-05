package de.marcel.restaurant.ejb.model;

import javax.annotation.PostConstruct;
import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Entity
@Table(name = "restaurantVisit")
@NamedQueries
				({
								@NamedQuery(name = "RestaurantVisit.findAll", query = "SELECT u FROM RestaurantVisit u")
				})
public class RestaurantVisit extends BaseEntity implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "prim", nullable = false, columnDefinition = "INT")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer prim;

	@Column(name = "visitingDateTime", nullable = true)
	private LocalDateTime visitingDateTime; // Dinner DateTime
	@Column(name = "memo", nullable = true, length = 200)
	private String memo;

	@ManyToMany(cascade = CascadeType.MERGE)
//	@JoinTable(
//					name="users_restaurantvisit",
//					joinColumns=@JoinColumn(name="participants_prim", referencedColumnName="prim"),
//					inverseJoinColumns=@JoinColumn(name="visitedRestaurants_prim", referencedColumnName="prim"))
	private Set<User> participants = new HashSet<>();

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
		//Logger.getLogger(getClass().getSimpleName()).log(Level.WARNING, "+#+# RestaurantVisit Entity: Konsturktor läuft und setzt " + stateVisit);
	}

	@PostConstruct
	public void initializeState()
	{
		stateVisit = State.OBJEKT_ERZ;
		//Logger.getLogger(getClass().getSimpleName()).log(Level.WARNING, "+#+# RestaurantVisit Entity: PostConstruct läuft und setzt " + stateVisit);
	}
	// GETTER SETTER

	public LocalDateTime getVisitingDateTime()
	{
		return visitingDateTime;
	}

	public void setVisitingDateTime(LocalDateTime visitingDateTime)
	{
		this.visitingDateTime = visitingDateTime;
	}

	public String getMemo()
	{
		return memo;
	}

	public void setMemo(String memo)
	{
		this.memo = memo.trim();
	}

	public Set<User> getParticipants()
	{
		return participants;
	}

	public String getParticipantsAsString()
	{
		StringBuffer s = new StringBuffer(participants.stream().map(e -> e.getFirstname()).collect(Collectors.joining(", ")));

		return s.toString().trim();
	}

	public void setParticipants(Set<User> participants)
	{
		this.participants = participants;
	}

	public Set<Rating> getRatingsVisit()
	{
		return ratingsVisit;
	}

	public void setRatingsVisit(Set<Rating> ratingsVisit)
	{
		this.ratingsVisit = ratingsVisit;
	}

	public byte getAverageRating()
	{
		return averageRating;
	}

	public void setAverageRating(byte averageRating)
	{
		this.averageRating = averageRating;
	}

	public Restaurant getRestaurantChosen()
	{
		return restaurantChosen;
	}

	public void setRestaurantChosen(Restaurant restaurantChosen)
	{
		this.restaurantChosen = restaurantChosen;
	}

	public Set<Restaurant> getRestaurantSuggestions()
	{
		return restaurantSuggestions;
	}

	public void setRestaurantSuggestions(Set<Restaurant> restaurantSuggestions)
	{
		this.restaurantSuggestions = restaurantSuggestions;
	}

	public Set<Restaurant> getRestaurantSearchHits()
	{
		return restaurantSearchHits;
	}

	public void setRestaurantSearchHits(Set<Restaurant> restaurantSearchHits)
	{
		this.restaurantSearchHits = restaurantSearchHits;
	}

	public Enum getStateVisit()
	{
		return stateVisit;
	}

	public void setStateVisit(State stateVisit)
	{
		this.stateVisit = stateVisit;
	}

	public Integer getPrim()
	{
		return prim;
	}

	public void setPrim(Integer prim)
	{
		this.prim = prim;
	}

	public Set<Culinary> getCulinaryMatchingBucket()
	{
		return culinaryMatchingBucket;
	}

	public void setCulinaryMatchingBucket(Set<Culinary> culinaryMatchingBucket)
	{
		this.culinaryMatchingBucket = culinaryMatchingBucket;
	}

	public Culinary getChosenCulinary()
	{
		return chosenCulinary;
	}

	public void setChosenCulinary(Culinary chosenCulinary)
	{
		this.chosenCulinary = chosenCulinary;
	}

	public Address getAddressVisit()
	{
		return addressVisit;
	}

	public void setAddressVisit(Address addressVisit)
	{
		this.addressVisit = addressVisit;
	}


}
