package de.marcel.restaurant.web.backingBeans;


import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import de.marcel.restaurant.ejb.model.Culinary;
import de.marcel.restaurant.ejb.model.RestaurantVisit;
import de.marcel.restaurant.ejb.model.State;
import de.marcel.restaurant.ejb.model.User;
import org.primefaces.event.DragDropEvent;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


@Named("backingBeanVisit")
@SessionScoped
//@ViewScoped
public class BackingBeanVisit implements Serializable
{
	private static final long serialVersionUID = 1L;
	private RestaurantVisit current = new RestaurantVisit();
	private User currParticipant;

	@Inject
	//@EJB
	// eventuell ist hier eher die @Resource die richtige Annotation? Testen.
	// https://dzone.com/articles/resource-injection-vs
	private IRestaurantEJB appServer;

	// findAll im "appServer" public <T> List<T> findAll(Class entitiyClass)

	public List<Culinary> getAllCulinaries()
	{
		return appServer.findAll(Culinary.class);
	}
	public List<RestaurantVisit> getAllVisits()
	{
		return appServer.findAll(RestaurantVisit.class);
	}

	public void setCurrent(RestaurantVisit u)
	{
		this.current = u;
	}

	public void setDateFromContext()
	{
		ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();
		String sessionId = ectx.getSession(false).toString();
		LocalDateTime dinginAt = (LocalDateTime) ectx.getSessionMap().get(sessionId + "chosenDate");
		current.setVisitingDateTime(dinginAt);
	}

	public String checkStateVisit()
	{
		setDateFromContext();
		setStateVisit();
		if(current.getStateVisit().ordinal() < 1)
		{
			return "";
		}
		else
		{
			saveVisit();
			return "VisitList?faces-redirect=true";
		}
	}

	public String checkStateVisitNext()
	{
		setDateFromContext();
		setStateVisit();
		if(current.getStateVisit().ordinal() < 1)
		{
			return "";
		}
		else
		{
			saveVisit();
			return "VisitSuggestion?faces-redirect=true";
		}
	}

	public String saveVisit()
	{
		if(current != null)
		{
				if(null == current.getPrim())
				{
					insert(current);
				}

				else
				{
					update(current);
				}

		}
		current = new RestaurantVisit();
		return "VisitList?faces-redirect=true";
	}

	public void insert(RestaurantVisit u)
	{
		appServer.persist(u);
	}

	public void update(RestaurantVisit u)
	{
		appServer.update(u);
	}

	public String edit(RestaurantVisit u)
	{
		this.current = u;
		return "VisitCreateDateTime?faces-redirect=true";
	}

	public String delete(RestaurantVisit u)
	{
		appServer.delete(u);
		return "VisitList?faces-redirect=true";
	}

	public RestaurantVisit getCurrent()
	{
		return current;
	}

	public String createNew()
	{
		current = new RestaurantVisit();

		return "VisitCreateDateTime?faces-redirect=true";
	}

	// Ratings von 1 - 11 Punkten
	// Rating mit 0 gibt es nicht, gilt als unbewertet.
	public void calculateAvgRating(RestaurantVisit r)
	{
		if(r != null)
		{
			byte b = (byte) r.getRatingsVisit().stream()
							.mapToInt((e)->e.getStars())
							.filter(f -> {
								if(f >= 11 || f <= 0)
									return false;
								return true;})
							.average()
							.orElseGet(()-> 0.0);

			r.setAverageRating(b);
		}
	}

	public void setStateVisit()
	{
		// Könnte man auch im Enum unterbringen, dann wäre alles zusammen.
		// Mind 1 TN zum Speichern
		// Vorschlagsszenarien (An oder um festem Ort mit Radius -> Immer es gibt immer einen Ort egal ob aus Wohnorten oder LiveStandorten)
			// Mit ausschließlicher Kulinarik Vorgabe (Kulinarik-Match-Bucket hat nur einen Eintrag)
			// Kulinarik aus dominierender Vorliebe (Kulinarik-Match-Bucket hat nur einen Eintrag)
			// Kulinarik Auswahl aus Vorlieben (Kulinarik-Match-Bucket hat mehrere Einträge)
			// alles nur ein Case

		// DateTime abgelaufen, bewertungsreif
		// >= 1 Bewertung erteilt, bewertungen offen
		// Alle Bewertungen aller Teilnehmer eingegangen, abgeschlossen

		Logger.getLogger(getClass().getSimpleName()).log(Level.WARNING, "+#+# BackingBeanVisit: Current enthält als State " + current);

		int switchVar = current.getStateVisit().ordinal();
		if (switchVar == 5) return;

		switch (switchVar)
		{
			case 0: {
				if((current.getParticipants().size() >= 1) &
					(current.getVisitingDateTime().isAfter(LocalDateTime.of(2000,01,01,01,01))))
					current.setStateVisit(State.ERWÜNSCHT);
				else break;
			}
			case 1:{
				if(current.getRestaurantChosen() != null)
					current.setStateVisit(State.GEPLANT);
				else break;
			}
			case 2:{
				if(current.getVisitingDateTime().isAfter(LocalDateTime.now()))
					current.setStateVisit(State.ERFOLGT);
				else break;

			}
			case 3:{
				if(current.getAverageRating() > 0)
					current.setStateVisit(State.BEWERTUNG_OFFEN);
				else break;
			}
			case 4:{
				if(current.getParticipants().size() == current.getRatingsVisit().size())
					current.setStateVisit(State.BEWERTET);
				else break;
			}
		}
	}

	public User getCurrParticipant()
	{
		return currParticipant;
	}

	public void setCurrParticipant(User currParticipant)
	{
		this.currParticipant = currParticipant;
	}

	// Methods and Members for Drag and Drop Area

	private Set<User> availableUsers;
	private Set<User> droppedUsers = current.getParticipants();
	private User selectedUser;

	public Set<User> getDroppedUsers() {
		return current.getParticipants();
	}

	public void fillUsers()
	{
		availableUsers = new HashSet<>(appServer.findAll(User.class));
		// reset
		//current.setParticipants(new HashSet<>());
		//Logger.getLogger(getClass().getSimpleName()).log(Level.WARNING, "+#+# BackingBeanVisit: fillUsers holte " + availableUsers.size() + " " + availableUsers);
	}

	public Set<User> getAvailableUsers() {

		current.getParticipants().stream().forEach(e -> availableUsers.remove(e));

		return availableUsers;
	}

	public User getSelectedUser() {


		return selectedUser;
	}

	public void setSelectedUser(User s) {

		this.selectedUser = s;
	}

	public void removeFromAvailableUsers(User u)
	{
		availableUsers.remove(u);
	}

	public void removeFromDroppedUsers(User u)
	{
		// TODO: Button remove selected User
	}

	public void onUserDrop(DragDropEvent ddEvent)
	{
		User u = (User) ddEvent.getData();
		Set<User> set = current.getParticipants();
		set.add(u);
		current.setParticipants(set);
		availableUsers.remove(u);
	}
}
