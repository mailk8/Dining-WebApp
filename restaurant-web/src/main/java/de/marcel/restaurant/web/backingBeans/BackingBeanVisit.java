package de.marcel.restaurant.web.backingBeans;


import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import de.marcel.restaurant.ejb.model.Culinary;
import de.marcel.restaurant.ejb.model.RestaurantVisit;
import de.marcel.restaurant.ejb.model.State;
import de.marcel.restaurant.ejb.model.User;

import javax.enterprise.concurrent.ManagedExecutorService;
import javax.annotation.Resource;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Named()
@SessionScoped
public class BackingBeanVisit implements Serializable
{
	private static final long serialVersionUID = 1L;
	private RestaurantVisit current = new RestaurantVisit();

	@Resource(name = "DefaultManagedExecutorService") ManagedExecutorService executor;
	@Inject private IRestaurantEJB appServer;
	@Inject private BackingBeanUser backingBeanUser;
	private String sizeParticipantsForValidator;
	private List<RestaurantVisit> visitList;
	private List<Culinary> allCulinariesProxy;
	private String zoneString;
	private Future<HashSet<Integer>> userSetVisits;
	private HashSet<Integer> set;

	////////////////////////////////// Methods for Culinary Selection //////////////////////////////
	public Culinary[] getCulinariesArray()
	{
		//Logger.getLogger(getClass().getSimpleName()).severe("+# getCulinariesArray läuft, return : " + current.getChosenCulinaries() );
		return current.getChosenCulinaries().stream().toArray(Culinary[]::new);
	}

	public void setCulinariesArray(Culinary[] culinariesArray)
	{
		//Logger.getLogger(getClass().getSimpleName()).severe("+# setCulinariesArray läuft, return " + Arrays.toString(culinariesArray));
		current.setChosenCulinaries(Arrays.asList(culinariesArray));
	}

	public List<Culinary> getAllCulinariesProxy()
	{
		//Logger.getLogger(getClass().getSimpleName()).severe("+# getAllCulinariesProxy läuft ");
		if(null == allCulinariesProxy)
		{
			allCulinariesProxy = getAllCulinaries();
		}
		return allCulinariesProxy;
	}




	//////////////////////////  Methods for Fetching & Performane //////////////////////////
	public void fetchVisitsForUser()
	{
		// Asynchronous Callable-Job for Servers Default-ThreadPoolExecutor
		// appServer and user CAN be passed as locale References
		// BUT user CAN NOT be retrieved by 'backingBean.getCurrent()' !!

		User user = backingBeanUser.getCurrent();
		if( null != user )
		{
			userSetVisits = executor.submit(()-> appServer.findAllVisitsForUser(user));
		}
		else
		{
			userSetVisits = null;
		}
	}

	public void prepareGetAllCulinaries()
	{
		allCulinariesProxy = getAllCulinaries();
	}

	public List<Culinary> getAllCulinaries()
	{
		//Logger.getLogger(getClass().getSimpleName()).severe("+# getAllCulinaries läuft ");
		return appServer.findAll(Culinary.class);
	}

	public List<RestaurantVisit> getAllVisits()
	{
		//Logger.getLogger(getClass().getSimpleName()).severe("+# getAllVisits läuft");
		visitList = appServer.findAll(RestaurantVisit.class);
		visitList.forEach((e) -> updateVisitState(e));
		fetchVisitsForUser();
		return  visitList;
	}


	public List<RestaurantVisit> getAllVisitsProxy()
	{
		return visitList;
	}

	//////////////////////////  Methods for Visit Functions //////////////////////////
	public void calculateAvgRating(RestaurantVisit r)
	{
		if(r != null)
		{
			byte b = (byte) r.getRatingsVisit().stream()
							.mapToInt((e)->e.getStars())
							.filter(f -> {
								if(f > 10 || f <= 0)
									return false;
								return true;})
							.average()
							.orElseGet(()-> 0.0);

			r.setAverageRating(b);
		}
	}

	public void updateVisitState(RestaurantVisit visit)
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

		////Logger.getLogger(getClass().getSimpleName()).severe("+# BackingBeanVisit: visit ist " + visit);

		int switchVar = visit.getStateVisit().ordinal();

