package de.marcel.restaurant.web.backingBeans;


import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import de.marcel.restaurant.ejb.model.*;
import de.marcel.restaurant.web.jsfFramework.WebSocketObserver;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@Named
@SessionScoped
@ManagedBean
public class BackingBeanVisit implements Serializable
{
	private static final long serialVersionUID = 1L;
	private RestaurantVisit current = new RestaurantVisit();

	@Resource/*(name = "DefaultManagedExecutorService") */ManagedExecutorService executor;
	@Inject private IRestaurantEJB appServer;
	@Inject private BackingBeanUser backingBeanUser;
	@Inject private WebSocketObserver websocket;

	private String sizeParticipantsForValidator;
	private List<RestaurantVisit> visitList;
	private List<Culinary> allCulinariesProxy;
	private String zoneString;
	private Future<HashSet<Integer>> allVisitsThisUser;
	private HashSet<Integer> set;
	private MapModel geoModel;
	private String googleMapsResult;
	private Logger log = Logger.getLogger(this.getClass().getSimpleName());

	public void init() {
		log.severe("+# init laeuft");
		geoModel = new DefaultMapModel();
	}

	public void proxyOnLoad() {
		log.severe("+# proxyOnLoad laeuft");
		getAllVisits().forEach(e -> e.setStateVisit(State.UNVOLLSTÄNDIG));
		visitList.stream().forEach(e -> updateVisitState(e));
	}

	////////////////////////////////// Methods for Restaurant & Culinary Selection //////////////////////////////
	public Culinary[] getCulinariesArray()
	{log.severe("+# getCulinariesArray laeuft");
		return current.getChosenCulinaries().stream().toArray(Culinary[]::new);
	}

	public void setCulinariesArray(Culinary[] culinariesArray)
	{log.severe("+# setCulinariesArray laeuft mit " + culinariesArray);
		current.setChosenCulinaries(Arrays.asList(culinariesArray));
	}

	public List<Culinary> getAllCulinariesProxy()
	{log.severe("+# getAllCulinariesProxy laeuft");
		if(null == allCulinariesProxy)
		{
			allCulinariesProxy = getAllCulinaries();
		}
		return allCulinariesProxy;
	}

	public Integer getAvgRating(Restaurant r)
	{log.severe("+# getAvgRating laeuft");
		return BigDecimal.valueOf(r.getAvgRating()).intValue();
	}


	////////////////////////////////// Methods for Location of Search //////////////////////////////
	public void setGoogleMapsResult(String googleApiReturn)
	{log.severe("+# setGoogleMapsResult laeuft");
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
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Restaurants werden gesucht in " + resultSplit[2] + " " + resultSplit[1] + ".",  ""));
					return;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		FacesContext.getCurrentInstance().addMessage("place",
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Zu dieser Eingabe wurde kein Ort gefunden.",  ""));
	}




	//////////////////////////  Methods for Fetching & Performane //////////////////////////
	public void fetchVisitsForUser()
	{log.severe("+# fetchVisitsForUser laeuft");
		User user = backingBeanUser.getCurrent();
		if( null != user )
		{log.severe("+# fetchVisitsForUser executor wird angesprochen");
			allVisitsThisUser = executor.submit(()-> {
				log.severe("+# executor läuft, findAllVisitsForUser ergibt: " + appServer.findAllVisitsForUser(user));
				return appServer.findAllVisitsForUser(user);
			});
		}
		else
		{
			allVisitsThisUser = null;
		}
	}

	public void prepareGetAllCulinaries()
	{log.severe("+# prepareGetAllCulinaries laeuft");
		allCulinariesProxy = getAllCulinaries();
	}

	public List<Culinary> getAllCulinaries()
	{log.severe("+# getAllCulinaries laeuft");
		return appServer.findAll(Culinary.class);
	}

	public List<RestaurantVisit> getAllVisits()
	{
		appServer.clearCache(RestaurantVisit.class);
		visitList = appServer.findAll(RestaurantVisit.class);
		visitList.sort((a,b) -> b.getVisitingDateTime().compareTo(a.getVisitingDateTime()));
		//visitList.forEach((e) -> updateVisitState(e.ra));
		fetchVisitsForUser();
		return  visitList;
	}

	public List<RestaurantVisit> getAllVisitsProxy()
	{log.severe("+# getAllVisitsProxy laeuft");
		return visitList;
	}





	//////////////////////////  Methods for Visit Functions //////////////////////////
	public void updateVisitState(RestaurantVisit visit)
	{log.severe("+# updateVisitState laeuft");
		int switchVar = visit.getStateVisit().ordinal();

		switch (switchVar)
		{
			case 0: {
				if( (visit.getParticipants().size() >= 1) && (visit.getVisitingDateTime().isAfter(LocalDateTime.of(2000,01,01,01,01))))
					visit.setStateVisit(State.ANGELEGT); // 1
			}
			case 1:{
				if(visit.getRestaurantChosen() != null && visit.getStateVisit().ordinal() == 1)
					visit.setStateVisit(State.GEPLANT); // 2
			}
			case 2:{
				if((ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault()))
					.isAfter(ZonedDateTime.of(visit.getVisitingDateTime(), ZoneId.of(visit.getTimezoneString())))
								&& visit.getStateVisit().ordinal() == 2)
					visit.setStateVisit(State.BEWERTBAR); // 3
			}
			case 3:{
				if(visit.getParticipants() != null && visit.getRatings() != null && visit.getRatings().size() > 0 &&
					(visit.getParticipants().size() >= visit.getRatings().size()) && visit.getStateVisit().ordinal() == 3)
					visit.setStateVisit(State.BEWERTUNG_AUSSTEHEND); // 4
			}
			case 4:{
				if(visit.getParticipants() != null && visit.getRatings() != null &&
					 visit.getParticipants().size() == visit.getRatings().size() && visit.getStateVisit().ordinal() == 4)
					visit.setStateVisit(State.BEWERTET); // 5
			}
		}
	}

