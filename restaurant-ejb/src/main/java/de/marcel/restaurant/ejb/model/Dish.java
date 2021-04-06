package de.marcel.restaurant.ejb.model;

import de.marcel.restaurant.ejb.interfaces.IDish;
import javax.persistence.*;


@Entity
@Table(name = "dishes")
@NamedQueries
				({
								@NamedQuery(name = "Dish.findAll", query = "SELECT u FROM Dish u"),
								@NamedQuery(name = "Dish.findMaxId", query = "SELECT MAX(u.id) FROM Dish u")
				})
public class Dish extends BaseEntity implements IDish
{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "prim", nullable = false, columnDefinition = "INT")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer prim;

	@Column(name = "id", columnDefinition = "INT", unique = true)
	private Integer id;

	@Column(name = "dishName", nullable = true, length = 50)
	private String dishName;

	public Dish()
	{
	}

	public Dish(String s)
	{
		this.dishName = s;
	}

	@Override public String getDishName()
	{
		return dishName;
	}

	@Override public void setDishName(String dishName)
	{
		this.dishName = dishName.trim();
	}

	@Override public String toString()
	{
		return this.dishName;
	}
}
