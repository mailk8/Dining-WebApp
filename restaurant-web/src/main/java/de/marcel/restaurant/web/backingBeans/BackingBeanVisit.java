package de.marcel.restaurant.web.backingBeans;


import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import de.marcel.restaurant.ejb.model.Culinary;
import de.marcel.restaurant.ejb.model.RestaurantVisit;
import de.marcel.restaurant.ejb.model.State;
import de.marcel.restaurant.ejb.model.User;
import org.primefaces.event.AbstractAjaxBehaviorEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.MapModel;

import javax.annotation.ManagedBean;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.annotation.Resource;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
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

/*
	Was passiert, wenn sich ein Teilnehmer löscht, der zu einem Visit gehört?
		Es ist eine @manyToMany Beziehung zwischen User und Visit: Da gibt es kein orphanRemoval als Attribut, das hinter CascadeType angeführt werden könnte.
		https://stackoverflow.com/questions/3055407/how-do-i-delete-orphan-entities-using-hibernate-and-jpa-on-a-many-to-many-relati

	Was passiert, wenn man ein Restaurant löscht, das in einem Visit verplant ist?
		Wird neu gespeichert

	Genau das gleiche bei Rating?
 */
@Named
@SessionScoped  // ViewScoped nicht ohne Weiteres möglich
@ManagedBean
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
	private Future<HashSet<Integer>> allVisitsThisUser;
	private HashSet<Integer> set;
	private MapModel geoModel;
	private String googleMapsResult;

	public void init() {
		geoModel = new DefaultMapModel();
	}

	public void proxyOnLoad() {
		getAllVisits().forEach(e -> e.setStateVisit(State.UNVOLLSTÄNDIG));
		visitList.stream().forEach(e -> updateVisitState(e));
	}



	////////////////////////////////// Methods for Culinary Selection //////////////////////////////
	public Culinary[] getCulinariesArray()
	{
		return current.getChosenCulinaries().stream().toArray(Culinary[]::new);
	}

	public void setCulinariesArray(Culinary[] culinariesArray)
	{
		Logger.getLogger(getClass().getSimpleName()).severe("+# setCulinariesArray  event value " + Arrays.deepToString(culinariesArray));
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




	////////////////////////////////// Methods for Location of Search //////////////////////////////
	public void setGoogleMapsResult(String googleApiReturn)
	{
		Logger.getLogger(getClass().getSimpleName()).severe("+# setGoogleMapsResult hat erhalten: " + googleApiReturn);
		try
		{
			if( null != googleApiReturn )
			{
				String[] resultSplit = googleApiReturn.split("(\\+#)");
				if( resultSplit[0].equals("OK") && resultSplit.length == 3 ) // http 200er Status der Google Api
				{
					String[] lat_lon = resultSplit[1].replaceAll("[)('\"]", "").split("[,]");
					current.getAddressVisit().setWgs84Latitude(Double.parseDouble(lat_lon[0].trim()));
					current.getAddressVisit().setWgs84Longitude(Double.parseDouble(lat_lon[1].trim()));
					FacesContext.getCurrentInstance().addMessage("place",
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Zu dieser Eingabe wurde die Location '" + resultSplit[2] + " " + resultSplit[1] + "' gefunden.",  ""));
					return;
				}
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(getClass().getSimpleName()).severe("+# Exception! " + e.getMessage() + e.getClass());
		}

		FacesContext.getCurrentInstance().addMessage("place",
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Zu dieser Eingabe wurde kein Ort gefunden.",  ""));

		//googleMapsResult = null;

	}




	//////////////////////////  Methods for Fetching & Performane //////////////////////////
	public void fetchVisitsForUser() // todo: warum native Query? Geht am Cache vorbei!
	{
		// Asynchronous Callable-Job for Servers Default-ThreadPoolExecutor
		// appServer and user CAN be passed as locale References
		// BUT user CAN NOT be retrieved by 'backingBean.getCurrent()' !!

		User user = backingBeanUser.getCurrent();
		if( null != user )
		{
			allVisitsThisUser = executor.submit(()-> {
				Logger.getLogger(getClass().getSimpleName()).severe("+# Executor führt asynchrone Methode aus: findAllVisitsForUser ");
				return appServer.findAllVisitsForUser(user);
			});
		}
		else
		{
			allVisitsThisUser = null;
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
		//visitList.forEach((e) -> updateVisitState(e.ra));
		fetchVisitsForUser();
		return  visitList;
	}

	public List<RestaurantVisit> getAllVisitsProxy()
	{
		return visitList;
	}





	//////////////////////////  Methods for Visit Functions //////////////////////////


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
				//else break;
			}
			case 1:{
				if(visit.getRestaurantChosen() != null && visit.getStateVisit().ordinal() == 1)
					visit.setStateVisit(State.GEPLANT); // 2
				//else break;
			}
			case 2:{
				if((ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault()))
					.isAfter(ZonedDateTime.of(visit.getVisitingDateTime(), ZoneId.of(visit.getTimezoneString())))
								&& visit.getStateVisit().ordinal() == 2)
					visit.setStateVisit(State.BEWERTBAR); // 3
				//else break;
			}
			case 3:{
				if(visit.getParticipants() != null && visit.getRatings() != null && visit.getRatings().size() > 0 &&
					(visit.getParticipants().size() >= visit.getRatings().size()) && visit.getStateVisit().ordinal() == 3)
					visit.setStateVisit(State.BEWERTUNG_AUSSTEHEND); // 4
				//else break;
			}
			case 4:{
				if(visit.getParticipants() != null && visit.getRatings() != null &&
					 visit.getParticipants().size() == visit.getRatings().size() && visit.getStateVisit().ordinal() == 4)
					visit.setStateVisit(State.BEWERTET); // 5
				//else break;
			}
		}
		//Logger.getLogger(getClass().getSimpleName()).severe("+# updateVisitState, for " + visit.getVisitingDateTime());
	}

	public String rate(RestaurantVisit u)
	{
		this.current = u;
		return "VisitRating?faces-redirect=true";
	}




	//////////////////////////  Methods for Basic Crud & Navigation //////////////////////////
	public String save()
	{
		Logger.getLogger(getClass().getSimpleName()).severe("+# saveVisit persistiert current mit chosen Rest. " + current.getRestaurantChosen());
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
		save();
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

	public String edit(RestaurantVisit u) {

		if(redirectIfWrongState(u, 0,2) == null)
		{
			this.current = u;
			current.setStateVisit(State.UNVOLLSTÄNDIG);
			return "VisitCreate?faces-redirect=true";
		}
		return "VisitList?faces-redirect=true";
	}

	public String delete(RestaurantVisit u) {

		if(redirectIfWrongState(u, 0,2) == null)
			appServer.delete(u);
		return "VisitList?faces-redirect=true";
	}

	public String createNew() {

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


	public String redirectIfWrongState(int allowedStateFrom, int allowedStateTill) {
		return redirectIfWrongState(current, allowedStateFrom, allowedStateTill);
	}

	public String redirectIfWrongState(RestaurantVisit visit, int allowedStateFrom, int allowedStateTill) {
		// Wird bei geschützte Aktionen in der OnLoad der Seite aufgerufen und bricht die Navigation ab,
		// falls sich der betrachtete Visit nicht im richtigen Status befindet.

		Logger.getLogger(getClass().getSimpleName()).severe("+# redirectIfWrongState aufgerufen mit Visit " + visit.getPrim());

		if(visit.getStateVisit().ordinal() >= allowedStateFrom && visit.getStateVisit().ordinal() <= allowedStateTill)
		{
			return null; // OK, Weiterleitung darf erfolgen.
		}

		FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Diese Aktion ist im aktuellen Status des Restaurantbesuchs nicht erlaubt.", ""));

		return "VisitList?faces-redirect-true"; // Anstatt Weiterleitung Umleitung auf VisitList
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






	//////////////////////////  Methods for DataList All Visits //////////////////////////
	public boolean isUnfinished(RestaurantVisit visit)
	{
		return visit.getStateVisit().ordinal() == 0; // State.UNVOLLSTÄNDIG
	}

	public boolean isUserParticipantOf(RestaurantVisit visit)
	{
		try
		{
			if( null != allVisitsThisUser)
			{
				// Abholen aus dem Future (async)
				if ( null !=  (set = allVisitsThisUser.get()) )
				{
					// Prüft, ob das Set der Visits dieses Users den aktuell betrachteten Visit enthält.
					// Der betrachtete Visit erhält dann in der List Buttons zum Bearbeiten (und Bewerten), oder eben nicht.
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





	////////////////////////////////// Getter Setter ////////////////////////////////////////

	public MapModel getGeoModel()
	{
		return geoModel;
	}

	public void setGeoModel(MapModel geoModel)
	{
		this.geoModel = geoModel;
	}

	public String getGoogleMapsResult()
	{
		return googleMapsResult;
	}


}