	public String rate(RestaurantVisit u)
	{log.severe("+# rate laeuft");
		this.current = u;
		return "VisitRating?faces-redirect=true";
	}




	//////////////////////////  Methods for Basic Crud & Navigation //////////////////////////
	public String save() {
		updateVisitState(current);
		if(null == current.getPrim())
		{
			insert(current);
		}
		else
		{
			update(current);
		}

		websocket.sendMessage(RestaurantVisit.class);

		return "VisitList?faces-redirect=true";
	}

	public String proxySaveVisit() {
		save();
		return "VisitList?faces-redirect=true";
	}

	public String saveVisitNext() {
		save();
		return "VisitSuggestions?faces-redirect=true";
	}

	public void insert(RestaurantVisit u) {
		int result = appServer.persist(u);
		u.setPrim(result);
	}

	public void update(RestaurantVisit u)
	{log.severe("+# getCulinariesArray laeuft");
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

		appServer.delete(u);
		websocket.sendMessage(RestaurantVisit.class);
		return "VisitList?faces-redirect=true";
	}

	public String createNew()
	{log.severe("+# createNew: Neuen Restaurantbesuch planen");
		current = new RestaurantVisit();
		current.setTimezoneString(zoneString);
		return "VisitCreate?faces-redirect=true";
	}

	public void setCurrent(RestaurantVisit u)
	{log.severe("+# setCurrent laeuft");
		this.current = u;
	}

	public RestaurantVisit getCurrent()
	{log.severe("+# getCurrent laeuft");
		return current;
	}

	public String redirectIfWrongState(int allowedStateFrom, int allowedStateTill) {
		return redirectIfWrongState(current, allowedStateFrom, allowedStateTill);
	}

	public String redirectIfWrongState(RestaurantVisit visit, int allowedStateFrom, int allowedStateTill) {
		// Wird bei geschützte Aktionen in der OnLoad der Seite aufgerufen und bricht die Navigation ab,
		// falls sich der betrachtete Visit nicht im richtigen Status befindet.
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
	{log.severe("+# getSizeParticipantsForValidator laeuft:" + sizeParticipantsForValidator);
		sizeParticipantsForValidator = current.getParticipants().size()+"";
		return sizeParticipantsForValidator;
	}

	public void setSizeParticipantsForValidator(String sizeParticipantsForValidator)
	{log.severe("+# setSizeParticipantsForValidator laeuft mit " + sizeParticipantsForValidator);
		this.sizeParticipantsForValidator = sizeParticipantsForValidator;

	}







	//////////////////////////  Methods for Date Time Functions //////////////////////////
	public Set<String> getAllTimezones()
	{log.severe("+# getCulinariesArray laeuft");
		return new TreeSet<String>(ZoneId.getAvailableZoneIds());
	}

	public List<String> completeText(String query) {
		String queryLowerCase = query.toLowerCase();
		return getAllTimezones().stream().filter(t -> t.toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
	}

	public String getZoneString()
	{log.severe("+# getCulinariesArray laeuft");
		return zoneString;
	}

	public void setZoneString(String zoneString)
	{log.severe("+# getCulinariesArray laeuft");
		this.zoneString = zoneString;
	}






	//////////////////////////  Methods for DataList All Visits //////////////////////////
	public boolean isUnfinished(RestaurantVisit visit)
	{
		return visit.getStateVisit().ordinal() == 0; // State.UNVOLLSTÄNDIG
	}

	public boolean isUserParticipantOf(RestaurantVisit visit)
	{log.severe("+# isUserParticipantOf laeuft");
		try
		{
			if( null != allVisitsThisUser )
			{
				// Abholen aus dem Future (async)
				if ( null !=  (set = allVisitsThisUser.get()) )
				{log.severe("+# allVisitsThisUser ist nicht null");
					// Prüft, ob das Set der Visits dieses Users den aktuell betrachteten Visit enthält.
					// Der betrachtete Visit erhält dann in der List Buttons zum Bearbeiten (und Bewerten), oder eben nicht.
					log.severe("+# user " + backingBeanUser.getCurrent() + " ist Teilnehmer in Visit vom " + visit.getVisitingDateTime());
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
	{log.severe("+# getCulinariesArray laeuft");
		return geoModel;
	}

	public void setGeoModel(MapModel geoModel)
	{log.severe("+# getCulinariesArray laeuft");
		this.geoModel = geoModel;
	}

	public String getGoogleMapsResult()
	{log.severe("+# getCulinariesArray laeuft");
		return googleMapsResult;
	}

}
