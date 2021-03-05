package de.marcel.restaurant.ejb;


import de.marcel.restaurant.ejb.interfaces.IBaseEntity;
import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;

import javax.annotation.ManagedBean;
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
	@Override public void persistCredentials(Integer id_prod_db, String pass, String salt)
	{
		Logger.getLogger(getClass().getSimpleName()).severe("+# persist credentials mit id " + id_prod_db + " pass " + pass + " salt " + salt);

		//Query q = entityManagerAuth.createNativeQuery("INSERT INTO users VALUES(?, ?, ?)");
		Query q = entityManagerAuth.createNativeQuery("INSERT INTO users (id_prod_db, password, salt) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE password=?, salt=?;");

		q = q.setParameter(1, id_prod_db);
		q = q.setParameter(2, pass);
		q = q.setParameter(3, salt);
		q = q.setParameter(4, pass);
		q = q.setParameter(5, salt);
		int i = q.executeUpdate();
		Logger.getLogger(getClass().getSimpleName()).severe("+# persist credentials mit ergab eine Änderung von  " + i + " Zeilen");
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Override public void persistEmail(Integer id_prod_db, String newEmail)
	{
		Query q = entityManagerAuth.createNativeQuery("INSERT INTO users (id_prod_db, email) VALUES(?, ?) ON DUPLICATE KEY UPDATE email=?;");

		q = q.setParameter(1, id_prod_db);
		q = q.setParameter(2, newEmail);
		q = q.setParameter(3, newEmail);
		int i = q.executeUpdate();
		Logger.getLogger(getClass().getSimpleName()).severe("+# persist email mit ergab eine Änderung von  " + i + " Zeilen");
	}


}
