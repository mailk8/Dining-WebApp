package de.marcel.restaurant.ejb.model;

import de.marcel.restaurant.ejb.interfaces.IUser;

import javax.persistence.*;
import java.util.Set;
import java.util.logging.Logger;

@Entity
@Table(name = "users")
@NamedQueries(
				{
								@NamedQuery(name = "User.findAll", query = "SELECT u FROM User u"),
								@NamedQuery(name = "User.findOne", query = "SELECT u FROM User u WHERE u.email = ?1"),
								@NamedQuery(name = "User.findOneById", query = "SELECT u FROM User u WHERE u.id = :attribute"),
								@NamedQuery(name = "User.findMaxId", query = "SELECT MAX(u.id) FROM User u")
				}
)
public class User extends BaseEntity implements IUser
{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "prim", nullable = false, columnDefinition = "INT")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer prim;

	@Column(name = "id", columnDefinition = "INT", unique = true)
	private Integer id;

	@Column(name = "firstname", nullable = true, length = 20)
	private String firstname;

	@Column(name = "lastname", nullable = true, length = 20)
	private String lastname;

	@OneToOne(cascade = CascadeType.ALL)
	private Address addressLiving = new Address();

	@OneToOne(cascade = CascadeType.ALL)
	private Address addressActual = new Address();

	@OneToOne
	private Culinary culinaryLiking;

	@OneToMany(mappedBy = "ratingUser")
	private Set<Rating> ratingsSubmitted;

	@ManyToMany(mappedBy = "participants", cascade = CascadeType.MERGE)
	private Set<RestaurantVisit> visitedRestaurants;
	// https://stackoverflow.com/questions/21985308/how-is-the-owning-side-of-this-many-to-many-relationship-determined
	// https://en.wikibooks.org/wiki/Java_Persistence/ManyToMany


	@Column(name = "phoneNumber", nullable = true, length = 30)
	private String phoneNumber;

	@Column(name = "email", nullable = true, length = 100)
	private String email;

	// CONSTRUCTORS

	public User(Address addressLiving, Culinary culinaryLiking, String phoneNumber, String email)
	{
		this.addressLiving = addressLiving;
		this.culinaryLiking = culinaryLiking;
		this.phoneNumber = phoneNumber;
		this.email = email;
	}

	public User(Address addressLiving, Culinary culinaryLiking)
	{
		this.addressLiving = addressLiving;
		this.culinaryLiking = culinaryLiking;
	}

	public User(Address addressLiving)
	{
		this.addressLiving = addressLiving;
	}

	public User()
	{
	}

	// GETTER SETTERS

	@Override public Address getAddressLiving()
	{
		return addressLiving;
	}

	@Override public void setAddressLiving(Address addressLiving)
	{
		this.addressLiving = addressLiving;
	}

	@Override public Address getAddressActual()
	{
		return addressActual;
	}

	@Override public void setAddressActual(Address addressActual)
	{
		this.addressActual = addressActual;
	}

	@Override public Culinary getCulinaryLiking()
	{
		return culinaryLiking;
	}

	@Override public void setCulinaryLiking(Culinary culinaryLiking)
	{
		this.culinaryLiking = culinaryLiking;
	}

	@Override public Set<Rating> getRatingsSubmitted()
	{
		return ratingsSubmitted;
	}

	@Override public void setRatingsSubmitted(Set<Rating> ratingsSubmitted)
	{
		this.ratingsSubmitted = ratingsSubmitted;
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
		Logger.getLogger(getClass().getSimpleName()).severe("+# Entity User: setEmail aufgerufen mit " + email);
		this.email = email.trim();
	}

	@Override public String getFirstname()
	{
		return firstname;
	}

	@Override public void setFirstname(String firstname)
	{
		this.firstname = firstname.trim();
	}

	@Override public String getLastname()
	{
		return lastname;
	}

	@Override public void setLastname(String lastname)
	{
		this.lastname = lastname.trim();
	}

	@Override public String toString()
	{
		return "User{" + "prim=" + prim + "Id=" + id +", firstname='" + firstname + '\'' + ", lastname='" + lastname + '\'' + ", addressLiving=" + addressLiving + ", addressActual=" + addressActual + ", culinaryLiking=" + culinaryLiking + ", ratingsSubmitted=" + ratingsSubmitted + ", visitedRestaurants=" + visitedRestaurants + ", phoneNumber='" + phoneNumber + '\'' + ", email='" + email + '\'' + '}';
	}


}
