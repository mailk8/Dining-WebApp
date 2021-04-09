package de.marcel.restaurant.ejb.model;

import de.marcel.restaurant.ejb.interfaces.IRating;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.logging.Logger;

@Entity
@Table(name = "ratings")
@NamedQueries
				({
								@NamedQuery(name = "Rating.findAll", query = "SELECT u FROM Rating u"),
								@NamedQuery(name = "Rating.findMaxId", query = "SELECT MAX(u.id) FROM Rating u")
				})
public class Rating extends BaseEntity implements IRating
{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "prim", nullable = false, columnDefinition = "INT")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer prim;

	@Column(name = "id", columnDefinition = "INT", unique = true)
	private Integer id;

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
	//@Column(name = "dish", nullable = true)
	private	Dish dish;

	@Column(name = "dishMemo", nullable = true, length = 50)
	private	String	dishMemo;


	@ManyToOne
	//@JoinColumn(name="ratingsSubmitted")
	//@Column(name = "ratingUser", nullable = true)
	private	User ratingUser;

	@Column(name = "ratingDateTime", nullable = true)
	private LocalDate ratingDateTime;

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

	@Override public Restaurant getRestaurantRated()
	{
		return restaurantRated;
	}

	@Override public void setRestaurantRated(Restaurant restaurantRated)
	{
		this.restaurantRated = restaurantRated;
	}

	@Override public RestaurantVisit getVisit()
	{
		return visit;
	}

	@Override public void setVisit(RestaurantVisit visit)
	{
		this.visit = visit;
	}

	@Override public User getRatingUser()
	{
		return ratingUser;
	}

	@Override public void setRatingUser(User ratingUser)
	{
		this.ratingUser = ratingUser;
	}

	@Override public LocalDate getRatingDateTime()
	{
		return ratingDateTime;
	}

	@Override public void setRatingDateTime(LocalDate ratingDateTime)
	{
		this.ratingDateTime = ratingDateTime;
	}

	@Override public byte getStars()
	{
		return stars;
	}

	@Override public void setStars(byte stars)
	{
		this.stars = stars;
	}

	@Override public double getPrice()
	{
		return price;
	}

	@Override public void setPrice(double price)
	{
		Logger.getLogger(this.getClass().getSimpleName()).severe("+# setPrice mit " + price);
		this.price = price;
	}

	@Override public Dish getDish()
	{
		return dish;
	}

	@Override public void setDish(Dish dish)
	{
		Logger.getLogger(getClass().getSimpleName()).severe("+# setDish mit: " + dish);
		this.dish = dish;
	}

	@Override public String getDishMemo()
	{
		return dishMemo;
	}

	@Override public void setDishMemo(String dishDescription)
	{
		this.dishMemo = dishDescription.trim();
	}

}
