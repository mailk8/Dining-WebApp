package de.marcel.restaurant.ejb.interfaces;

import de.marcel.restaurant.ejb.RestaurantEJB;
import de.marcel.restaurant.ejb.model.User;
import de.marcel.restaurant.web.credentials.ICredentials;

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

	@TransactionAttribute(TransactionAttributeType.REQUIRED) Integer persistCredentials(de.marcel.restaurant.web.credentials.ICredentials cred);

	@TransactionAttribute(TransactionAttributeType.REQUIRED) Integer persistEmail(String email, Integer id);

	@TransactionAttribute(TransactionAttributeType.REQUIRED) void deleteCredentials(Integer id_prod_db);

	<T extends IBaseEntity> IBaseEntity findOne(Object characterisitcAttribute, Class attributeClazz, Class<T> resultClazz);

	<T extends IBaseEntity> IBaseEntity findOneByPrim(String prim, Class<T> resultClazz);

	<IBaseEntity> List<IBaseEntity> findAll(Class entitiyClass);

}
