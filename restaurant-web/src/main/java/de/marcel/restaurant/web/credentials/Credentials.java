package de.marcel.restaurant.web.credentials;


public class Credentials implements de.marcel.restaurant.web.credentials.ICredentials
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

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getSalt()
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
		this.id_prod_db = id_prod_db;
	}
}