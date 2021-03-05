package de.marcel.restaurant.ejb.interfaces;

import javax.annotation.ManagedBean;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.io.Serializable;
import java.util.List;


@Remote(IRestaurantEJB.class)
public interface IRestaurantEJB extends Serializable
{
	@TransactionAttribute(TransactionAttributeType.REQUIRED) <T> void persist(T t);

	@TransactionAttribute(TransactionAttributeType.REQUIRED) <T> void update(T t);

	@TransactionAttribute(TransactionAttributeType.REQUIRED) <T> void delete(T t);

	@TransactionAttribute(TransactionAttributeType.REQUIRED) void persistCredentials(String email, String pass, String salt);

	<T> List<T> findAll(Class entitiyClass);

	@TransactionAttribute(TransactionAttributeType.REQUIRED) void persistEmail(String oldValue, String newValue);
}
