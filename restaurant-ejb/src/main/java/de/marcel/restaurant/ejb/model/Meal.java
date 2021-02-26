package de.marcel.restaurant.ejb.model;

import de.marcel.restaurant.ejb.interfaces.IMeal;

import javax.persistence.*;

/*
Holds Meals.
To be filled by Database Table.
 */

@Entity
@Table(name = "meals")
@NamedQueries
				({
								@NamedQuery(name = "Meal.findAll", query = "SELECT u FROM Meal u")
				})
public class Meal extends BaseEntity implements IMeal
{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "prim", nullable = false, columnDefinition = "INT")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer prim;

	@ManyToOne
	private Culinary mealCategory;

	@Column(name = "mealName", nullable = true, length = 50)
	private String mealName;

	public Meal()
	{
	}

	public Meal(String s)
	{
		this.mealName = s;
	}

	@Override public Culinary getMealCategory()
	{
		return mealCategory;
	}

	@Override public void setMealCategory(Culinary mealCategory)
	{
		this.mealCategory = mealCategory;
	}

	@Override public String getMealName()
	{
		return mealName;
	}

	@Override public void setMealName(String mealName)
	{
		this.mealName = mealName.trim();
	}

	@Override public Integer getPrim()
	{
		return prim;
	}
	@Override public void setPrim(Integer prim)
	{
		this.prim = prim;
	}
}
