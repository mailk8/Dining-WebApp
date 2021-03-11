package de.marcel.restaurant.web.credentials;

import java.io.Serializable;

public interface ICredentials extends Serializable
{
	 String getEmail();
	 String getPassword();
	 String getSalt();
	 Integer getId_prod_db();
}
