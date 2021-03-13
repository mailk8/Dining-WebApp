package de.marcel.restaurant.ejb;


import de.marcel.restaurant.ejb.interfaces.IBaseEntity;
import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import de.marcel.restaurant.web.security.ICredentials;

import javax.annotation.Priority;
import javax.ejb.*;
import javax.faces.context.FacesContext;
import javax.persistence.*;
import javax.transaction.UserTransaction;
import java.sql.Connection;
import java.util.List;
import java.util.logging.Logger;

@Startup()
@Priority(1)
@Stateful
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
		Logger.getLogger(getClass().getSimpleName()).severe("+# persist aufgerufen. PhaseId des Events ist " + FacesContext.getCurrentInstance().getCurrentPhaseId().getName()+ " appServer Objekt " + this);

		// Persist takes an entity instance, adds it to the context and makes that instance managed (ie future updates to the entity will be tracked).

		entityManager.persist(t);
////////////////////////////////// Test ////////////////////////////////////
//		javax.servlet.ServletException: javax.servlet.ServletException: javax.ejb.EJBException: java.lang.IllegalStateException:
//		Exception Description: Cannot use an EntityTransaction while using JTA.
//
		EntityTransaction transUser = entityManager.getTransaction();
		EntityTransaction transPassword = entityManagerAuth.getTransaction();
		transUser.commit();


///////////////////////////////////////////////////////////////////////////////////


		entityManager.flush();
		Logger.getLogger(getClass().getSimpleName()).severe("+# nach persist (INSERT) und flush. id des persistierten Objekts ist "  + t.getPrim() + " appServer Objekt " + this);
		return t.getPrim();
	}

	@Override @TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <IBaseEntity> Integer update(de.marcel.restaurant.ejb.interfaces.IBaseEntity t)
	{
		try
		{
			// Mit merge meldet der EntityManager keinen Erfolg.
			// Jedes mal wird das Objekt zurückgegeben, egal ob es in die Tabelle gefügt wurde oder nicht!
			entityManager.merge(t);
			Logger.getLogger(getClass().getSimpleName()).severe("+# nach merge. Objekt ist " + t + " prim ist " + t.getPrim());
			entityManager.flush();

			Logger.getLogger(getClass().getSimpleName()).severe("+# nach merge (UPDATE) und flush. id des persistierten Objekts ist "  + t.getPrim() + " appServer Objekt " + this);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return -1;
		}

		return 3;
	}

	@Override @TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <IBaseEntity> void delete(de.marcel.restaurant.ejb.interfaces.IBaseEntity t)
	{
		// Because merge returns managed entity instance, you can call remove with the object it returns, because it is managed by JPA
		// Retourniert eine Kopie der Entity, diese wird durch die Kopie zur JPA-Managed Bean
		t = entityManager.merge(t);
		entityManager.remove(t);
	}

	@Override public <IBaseEntity> List<IBaseEntity> findAll(Class entitiyClass)
	{
		TypedQuery<IBaseEntity> query = entityManager.createNamedQuery(entitiyClass.getSimpleName()+".findAll", entitiyClass);
		return query.getResultList();
	}

	@Override
	public <T extends IBaseEntity> IBaseEntity findOne(Object attributeFromNamedQuery, Class attributeClazz, Class<T> resultClazz)
	{
		TypedQuery<?> query = entityManager.createNamedQuery(resultClazz.getSimpleName()+".findOne", resultClazz);
		query.setParameter(1, attributeClazz.cast(attributeFromNamedQuery));

		return (IBaseEntity) query.getSingleResult();
	}

	@Override
	public <T extends IBaseEntity> IBaseEntity findOneByPrim(String prim, Class<T> resultClazz)
	{
		TypedQuery<?> query = entityManager.createNamedQuery(resultClazz.getSimpleName()+".findOneByPrim", resultClazz);
		query.setParameter("attribute", Integer.parseInt(prim));

		return (IBaseEntity) query.getSingleResult();
	}



	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public synchronized Integer persistCredentials(ICredentials cred) {
		Logger.getLogger(getClass().getSimpleName()).severe("+# persistCredentials aufgerufen, Parameterübergabe geht klar. Vor Cast." + " appServer Objekt " + this);
		Query q = null;
		int result = 0;
		try
		{
			q = entityManagerAuth.createNativeQuery("INSERT INTO users (email, id_prod_db, password, salt) VALUES(?1, ?2, ?3, ?4) ON DUPLICATE KEY UPDATE email=?1, password=?3, salt=?4;");
			q = q.setParameter(1, cred.getEmail());
			q = q.setParameter(2, cred.getId_prod_db().toString());
			q = q.setParameter(3, cred.getPassword());
			q = q.setParameter(4, cred.getSalt());
			result = q.executeUpdate();
			Logger.getLogger(getClass().getSimpleName()).severe("+# persist credentials ergab eine Änderung von  " + result + " Elementen mit email " + cred.getEmail() + " appServer Objekt " + this);
			cred = null; cred = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return -1;
		}

		if(result < 0)
			return -1;
		return 3;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public synchronized Integer persistEmail(String email, Integer id) {
		Query q = null;
		int result = 0;
		try
		{
			q = entityManagerAuth.createNativeQuery("UPDATE users SET email=?1 WHERE id_prod_db=?2");
			q = q.setParameter(1, email);
			q = q.setParameter(2, id);
			result = q.executeUpdate();
			Logger.getLogger(getClass().getSimpleName()).severe("+# persist Email ergab eine Änderung von  " + result + " Elementen mit email " + email + " appServer Objekt " + this);
			email = null; id = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return -1;
		}

		if(result < 0)
			return -1;
		return 1;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Override public synchronized void deleteCredentials(Integer id_prod_db) {
		Logger.getLogger(getClass().getSimpleName()).severe("+# deleteCredentials mit id " + id_prod_db + "  " + this);

		Query q = entityManagerAuth.createNativeQuery("DELETE FROM users WHERE id_prod_db=?");

		q = q.setParameter(1, id_prod_db);
		int i = q.executeUpdate();

		Logger.getLogger(getClass().getSimpleName()).severe("+# deleteCredentials ergab eine Änderung von  " + i + " Elementen " + " appServer Objekt " + this);
	}


}

