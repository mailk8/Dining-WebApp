package de.marcel.restaurant.web.backingBeans;


import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import de.marcel.restaurant.ejb.model.Address;
import de.marcel.restaurant.ejb.model.Culinary;
import de.marcel.restaurant.ejb.model.User;
import de.marcel.restaurant.web.UserMailController;
import de.marcel.restaurant.web.httpClient.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.omnifaces.util.Faces;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.RowSet;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Named
@SessionScoped
public class BackingBeanUser implements Serializable
{

	private static final long serialVersionUID = 1L;
	private User current;
	@Inject private IRestaurantEJB appServer;
	@Inject private HttpClientWGS client;

	private String validateLon, validateLat;


	@PostConstruct
	private void fetchLoggedInUser()
	{
		Subject s = SecurityUtils.getSubject();
		if(s.isAuthenticated())
		{
			Session sess = s.getSession();
			User u = (User) sess.getAttribute("loggedInUser");
			if(u.getEmail().equals(s.getPrincipal()))
			{
				setCurrent(u);
			}
			sess.removeAttribute("loggedInUser");
		}
		else
		{
			setCurrent(new User());
		}
	}

	public List<Culinary> getAllCulinaries()
	{
		return appServer.findAll(Culinary.class);
	}

	public List<User> getAllUsers(){return appServer.findAll(User.class);}

	public void setCurrent(User u)
	{
		this.current = u;
		setCoordinatesBean(u);
	}

	public String saveUser()
	{
		String s="empty";
		if(current != null)
		{
			if(null == current.getPrim())
			{
				setCoordinatesEntity();
				insert(current);
				s = "insert";
			}

			else
			{
				setCoordinatesEntity();
				update(current);
				s = "update";
			}

		}

		Logger.getLogger(getClass().getSimpleName()).severe("+# Es wurde " + s + " aufgerufen in Phase " + FacesContext.getCurrentInstance().getCurrentPhaseId().getName() +
						" Current User firstname ist " + current.getFirstname() + " mit id " + current.getPrim());

		//current = new User(); // Nur in createNew !
		return "UserList?faces-redirect=true";
	}

	public void insert(User u)
	{
		Integer i = appServer.persist(u);
		u.setPrim(i);
	}

	public void update(User u)
	{
		Integer i = appServer.update(u);
		u.setPrim(i);
	}

	public String edit(User u)
	{
		this.current = u;
		setCoordinatesBean(u);
		return "UserCreate?faces-redirect=true";
	}

	public String delete(User u)
	{
		appServer.deleteCredentials(u.getPrim());
		appServer.delete(u);
		return "UserList?faces-redirect=true";
	}

	public User getCurrent()
	{
		return current;
	}

	public String createNew()
	{
		current = new User();
		setCoordinatesBean(current);
		insert(current);

		return "UserCreate?faces-redirect=true";
	}

	public String getValidateLat()
	{
		return validateLat;
	}

	public String getValidateLon()
	{
		return validateLon;
	}

	public void setValidateLat(String validateLat)
	{
		validateLat = validateLat.replace(",", ".");
		this.validateLat = validateLat;
	}

	public void setValidateLon(String validateLon)
	{
		validateLon = validateLon.replace(",", ".");
		this.validateLon = validateLon;
	}

	public void setCoordinatesBean(User u)
	{
		try
		{
			// Ist der Wert null, also nicht gesetzt, soll ein empty String dargestellt werden
			this.validateLat = u.getAddressActual().getWgs84Latitude().toString();
			this.validateLon = u.getAddressActual().getWgs84Longitude().toString();
		}
		catch (NullPointerException e)
		{
			this.validateLat = "";
			this.validateLon = "";
		}
	}

	public void setCoordinatesEntity()
	{
		if(validateLat.equals(""))
			current.getAddressActual().setWgs84Latitude(null);

		if(validateLon.equals(""))
			current.getAddressActual().setWgs84Longitude(null);

		try
		{
			current.getAddressActual().setWgs84Latitude(Double.parseDouble(validateLat));
			current.getAddressActual().setWgs84Longitude(Double.parseDouble(validateLon));
		}
		catch (Exception e)
		{
			// null soll erlaubt sein wenn kein Wert gesetzt ist
			//throw new NullPointerException("Fehler in setCoordinatesEntity");
			throw new NumberFormatException("Fehler in setCoordinatesEntity mit Lat" + validateLat + " Lon " + validateLon);
		}
	}

