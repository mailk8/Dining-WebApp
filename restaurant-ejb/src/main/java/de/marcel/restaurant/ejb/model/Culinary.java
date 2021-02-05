package de.marcel.restaurant.ejb.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
/*
Holds Meal Categorys.
To be filled by Database Table.
 */

@Entity
@Table(name = "culinaries")
@NamedQueries
				({
								@NamedQuery(name = "Culinary.findAll", query = "SELECT u FROM Culinary u")
				})
public class Culinary extends BaseEntity implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "prim", nullable = false, columnDefinition = "INT")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer prim;

	@Column(name = "category", nullable = false, length = 50)
	private String category;

	@OneToMany(mappedBy = "mealCategory")
	private Set<Meal> meals = new HashSet<>();

	// Constructors
	public Culinary(){}

	// GETTER SETTER

	public Set<Meal> getMeals()
	{
		return meals;
	}

	public void setMeals(Set<Meal> meals)
	{
		this.meals = meals;
	}

	public String getCategory()
	{
		return category;
	}

	public void setCategory(String category)
	{
		this.category = category.trim();
	}

	public Integer getPrim()
	{
		return prim;
	}

	public void setPrim(Integer prim)
	{
		this.prim = prim;
	}

	public String toString()
	{
		return category;
	}

}
