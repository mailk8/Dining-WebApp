package de.marcel.restaurant.ejb;


import de.marcel.restaurant.ejb.interfaces.IBaseEntity;
import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import de.marcel.restaurant.ejb.model.User;
import de.marcel.restaurant.web.security.ICredentials;
import org.eclipse.persistence.config.CascadePolicy;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.ejb.*;
import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

@Startup()
@Priority(1)
@Stateful
@Remote(IRestaurantEJB.class)
public class RestaurantEJB implements IRestaurantEJB
{
	private static final long serialVersionUID = 1L;

	@PostConstruct
	void infoCache() {
		Logger.getLogger(getClass().getSimpleName()).severe("+# @PostConstruct: EntityManager benutzt Cache: " + entityManager.getEntityManagerFactory().getCache());
	}

	@PersistenceContext(unitName="restaurant_ejb")
	private transient EntityManager entityManager;

	@PersistenceContext(unitName="restaurant_auth")
	private transient EntityManager entityManagerAuth;
//#############################################################

	@Override
	public <T extends IBaseEntity> IBaseEntity findOneById(String id, Class<T> resultClazz) {
		TypedQuery<?> query = entityManager.createNamedQuery(resultClazz.getSimpleName()+".findOneById", resultClazz);
		query.setParameter("attribute", Integer.parseInt(id));
		IBaseEntity result = (IBaseEntity) query.getSingleResult();
		Logger.getLogger(getClass().getSimpleName()).severe("+# nach findOneById für " + resultClazz.getSimpleName());
		return result;
	}

	@Override
	public <T extends IBaseEntity> IBaseEntity findOneByPrim(Integer prim, Class<T> resultClazz, boolean withRefresh) {
		Logger.getLogger(getClass().getSimpleName()).severe("+# vor refreshOneByPrim für " + resultClazz.getSimpleName());
		TypedQuery<?> query = entityManager.createNamedQuery(resultClazz.getSimpleName()+".findOneByPrim", resultClazz);
		query = query.setParameter("attribute", prim);
		if(withRefresh)
		{
			Logger.getLogger(getClass().getSimpleName()).severe("+# EM Cache wird gelöscht");
			invalidateCachesOne(resultClazz, prim);
			// QueryHints.REFRESH_CASCADE = eclipselink.refresh.cascade -> Implementierungspezifisch
			// https://www.eclipse.org/eclipselink/documentation/2.4/jpa/extensions/q_refresh_cache.htm
			query = query.setHint("eclipselink.refresh.cascade", CascadePolicy.CascadeAllParts);
		}

		return (IBaseEntity) query.getSingleResult();
	}

	private void invalidateCachesOne(Class clazz, Object primKey)
	{
		// Löscht EntityManager Level2 Cache für eine Instanz
		entityManager.getEntityManagerFactory().getCache().evict(clazz, primKey);
	}
//#############################################################
	@Override @TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <IBaseEntity> Integer persist(de.marcel.restaurant.ejb.interfaces.IBaseEntity t) {
		try
		{
			// Persist takes an entity instance, adds it to the context and makes that instance managed (ie future updates to the entity will be tracked).
			entityManager.persist(t);
			entityManager.flush();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return -1;
		}
		Logger.getLogger(getClass().getSimpleName()).severe("+# nach persist ");
		return t.getPrim();
	}

	@Override @TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <IBaseEntity> Integer update(de.marcel.restaurant.ejb.interfaces.IBaseEntity t) {
		try
		{
			// Mit merge meldet der EntityManager keinen Erfolg.
			// Jedes mal wird das Objekt zurückgegeben, egal ob es in die Tabelle gefügt wurde oder nicht!
			entityManager.merge(t);
			//Logger.getLogger(getClass().getSimpleName()).severe("+# nach merge. Objekt ist " + t + " id ist " + t.getId());
			entityManager.flush();
			Logger.getLogger(getClass().getSimpleName()).severe("+# nach merge (UPDATE) und flush. id des persistierten Objekts ist "  + t.getId() + " appServer Objekt " + this);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return -1;
		}

		return 3;
	}

