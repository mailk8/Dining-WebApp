package de.marcel.restaurant.ejb.model;

import de.marcel.restaurant.ejb.interfaces.ICulinary;
import javax.persistence.*;


@Entity
@Table(name = "culinaries")
@NamedQueries
				({
								@NamedQuery(name = "Culinary.findAll", query = "SELECT u FROM Culinary u"),
								@NamedQuery(name = "Culinary.findMaxId", query = "SELECT MAX(u.id) FROM Culinary u")
				})
public class Culinary extends BaseEntity implements ICulinary
{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "prim", nullable = false, columnDefinition = "INT")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer prim;

	@Column(name = "id", columnDefinition = "INT", unique = true)
	private Integer id;

	@Column(name = "category", nullable = false, length = 50)
	private String category;

	// Constructors
	public Culinary(){}

	// GETTER SETTER

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
		return category;
	}

}
