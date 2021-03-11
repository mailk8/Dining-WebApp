package de.marcel.restaurant.web.security;


public class Credentials implements ICredentials
{
	public static final long serialVersionUID = 1L;

	private String salt;

	private Integer id_prod_db;

	private String password;

	private String email;

	public String getEmail()
	{
		return email;
	}

	public String getPassword()
	{
		return password;
	}

	public String getSalt()
	{
		return salt;
	}

	public Integer getId_prod_db()
	{
		return id_prod_db;
	}

	protected void setSalt(String salt)
	{
		this.salt = salt;
	}

	protected void setId_prod_db(Integer id_prod_db)
	{
		this.id_prod_db = id_prod_db;
	}

	protected void setPassword(String password)
	{
		this.password = password;
	}

	protected void setEmail(String email)
	{
		this.email = email;
	}
}