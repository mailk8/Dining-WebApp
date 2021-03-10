package de.marcel.restaurant.ejb.interfaces;

import de.marcel.restaurant.ejb.model.User;

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

	<T extends IBaseEntity> IBaseEntity findOne(Object characterisitcAttribute, Class attributeClazz, Class<T> resultClazz);

	<T extends IBaseEntity> IBaseEntity findOneByPrim(String prim, Class<T> resultClazz);

	void proxyPersistCredentials(Integer id, String pass, String salt);

	Integer proxyPersistUser(User u);

	@TransactionAttribute(TransactionAttributeType.REQUIRED) void deleteCredentials(Integer id_prod_db);

	<IBaseEntity> List<IBaseEntity> findAll(Class entitiyClass);

}