	@Override @TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <IBaseEntity> Integer delete(de.marcel.restaurant.ejb.interfaces.IBaseEntity t) {
		// Because merge returns managed entity instance, you can call remove with the object it returns, because it is managed by JPA
		// Retourniert eine Kopie der Entity, diese wird durch die Kopie zur JPA-Managed Bean
		try
		{
			t = entityManager.merge(t);
			entityManager.remove(t);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return -1;
		}
		return 1;
	}

	@Override public List<IBaseEntity> findAll(Class entitiyClass) {
		TypedQuery<?> query = entityManager.createNamedQuery(entitiyClass.getSimpleName()+".findAll", entitiyClass);
		List<IBaseEntity> result = (List<IBaseEntity>) query.getResultList();
		Logger.getLogger(getClass().getSimpleName()).severe("+# nach findAll für " + entitiyClass.getSimpleName() );
		return result;
	}

	@Override
	public <T extends IBaseEntity> IBaseEntity findOne(Object attributeFromNamedQuery, Class attributeClazz, Class<T> resultClazz) {
		TypedQuery<?> query = entityManager.createNamedQuery(resultClazz.getSimpleName()+".findOne", resultClazz);
		query.setParameter(1, attributeClazz.cast(attributeFromNamedQuery));
		IBaseEntity result = (IBaseEntity) query.getSingleResult();
		Logger.getLogger(getClass().getSimpleName()).severe("+# nach findOne für " + resultClazz.getSimpleName());
		return result;
	}

	@Override
	public <T extends IBaseEntity> Integer findMaxId(Class<T> resultClazz) {

		TypedQuery<?> query = entityManager.createNamedQuery(resultClazz.getSimpleName()+".findMaxId", resultClazz);
		Integer i = (Integer) query.getSingleResult();
		Logger.getLogger(getClass().getSimpleName()).severe("+# nach findMaxId für " + resultClazz.getSimpleName());
		return i;
	}

	@Override
	public HashSet<Integer> findAllVisitsForUser(User participant) {
		//Logger.getLogger(getClass().getSimpleName()).severe("+# findAllVisitsForUser aufgerufen");

		TypedQuery<Integer> query = entityManager.createNamedQuery("RestaurantVisit.findAllVisitsForUser", Integer.class);
		query.setParameter(1, participant.getPrim());
		HashSet<Integer> mySet = query.getResultStream().collect(
			HashSet::new,
			HashSet::add,
			HashSet::addAll
		);

		Logger.getLogger(getClass().getSimpleName()).severe("+# findAllVisitsForUser");

		return mySet;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public synchronized Integer persistCredentials(ICredentials cred) {
		//Logger.getLogger(getClass().getSimpleName()).severe("+# persistCredentials aufgerufen, Parameterübergabe geht klar. Vor Cast." + " appServer Objekt " + this);
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
			//Logger.getLogger(getClass().getSimpleName()).severe("+# persist credentials ergab eine Änderung von  " + result + " Elementen mit email " + cred.getEmail() + " appServer Objekt " + this);
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
			//Logger.getLogger(getClass().getSimpleName()).severe("+# persist Email ergab eine Änderung von  " + result + " Elementen mit email " + email + " appServer Objekt " + this);
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
		//Logger.getLogger(getClass().getSimpleName()).severe("+# deleteCredentials mit id " + id_prod_db + "  " + this);

		Query q = entityManagerAuth.createNativeQuery("DELETE FROM users WHERE id_prod_db=?");

		q = q.setParameter(1, id_prod_db);
		int i = q.executeUpdate();

		//Logger.getLogger(getClass().getSimpleName()).severe("+# deleteCredentials ergab eine Änderung von  " + i + " Elementen " + " appServer Objekt " + this);
	}

	@Override public void clearCacheAllObjects() {
		entityManager.getEntityManagerFactory().getCache().evictAll();
	}

	@Override public void clearCache(Class clazz) {
		entityManager.getEntityManagerFactory().getCache().evict(clazz);
	}
}

