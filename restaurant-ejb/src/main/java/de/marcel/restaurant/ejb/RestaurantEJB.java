package de.marcel.restaurant.ejb;


import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;

import javax.annotation.ManagedBean;
import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

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

	@PersistenceContext
	private transient EntityManager entityManager;


	@Override @TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <T> void persist(T t)
	{
		//Logger.getLogger(getClass().getSimpleName()).log(Level.WARNING, "+#+# Aufruf persist in ejb");
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


}