		switch (switchVar)
		{
			case 0: {
				if( (visit.getParticipants().size() >= 1) && (visit.getVisitingDateTime().isAfter(LocalDateTime.of(2000,01,01,01,01))))
					visit.setStateVisit(State.ANGELEGT); // 1
				else break;
			}
			case 1:{
				if(visit.getRestaurantChosen() != null)
					visit.setStateVisit(State.GEPLANT); // 2
				else break;
			}
			case 2:{
				if(ZonedDateTime.of(visit.getVisitingDateTime(), ZoneId.of(visit.getTimezoneString())).isAfter(ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault()))) //////////// SERVERZEIT, Vorsicht
					visit.setStateVisit(State.ERFOLGT); // 3
				else break;

			}
			case 3:{
				if(visit.getAverageRating() > 0)
					visit.setStateVisit(State.BEWERTUNG_OFFEN); // 4
				else break;
			}
			case 4:{
				if(visit.getParticipants().size() == visit.getRatingsVisit().size())
					visit.setStateVisit(State.BEWERTET);
				else break;
			}
		}
		//Logger.getLogger(getClass().getSimpleName()).severe("+# updateVisitState, for " + visit.getVisitingDateTime());
	}





	//////////////////////////  Methods for Basic Crud & Navigation //////////////////////////
	public String saveVisit()
	{
		//Logger.getLogger(getClass().getSimpleName()).severe("+# begin save Visit -----------------------------");
		//Logger.getLogger(getClass().getSimpleName()).severe("+# vor update visitState, current.getPrim " + current.getPrim());
		updateVisitState(current);
		if(null == current.getPrim())
		{
			//Logger.getLogger(getClass().getSimpleName()).severe("+# insert zweig, current.getPrim " + current.getPrim());
			insert(current);
		}
		else
		{
			update(current);
		}
		//prepareGetAllVisits();
		//Logger.getLogger(getClass().getSimpleName()).severe("+# nach save und prepareAllVisits -------------------");


		return "VisitList?faces-redirect=true";
	}

	public String saveVisitNext()
	{
		saveVisit();
		return "VisitSuggestions?faces-redirect=true";
	}

	public void insert(RestaurantVisit u)
	{
		int result = appServer.persist(u);
		u.setPrim(result);
		//Logger.getLogger(getClass().getSimpleName()).severe("+# nach insert, prim ist lt appServer " + result + " current hat prim " + current.getPrim());
	}

	public void update(RestaurantVisit u)
	{
		appServer.update(u);
	}

	public String edit(RestaurantVisit u)
	{
		this.current = u;
		//Logger.getLogger(getClass().getSimpleName()).severe("+# edit aufgerufen, von der Datalist wurde Visit übergeben " + u);
		current.setStateVisit(State.UNVOLLSTÄNDIG);
		Logger.getLogger(getClass().getSimpleName()).severe("+# edit leitet weiter ----------------------------- ");
		return "VisitCreate?faces-redirect=true";
	}

	public String delete(RestaurantVisit u)
	{
		appServer.delete(u);
		//prepareGetAllVisits();
		return "VisitList?faces-redirect=true";
	}

	public String createNew()
	{
		current = new RestaurantVisit();
		current.setTimezoneString(zoneString);
		return "VisitCreate?faces-redirect=true";
	}

	public void setCurrent(RestaurantVisit u)
	{
		this.current = u;
	}

	public RestaurantVisit getCurrent()
	{
		return current;
	}





	//////////////////////////  Methods for Participants Functions //////////////////////////
	public String getSizeParticipantsForValidator()
	{
		//Logger.getLogger(getClass().getSimpleName()).severe("+# getSizeParticipantsForValidator aufgerufen mit " + current.getParticipants().size());
		sizeParticipantsForValidator = current.getParticipants().size()+"";
		return sizeParticipantsForValidator;
	}

	public void setSizeParticipantsForValidator(String sizeParticipantsForValidator)
	{
		//Logger.getLogger(getClass().getSimpleName()).severe("+# setSizeParticipantsForValidator aufgerufen mit " + sizeParticipantsForValidator);
		this.sizeParticipantsForValidator = sizeParticipantsForValidator;

	}


	//////////////////////////  Methods for Date Time Functions //////////////////////////
	public Set<String> getAllTimezones()
	{
		return new TreeSet<String>(ZoneId.getAvailableZoneIds());
	}

	public List<String> completeText(String query) {
		String queryLowerCase = query.toLowerCase();

		return getAllTimezones().stream().filter(t -> t.toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
	}

	public String getZoneString()
	{
		return zoneString;
	}

	public void setZoneString(String zoneString)
	{
		this.zoneString = zoneString;
	}





	//////////////////////////  Methods for View DataList All Visits //////////////////////////
	public boolean isUnfinished(RestaurantVisit visit)
	{
		return visit.getStateVisit().ordinal() == 0; // State.UNVOLLSTÄNDIG
	}

	public boolean isUserParticipantOf(RestaurantVisit visit)
	{
		try
		{
			if( null != userSetVisits )
			{
				if ( null !=  (set = userSetVisits.get()) )
				{
					return set.contains(visit.getPrim());
				}
				else
				{
					return false;
				}
			}
		}
		catch (InterruptedException | ExecutionException e)
		{
			e.printStackTrace();
			set = null;
			return false;
		}
		set = null;
		return false;
	}

}
