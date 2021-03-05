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
	@TransactionAttribute(TransactionAttributeType.REQUIRED) <IBaseEntity> Integer persist(de.marcel.restaurant.ejb.interfaces.IBaseEntity  t);

	@TransactionAttribute(TransactionAttributeType.REQUIRED) <IBaseEntity> Integer update(de.marcel.restaurant.ejb.interfaces.IBaseEntity t);

	@TransactionAttribute(TransactionAttributeType.REQUIRED) <IBaseEntity> void delete(de.marcel.restaurant.ejb.interfaces.IBaseEntity t);

	@TransactionAttribute(TransactionAttributeType.REQUIRED) void persistCredentials(Integer id_prod_db, String pass, String salt);

	@TransactionAttribute(TransactionAttributeType.REQUIRED) void persistEmail(Integer id_prod_db, String newEmail);

	<T> List<T> findAll(Class entitiyClass);
}
