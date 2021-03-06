package de.marcel.restaurant.ejb.interfaces;

//import de.marcel.restaurant.ejb.RestaurantEJB;
//import de.marcel.restaurant.web.security.ICredentials;
import de.marcel.restaurant.ejb.model.User;


import javax.annotation.ManagedBean;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;


@Remote(IRestaurantEJB.class)
public interface IRestaurantEJB extends Serializable
{
	<T extends IBaseEntity> IBaseEntity findOneByPrim(Integer prim, Class<T> resultClazz, boolean withRefresh);

	@TransactionAttribute(TransactionAttributeType.REQUIRED) <IBaseEntity> Integer persist(de.marcel.restaurant.ejb.interfaces.IBaseEntity  t);

	@TransactionAttribute(TransactionAttributeType.REQUIRED) <IBaseEntity> Integer update(de.marcel.restaurant.ejb.interfaces.IBaseEntity t);

	@TransactionAttribute(TransactionAttributeType.REQUIRED) <IBaseEntity> Integer delete(de.marcel.restaurant.ejb.interfaces.IBaseEntity t);

	HashSet<Integer> findAllVisitsForUser(User participant);

	@TransactionAttribute(TransactionAttributeType.REQUIRED) Integer persistCredentials(ICredentials ic);

	@TransactionAttribute(TransactionAttributeType.REQUIRED) Integer persistEmail(String email, Integer id);

	@TransactionAttribute(TransactionAttributeType.REQUIRED) void deleteCredentials(Integer id_prod_db);

	<T extends IBaseEntity> IBaseEntity findOne(Object characterisitcAttribute, Class attributeClazz, Class<T> resultClazz);

	<T extends IBaseEntity> IBaseEntity findOneById(String id, Class<T> resultClazz);

	<IBaseEntity> List<IBaseEntity> findAll(Class entitiyClass);

	<T extends IBaseEntity> Integer findMaxId(Class<T> resultClazz);

	void clearCacheAllObjects();

	void clearCache(Class clazz);
}
