package de.marcel.restaurant.ejb.model;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "ratings")
@NamedQueries
				({
								@NamedQuery(name = "Rating.findAll", query = "SELECT u FROM User u")
				})
public class Rating extends BaseEntity implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "prim", nullable = false, columnDefinition = "INT")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer prim;



	@OneToOne
	//@JoinColumn(name="prim")
	//@Column(name = "restaurantRated", nullable = false)
	private	Restaurant restaurantRated;


	@ManyToOne
	//@JoinColumn(name="ratingsVisit")
	//@Column(name = "visit", nullable = false)
	private	RestaurantVisit visit;

	@Column(name = "stars", nullable = true, columnDefinition = "TINYINT")
	private	byte stars;
	@Column(name = "price", nullable = true, columnDefinition = "DOUBLE")
	private	double price;


	@OneToOne
	//@JoinColumn(name="prim")
	//@Column(name = "meal", nullable = true)
	private	Culinary meal;

	@Column(name = "mealDescription", nullable = true, length = 50)
	private	String	mealDescription;


	@ManyToOne
	//@JoinColumn(name="ratingsSubmitted")
	//@Column(name = "ratingUser", nullable = true)
	private	User ratingUser;

	@Column(name = "ratingDateTime", nullable = true)
	private LocalDateTime ratingDateTime;

	// CONSTRUCTORS

	public Rating(Restaurant rr, RestaurantVisit v)
	{
		this.restaurantRated = rr;
		this.visit = v;
	}

	public Rating(Restaurant rr)
	{
		this.restaurantRated = rr;
	}

	public Rating(RestaurantVisit v)
	{
		this.visit = v;
	}

	public Rating(User ru)
	{
		this.ratingUser = ru;
	}

	public Rating(Restaurant rr, RestaurantVisit v, User ru)
	{
		this.restaurantRated = rr;
		this.visit = v;
		this.ratingUser = ru;
	}

	public Rating(Restaurant rr, User ru)
	{
		this.restaurantRated = rr;
		this.ratingUser = ru;
	}

	public Rating(RestaurantVisit v, User ru)
	{
		this.visit = v;
		this.ratingUser = ru;
	}

	public Rating()
	{
	}

	// GETTER SETTER

	public Restaurant getRestaurantRated()
	{
		return restaurantRated;
	}

	public void setRestaurantRated(Restaurant restaurantRated)
	{
		this.restaurantRated = restaurantRated;
	}

	public RestaurantVisit getVisit()
	{
		return visit;
	}

	public void setVisit(RestaurantVisit visit)
	{
		this.visit = visit;
	}

	public User getRatingUser()
	{
		return ratingUser;
	}

	public void setRatingUser(User ratingUser)
	{
		this.ratingUser = ratingUser;
	}

	public LocalDateTime getRatingDateTime()
	{
		return ratingDateTime;
	}

	public void setRatingDateTime(LocalDateTime ratingDateTime)
	{
		this.ratingDateTime = ratingDateTime;
	}

	public byte getStars()
	{
		return stars;
	}

	public void setStars(byte stars)
	{
		this.stars = stars;
	}

	public double getPrice()
	{
		return price;
	}

	public void setPrice(double price)
	{
		this.price = price;
	}

	public Culinary getMeal()
	{
		return meal;
	}

	public void setMeal(Culinary meal)
	{
		this.meal = meal;
	}

	public String getMealDescription()
	{
		return mealDescription;
	}

	public void setMealDescription(String mealDescription)
	{
		this.mealDescription = mealDescription.trim();
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
