package de.marcel.restaurant.ejb;


import de.marcel.restaurant.ejb.interfaces.IBaseEntity;
import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import de.marcel.restaurant.ejb.model.User;
import jakarta.enterprise.context.RequestScoped;

import javax.annotation.ManagedBean;
import javax.ejb.*;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.logging.Logger;



//@Named
//@SessionScoped
//@RequestScoped
@Stateful
@Remote(IRestaurantEJB.class)
public class RestaurantEJB implements IRestaurantEJB
{
	private static final long serialVersionUID = 1L;

	@PersistenceContext(unitName="restaurant_ejb")
	private transient EntityManager entityManager;

	@PersistenceContext(unitName="restaurant_auth")
	private transient EntityManager entityManagerAuth;

	private transient Credentials cred = new Credentials();
	private transient User user = new User();


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

	@Override public <IBaseEntity> List<IBaseEntity> findAll(Class entitiyClass)
	{
		TypedQuery<IBaseEntity> query = entityManager.createNamedQuery(entitiyClass.getSimpleName()+".findAll", entitiyClass);

		return query.getResultList();
	}

	@Override
	public <T extends IBaseEntity> IBaseEntity findOne(Object characterisitcAttribute, Class attributeClazz, Class<T> resultClazz)
	{
		TypedQuery<?> query = entityManager.createNamedQuery(resultClazz.getSimpleName()+".findOne", resultClazz);
		query.setParameter(1, attributeClazz.cast(characterisitcAttribute));

		return (IBaseEntity) query.getSingleResult();
	}

	@Override
	public <T extends IBaseEntity> IBaseEntity findOneByPrim(String prim, Class<T> resultClazz)
	{
		TypedQuery<?> query = entityManager.createNamedQuery(resultClazz.getSimpleName()+".findOneByPrim", resultClazz);
		query.setParameter("attribute", Integer.parseInt(prim));

		return (IBaseEntity) query.getSingleResult();
	}


	@Override public synchronized void proxyPersistCredentials(Integer id, String pass, String salt) {
		Logger.getLogger(getClass().getSimpleName()).severe("+# persist credentials aufgerufen mit id " + id + " pass " + pass + " salt " + salt + " this + " + this);

		cred.setPassword(pass);
		cred.setSalt(salt);
		cred.setId_prod_db(id);
		// checkAndMatch(); // ValueChangedListener der das Passwort liefert läuft früh in JSF Phase 3
	}

	@Override public synchronized Integer proxyPersistUser(User u) {
		Logger.getLogger(getClass().getSimpleName()).severe("+# proxyPersistUser aufgerufen. this" +  this);
		this.user = u;
		return checkAndMatch();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	private synchronized int persistCredentials(Credentials credentials) {
		Logger.getLogger(getClass().getSimpleName()).severe("+# dpersistCredentials aufgerufen this + " + this);
		Query q = null;
		int result;
		try
		{
			q = entityManagerAuth.createNativeQuery("INSERT INTO users (email, id_prod_db, password, salt) VALUES(?1, ?2, ?3, ?4) ON DUPLICATE KEY UPDATE email=?1, password=?3, salt=?4;");
			q = q.setParameter(1, user.getEmail());
			q = q.setParameter(2, credentials.getId_prod_db());
			q = q.setParameter(3, credentials.getPassword());
			q = q.setParameter(4, credentials.getSalt());
			result = q.executeUpdate();
			Logger.getLogger(getClass().getSimpleName()).severe("+# persist credentials ergab eine Änderung von  " + result + " Elementen mit email " + user.getEmail() );

		}
		catch (Exception e)
		{
			e.printStackTrace();
			return -1;
		}

		return result;
	}

	private synchronized int checkAndMatch() {
		Logger.getLogger(getClass().getSimpleName()).severe("+# checkAndMatch aufgerufen " + this);

		if(null == user.getPrim()) // User invalid
		{
			return -1;
		}
		else if(null == cred.getId_prod_db()) // Nur User speichern
		{
			Logger.getLogger(getClass().getSimpleName()).severe("+# null == cred.getId_prod_db()    " + this);

			if(null != user.getEmail()) // Speichern wenn er eine E-Mail besitzt
			{
				if(update(user) > 0)
				{
					return 1; // User erfolgreich gespeichert
				}
				else
				{
					return -2;
				}
			}
			else
			{
				return -3;
			}
		}
		else // User und Credentials speichern
		{
			if(user.getPrim().equals(cred.getId_prod_db())) // Gehören User und Credentials zusammen?
			{
				int resultUser = update(user);
				int resultCred = persistCredentials(cred);
				user = new User(); cred = new Credentials();

				if((resultUser > 0) && (resultCred > 0))
				{
					return 2; // User und Credentials erfolgreich gespeichert
				}
				else
				{
					return -3;
				}
			}
			else
			{
				return -4;
			}
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Override public synchronized void deleteCredentials(Integer id_prod_db) {
		Logger.getLogger(getClass().getSimpleName()).severe("+# deleteCredentials mit id " + id_prod_db + "  " + this);

		Query q = entityManagerAuth.createNativeQuery("DELETE FROM users WHERE id_prod_db=?");

		q = q.setParameter(1, id_prod_db);
		int i = q.executeUpdate();

		Logger.getLogger(getClass().getSimpleName()).severe("+# deleteCredentials ergab eine Änderung von  " + i + " Elementen " + this);
	}

	private class Credentials {
		private String salt;
		private Integer id_prod_db;
		private String password;

		private String getPassword()
		{
			return password;
		}

		private void setPassword(String password)
		{
			this.password = password;
		}

		private String getSalt()
		{
			return salt;
		}

		public void setSalt(String salt)
		{
			this.salt = salt;
		}

		public Integer getId_prod_db()
		{
			return id_prod_db;
		}

		public void setId_prod_db(Integer id_prod_db)
		{
			Logger.getLogger(getClass().getSimpleName()).severe("+# setId_prod_db aufgerufen, value ist " + id_prod_db +" " );
			this.id_prod_db = id_prod_db;
		}
	}


}

