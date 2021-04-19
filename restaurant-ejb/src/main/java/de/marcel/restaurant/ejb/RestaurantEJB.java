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

	@Override
	public <T extends IBaseEntity> IBaseEntity findOneById(String id, Class<T> resultClazz) {
		TypedQuery<?> query = entityManager.createNamedQuery(resultClazz.getSimpleName()+".findOneById", resultClazz);
		query.setParameter("attribute", Integer.parseInt(id));
		IBaseEntity result = (IBaseEntity) query.getSingleResult();
		return result;
	}

	@Override
	public <T extends IBaseEntity> IBaseEntity findOneByPrim(Integer prim, Class<T> resultClazz, boolean withRefresh) {
		TypedQuery<?> query = entityManager.createNamedQuery(resultClazz.getSimpleName()+".findOneByPrim", resultClazz);
		query = query.setParameter("attribute", prim);
		if(withRefresh)
		{
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
		return t.getPrim();
	}

	@Override @TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <IBaseEntity> Integer update(de.marcel.restaurant.ejb.interfaces.IBaseEntity t) {
		try
		{
			// Mit merge meldet der EntityManager keinen Erfolg.
			// Jedes mal wird das Objekt zurückgegeben, egal ob es in die Tabelle gefügt wurde oder nicht!
			entityManager.merge(t);
			entityManager.flush();
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
		return result;
	}

	@Override
	public <T extends IBaseEntity> IBaseEntity findOne(Object attributeFromNamedQuery, Class attributeClazz, Class<T> resultClazz) {
		TypedQuery<?> query = entityManager.createNamedQuery(resultClazz.getSimpleName()+".findOne", resultClazz);
		query.setParameter(1, attributeClazz.cast(attributeFromNamedQuery));
		IBaseEntity result = (IBaseEntity) query.getSingleResult();
		return result;
	}

	@Override
	public <T extends IBaseEntity> Integer findMaxId(Class<T> resultClazz) {

		TypedQuery<?> query = entityManager.createNamedQuery(resultClazz.getSimpleName()+".findMaxId", resultClazz);
		Integer i = (Integer) query.getSingleResult();
		return i;
	}

	@Override
	public HashSet<Integer> findAllVisitsForUser(User participant) {

		TypedQuery<Integer> query = entityManager.createNamedQuery("RestaurantVisit.findAllVisitsForUser", Integer.class);
		query.setParameter(1, participant.getPrim());
		HashSet<Integer> mySet = query.getResultStream().collect(
			HashSet::new,
			HashSet::add,
			HashSet::addAll
		);
		return mySet;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public synchronized Integer persistCredentials(ICredentials cred) {
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

		Query q = entityManagerAuth.createNativeQuery("DELETE FROM users WHERE id_prod_db=?");

		q = q.setParameter(1, id_prod_db);
		int i = q.executeUpdate();

	}

	@Override public void clearCacheAllObjects() {
		entityManager.getEntityManagerFactory().getCache().evictAll();
	}

	@Override public void clearCache(Class clazz) {
		entityManager.getEntityManagerFactory().getCache().evict(clazz);
	}
}

