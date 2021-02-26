package de.marcel.restaurant.ejb.interfaces;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.io.Serializable;
import java.util.List;

@Local
public interface IRestaurantEJB extends Serializable
{
	@TransactionAttribute(TransactionAttributeType.REQUIRED) <T> void persist(T t);

	@TransactionAttribute(TransactionAttributeType.REQUIRED) <T> void update(T t);

	@TransactionAttribute(TransactionAttributeType.REQUIRED) <T> void delete(T t);

	<T> List<T> findAll(Class entitiyClass);
}
