package de.marcel.restaurant.ejb;


import javax.annotation.ManagedBean;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ManagedBean
@Stateless
@LocalBean
public class RestaurantEJB implements Serializable
{
	private static final long serialVersionUID = 1L;

	@PersistenceContext
	private EntityManager entityManager;


	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <T> void persist(T t)
	{
		//Logger.getLogger(getClass().getSimpleName()).log(Level.WARNING, "+#+# Aufruf persist in ejb");
		entityManager.persist(t);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <T> void update(T t)
	{
		entityManager.merge(t);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <T> void delete(T t)
	{
		// Entity must be managed:
		// Because merge returns managed entity instance, you can call remove with the object it returns, because it is managed by JPA
		t = entityManager.merge(t);
		entityManager.remove(t);
	}

	public <T> List<T> findAll(Class entitiyClass)
	{
		TypedQuery<T> query = entityManager.createNamedQuery(entitiyClass.getSimpleName()+".findAll", entitiyClass);

		return query.getResultList();
	}


}
