package de.marcel.restaurant.ejb.model;

import de.marcel.restaurant.ejb.interfaces.ICulinary;

import javax.persistence.*;
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
public class Culinary extends BaseEntity implements ICulinary
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

	@Override public Set<Meal> getMeals()
	{
		return meals;
	}

	@Override public void setMeals(Set<Meal> meals)
	{
		this.meals = meals;
	}

	@Override public String getCategory()
	{
		return category;
	}

	@Override public void setCategory(String category)
	{
		this.category = category.trim();
	}

	@Override public Integer getPrim()
	{
		return prim;
	}

	@Override public void setPrim(Integer prim)
	{
		this.prim = prim;
	}

	@Override public String toString()
	{
		return category;
	}

}
