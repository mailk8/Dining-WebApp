package de.marcel.restaurant.ejb;


import de.marcel.restaurant.ejb.interfaces.IBaseEntity;
import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import de.marcel.restaurant.web.security.ICredentials;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.logging.Logger;

@Stateless
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

		// Persist takes an entity instance, adds it to the context and makes that instance managed (ie future updates to the entity will be tracked).

		entityManager.persist(t);

		entityManager.flush();
		Logger.getLogger(getClass().getSimpleName()).severe("+# nach persist und flush. id des Objekts ist "  + t.getPrim());
		return t.getPrim();
	}

	@Override @TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <IBaseEntity> Integer update(de.marcel.restaurant.ejb.interfaces.IBaseEntity t)
	{
		try
		{
			entityManager.merge(t);
			entityManager.flush();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return -1;
		}

		return 2;
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
	//public synchronized Integer persistCredentials(de.marcel.restaurant.web.security.ICredentials cred) {
	public synchronized Integer persistCredentials(String email, String pass, String salt, String id) {
		Logger.getLogger(getClass().getSimpleName()).severe("+# persistCredentials aufgerufen, Parameterübergabe geht klar. Vor Cast." );
		Query q = null;
		int result = 0;
		try
		{
			q = entityManagerAuth.createNativeQuery("INSERT INTO users (email, id_prod_db, password, salt) VALUES(?1, ?2, ?3, ?4) ON DUPLICATE KEY UPDATE email=?1, password=?3, salt=?4;");
			q = q.setParameter(1, email);
			q = q.setParameter(2, id);
			q = q.setParameter(3, pass);
			q = q.setParameter(4, salt);
			result = q.executeUpdate();
			Logger.getLogger(getClass().getSimpleName()).severe("+# persist credentials ergab eine Änderung von  " + result + " Elementen mit email " + email );
			//cred = null; cred = null;
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
	//public synchronized Integer persistCredentials(de.marcel.restaurant.web.security.ICredentials cred) {
	public synchronized Integer persistCredentials(ICredentials cred) {
		Logger.getLogger(getClass().getSimpleName()).severe("+# persistCredentials aufgerufen, Parameterübergabe geht klar. Vor Cast." );
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
			Logger.getLogger(getClass().getSimpleName()).severe("+# persist credentials ergab eine Änderung von  " + result + " Elementen mit email " + cred.getEmail() );
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
			Logger.getLogger(getClass().getSimpleName()).severe("+# persist Email ergab eine Änderung von  " + result + " Elementen mit email " + email );
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

		Logger.getLogger(getClass().getSimpleName()).severe("+# deleteCredentials ergab eine Änderung von  " + i + " Elementen " + this);
	}


}

