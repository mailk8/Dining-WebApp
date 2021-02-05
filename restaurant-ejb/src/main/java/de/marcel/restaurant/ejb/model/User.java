package de.marcel.restaurant.ejb.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "users")
@NamedQueries(
				{
								@NamedQuery(name = "User.findAll", query = "SELECT u FROM User u")
				}
)
public class User extends BaseEntity implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "prim", nullable = false, columnDefinition = "INT")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer prim;

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

	public Address getAddressLiving()
	{
		return addressLiving;
	}

	public void setAddressLiving(Address addressLiving)
	{
		this.addressLiving = addressLiving;
	}

	public Address getAddressActual()
	{
		return addressActual;
	}

	public void setAddressActual(Address addressActual)
	{
		this.addressActual = addressActual;
	}

	public Culinary getCulinaryLiking()
	{
		return culinaryLiking;
	}

	public void setCulinaryLiking(Culinary culinaryLiking)
	{
		this.culinaryLiking = culinaryLiking;
	}

	public Set<Rating> getRatingsSubmitted()
	{
		return ratingsSubmitted;
	}

	public void setRatingsSubmitted(Set<Rating> ratingsSubmitted)
	{
		this.ratingsSubmitted = ratingsSubmitted;
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

	public String getFirstname()
	{
		return firstname;
	}

	public void setFirstname(String firstname)
	{
		this.firstname = firstname.trim();
	}

	public String getLastname()
	{
		return lastname;
	}

	public void setLastname(String lastname)
	{
		this.lastname = lastname.trim();
	}

	public Integer getPrim()
	{
		return prim;
	}

	public void setPrim(Integer prim)
	{
		this.prim = prim;
	}

	@Override public String toString()
	{
		return "User{" + "prim=" + prim + ", firstname='" + firstname + '\'' + ", lastname='" + lastname + '\'' + ", addressLiving=" + addressLiving + ", addressActual=" + addressActual + ", culinaryLiking=" + culinaryLiking + ", ratingsSubmitted=" + ratingsSubmitted + ", visitedRestaurants=" + visitedRestaurants + ", phoneNumber='" + phoneNumber + '\'' + ", email='" + email + '\'' + '}';
	}
}
