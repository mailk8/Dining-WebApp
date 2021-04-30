package de.marcel.restaurant.ejb.interfaces;

import java.io.Serializable;

public interface ICredentials extends Serializable
{
	 String getEmail();
	 String getPassword();
	 String getSalt();
	 Integer getId_prod_db();

	void setSalt(String salt);
	void setId_prod_db(Integer id_prod_db);
	void setPassword(String password);
	void setEmail(String email);
}
