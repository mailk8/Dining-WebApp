package de.marcel.restaurant.ejb.interfaces;

import de.marcel.restaurant.ejb.model.BaseEntity;

import java.io.Serializable;

public interface IBaseEntity extends Serializable
{
	//https://stackoverflow.com/questions/17343032/implement-converters-for-entities-with-java-generics/17343582#17343582
	@Override int hashCode();

	@Override boolean equals(Object other);

	@Override String toString();

	Integer getPrim();

	void setPrim(Integer prim);

	Integer getId();
	void setId(Integer id);

	float calculateAvgRating(BaseEntity dataObject);

	float calculateAvgRating();
}
