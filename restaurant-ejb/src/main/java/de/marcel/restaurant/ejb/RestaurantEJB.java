package de.marcel.restaurant.ejb;


import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;

import javax.annotation.ManagedBean;
import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.logging.Logger;

//@ManagedBean
//@Stateless
//@LocalBean
//@Startup
@Stateful
@ManagedBean
@Remote(IRestaurantEJB.class)
public class RestaurantEJB implements IRestaurantEJB
{
	private static final long serialVersionUID = 1L;

	@PersistenceContext(unitName="restaurant_ejb")
	private transient EntityManager entityManager;

	@PersistenceContext(unitName="restaurant_auth")
	private transient EntityManager entityManagerAuth;


	@Override @TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <T> void persist(T t)
	{
		//Logger.getLogger(getClass().getSimpleName()).log(Level.WARNING, "+# Aufruf persist in ejb");
		entityManager.persist(t);
	}

	@Override @TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <T> void update(T t)
	{
		entityManager.merge(t);
	}

	@Override @TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <T> void delete(T t)
	{
		// Entity must be managed:
		// Because merge returns managed entity instance, you can call remove with the object it returns, because it is managed by JPA
		t = entityManager.merge(t);
		entityManager.remove(t);


	}

	@Override public <T> List<T> findAll(Class entitiyClass)
	{
		TypedQuery<T> query = entityManager.createNamedQuery(entitiyClass.getSimpleName()+".findAll", entitiyClass);

		return query.getResultList();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Override public void persistCredentials(String email, String pass, String salt)
	{
		Logger.getLogger(getClass().getSimpleName()).severe("+# persist credentials mit email" + email + " pass " + pass + " salt " + salt);

		//Query q = entityManagerAuth.createNativeQuery("INSERT INTO users VALUES(?, ?, ?)");
		Query q = entityManagerAuth.createNativeQuery("INSERT INTO users VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE password=?, salt=?;");

		q = q.setParameter(1, email);
		q = q.setParameter(2, pass);
		q = q.setParameter(3, salt);
		q = q.setParameter(4, pass);
		q = q.setParameter(5, salt);
		int i = q.executeUpdate();
		Logger.getLogger(getClass().getSimpleName()).severe("+# persist credentials mit ergab Änderungen " + i);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Override public void persistEmail(String oldValue, String newValue)
	{
		Query q = entityManagerAuth.createNativeQuery("UPDATE users SET email=? WHERE email=? ");

		Logger.getLogger(getClass().getSimpleName()).severe("+# EJB persist email aufgeruen mit oldValue" + oldValue + " new Value " + newValue);

		q = q.setParameter(1, newValue);
		q = q.setParameter(2, oldValue);
		q.executeUpdate();
	}


}
