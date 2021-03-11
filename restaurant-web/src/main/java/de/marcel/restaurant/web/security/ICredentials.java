package de.marcel.restaurant.web.security;

import java.io.Serializable;

public interface ICredentials extends Serializable
{
	 public String getEmail();
	 public String getPassword();
	 public String getSalt();
	 public Integer getId_prod_db();
}