	public void requestWgsForAddress()
	{
		//client.enqueueNewRequest(current.getAddressLiving(), appServer);
			List<Address> ballern = appServer.findAll(Address.class).stream()
							.map( e -> (Address) e)
							.filter((Address d) -> (d.getPrim().intValue() >= 810))//.limit(1)
							.collect(Collectors.toList());
		System.out.println("+# Testliste mit Elementen " + ballern.size() + "\n" + ballern);
		ballern.forEach(e->{
			System.out.println(e + " trys: " + e.getCounterApiCalls());
		});
//
//		ballern.forEach(e->{
//			client.enqueueNewRequest(e, appServer);
//		});
	}

	public void removeMessages(AjaxBehaviorEvent event)
	{
		Iterator<FacesMessage> it = FacesContext.getCurrentInstance().getMessages();
		while ( it.hasNext() ) {
			it.next();
			it.remove();
		}
	}

	public void isEmailDuplicated(AjaxBehaviorEvent event)
	{

		Iterator<FacesMessage> it = FacesContext.getCurrentInstance().getMessages();
		while ( it.hasNext() ) {
			it.next();
			it.remove();
		}

		UIComponent uc = event.getComponent();
		UIInput ui = (UIInput) event.getSource();

		String input = ui.getSubmittedValue().toString(); //////////////// °!!!!!!!!!!

		if(!input.equals("test")){
			FacesMessage fm = new FacesMessage("Bereits vorhanden sum", "Bereits vorhanden det");
			fm.setSeverity(FacesMessage.SEVERITY_FATAL);
			FacesContext.getCurrentInstance().addMessage(null, fm);
		}
		else
		{
			FacesMessage fm = new FacesMessage("Email ist wählbar");
			fm.setSeverity(FacesMessage.SEVERITY_INFO);
			FacesContext.getCurrentInstance().addMessage(null, fm);
		}
//
//		Logger.getLogger(getClass().getSimpleName()).severe("+# Habe als email erhalten: " + input  );
//
//		Logger.getLogger(getClass().getSimpleName()).severe("+# UIComponent renderType" + uc.getRendererType() + " clientId " + uc.getClientId()  );
//		Logger.getLogger(getClass().getSimpleName()).severe("+# UIInput renderType" + ui.getRendererType() + " clientId " + ui.getClientId()  );

//		Iterator<String> it = event.getFacesContext().getClientIdsWithMessages();
//		while(it.hasNext())
//		{
//			Logger.getLogger(getClass().getSimpleName()).severe("+# getFacesContext().getClientIdsWithMessages" + it.next());
//		}


//		if(UserMailController.check(input))
//		{

//			FacesMessage fm = new FacesMessage("Bereits vorhanden sum", "Bereits vorhanden det");
//			fm.setSeverity(FacesMessage.SEVERITY_FATAL);
//			FacesContext.getCurrentInstance().addMessage(null, fm);
//			event.getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Bereits vorhanden sum", "Bereits vorhanden det"));
//
//			FacesContext.getCurrentInstance().addMessage("messageForEmailP", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Bereits vorhanden sum", "Bereits vorhanden det"));
//			event.getFacesContext().addMessage("messageForEmailP", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Bereits vorhanden sum", "Bereits vorhanden det"));
//
//			FacesContext.getCurrentInstance().addMessage("messageForEmailH", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Bereits vorhanden sum", "Bereits vorhanden det"));
//			event.getFacesContext().addMessage("messageForEmailH", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Bereits vorhanden sum", "Bereits vorhanden det"));
//
//			FacesContext.getCurrentInstance().addMessage("email", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Bereits vorhanden sum", "Bereits vorhanden det"));
//			event.getFacesContext().addMessage("email", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Bereits vorhanden sum", "Bereits vorhanden det"));



//
//			ui.setValid(false);
//			((UIInput)uc).setValid(false);
//		}

		//FacesContext.getCurrentInstance().renderResponse();

	}

}
