package de.marcel.restaurant.ejb.model;

import de.marcel.restaurant.ejb.interfaces.ICredentials;

/*
	Credentials-Objekt zur Vereinfachung des Buildprozesses in EJB-Komponente verschoben.
	Verlagert man es in die Webkomponente ergibt sich
		Vorteil: Setter können protected sein
		Nachteil: Zirkuläre Dependency, die beim Clean beider Komponenten einen aufwendigen Workaround notwendig macht.
 */

public class Credentials implements ICredentials
{
	public static final long serialVersionUID = 1L;

	private String salt;

	private Integer id_prod_db;

	private String password;

	private String email;

	@Override public String getEmail()
	{
		return email;
	}

	@Override public String getPassword()
	{
		return password;
	}

	@Override public String getSalt()
	{
		return salt;
	}

	@Override public Integer getId_prod_db()
	{
		return id_prod_db;
	}

	@Override public void setSalt(String salt)
	{
		this.salt = salt;
	}

	@Override public void setId_prod_db(Integer id_prod_db)
	{
		this.id_prod_db = id_prod_db;
	}

	@Override public void setPassword(String password)
	{
		this.password = password;
	}

	@Override public void setEmail(String email)
	{
		this.email = email;
	}
}