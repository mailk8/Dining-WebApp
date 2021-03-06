package de.marcel.restaurant.ejb.model;

import de.marcel.restaurant.ejb.interfaces.IBaseEntity;

import javax.transaction.NotSupportedException;
import java.lang.reflect.Field;

public class BaseEntity implements IBaseEntity
{
	private static final long serialVersionUID = 1L;

	private Integer prim;

	private Integer id;

	//https://stackoverflow.com/questions/17343032/implement-converters-for-entities-with-java-generics/17343582#17343582
	@Override
	public int hashCode() {
		return (getPrim() != null)
						? (getClass().getSimpleName().hashCode() + getPrim().hashCode())
						: super.hashCode();
	}

	@Override
	public boolean equals(Object other) {

		// Vergleich der Datentypen der Felder des Objekts
		Field[] fieldsThis = this.getClass().getDeclaredFields();
		Field[] fieldsOther = other.getClass().getDeclaredFields();

		for (int i = 0; i < fieldsThis.length; i++)
		{
			if (!(fieldsThis[i].getType() == fieldsOther[i].getType()))
			{
				return false;
			}
		}

		// Vergleich von Referenztyp und Primärschlüssel
		return (other != null && getPrim() != null
						&& other.getClass().isAssignableFrom(getClass())
						&& getClass().isAssignableFrom(other.getClass()))
						? getPrim().equals(((BaseEntity) other).getPrim())
						: (other == this);
	}

	@Override public String toString()
	{
		return "BaseEntity{" + "prim=" + prim + ", id=" + id + " Class="+getClass().getSimpleName()+'}';
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

	@Override public void setId(Integer id) {
		this.id = id;
	}

}
