package de.marcel.restaurant.ejb;


import de.marcel.restaurant.ejb.interfaces.IBaseEntity;
import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.ejb.*;
import javax.faces.context.FacesContext;
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
	public <IBaseEntity> Integer persist(de.marcel.restaurant.ejb.interfaces.IBaseEntity t)
	{
		Logger.getLogger(getClass().getSimpleName()).severe("+# persist aufgerufen. PhaseId des Events ist " + FacesContext.getCurrentInstance().getCurrentPhaseId().getName());
		entityManager.persist(t);
		entityManager.flush();
		Logger.getLogger(getClass().getSimpleName()).severe("+# nach persist und flush. id des Objekts ist "  + t.getPrim());

		return t.getPrim();
	}

	@Override @TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <IBaseEntity> Integer update(de.marcel.restaurant.ejb.interfaces.IBaseEntity t)
	{
		entityManager.merge(t);
		entityManager.flush();
		return t.getPrim();
	}

	@Override @TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <IBaseEntity> void delete(de.marcel.restaurant.ejb.interfaces.IBaseEntity t)
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
	@Override public synchronized int persistCredentials(Integer id_prod_db, String pass, String salt)
	{
		Logger.getLogger(getClass().getSimpleName()).severe("+# persist credentials mit id " + id_prod_db + " pass " + pass + " salt " + salt);

		Query q = null;
		int result;
		try
		{
			q = entityManagerAuth.createNativeQuery("INSERT INTO users (id_prod_db, password, salt) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE password=?, salt=?;");
			q = q.setParameter(1, id_prod_db);
			q = q.setParameter(2, pass);
			q = q.setParameter(3, salt);
			q = q.setParameter(4, pass);
			q = q.setParameter(5, salt);
			result = q.executeUpdate();
			Logger.getLogger(getClass().getSimpleName()).severe("+# persist credentials ergab eine Änderung von  " + result + " Elementen");
			result = checkEntryComplete(id_prod_db);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return -1;
		}

		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Override public synchronized void deleteCredentials(Integer id_prod_db)
	{
		Logger.getLogger(getClass().getSimpleName()).severe("+# deleteCredentials mit id " + id_prod_db );

		Query q = entityManagerAuth.createNativeQuery("DELETE FROM users WHERE id_prod_db=?");

		q = q.setParameter(1, id_prod_db);
		int i = q.executeUpdate();

		Logger.getLogger(getClass().getSimpleName()).severe("+# deleteCredentials ergab eine Änderung von  " + i + " Elementen");
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Override public synchronized int persistEmail(Integer id_prod_db, String newEmail)
	{
		Logger.getLogger(getClass().getSimpleName()).severe("+# persist email läuft mit email " + newEmail);

		Query q = entityManagerAuth.createNativeQuery("INSERT INTO users (id_prod_db, email) VALUES(?, ?) ON DUPLICATE KEY UPDATE email=?;");

		int result;

		try{
		q = q.setParameter(1, id_prod_db);
		q = q.setParameter(2, newEmail);
		q = q.setParameter(3, newEmail);
		result = q.executeUpdate();
		Logger.getLogger(getClass().getSimpleName()).severe("+# persist email ergab eine Änderung von  " + result + " Elementen");
		result = checkEntryComplete(id_prod_db);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return -1;
		}

		return result;

	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	private synchronized int checkEntryComplete(int id_prod_db)
	{
		Query q = entityManagerAuth.createNativeQuery("SELECT * FROM users WHERE id_prod_db=?");
		q.setParameter(1, id_prod_db);
		List<Object[]> resultList = q.getResultList();

		if(resultList.size() != 1 || resultList.get(0).length != 4)
		{
			return -1;
		}

		for (Object s : resultList.get(0))
		{
			Logger.getLogger(getClass().getSimpleName()).severe("+# Eintrag Datensatz in DB " + s + " Result hat " + resultList.size() + " Elemente. Object[] size ist " + resultList.get(0).length);

			if(null == s || s.toString().isEmpty())
				return 0;
		}

		return 1;
	}




}
