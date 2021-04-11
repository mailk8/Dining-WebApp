package de.marcel.restaurant.web.backingBeans;

import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import de.marcel.restaurant.ejb.model.*;
import org.omnifaces.util.Faces;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearTicks;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.hbar.HorizontalBarChartDataSet;
import org.primefaces.model.charts.hbar.HorizontalBarChartModel;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Named
@SessionScoped
public class RatingBean implements Serializable
{
	@Inject private IRestaurantEJB appServer;
	@Inject private BackingBeanVisit backingBeanVisit;
	@Inject private BackingBeanUser backingBeanUser;
	@Inject private BackingBeanRestaurant backingBeanRestaurant;

	private List<Dish> allDishesProxy;

	private Rating currentRating;
	private User currentUser;
	private RestaurantVisit currentVisit;
	private Restaurant currentRestaurant;

	private String retrospectVisit;

	private byte numberOfStars = 6;
	private String rating;

	private HorizontalBarChartModel myModelRest;
	private HorizontalBarChartModel myModelVisit;
	private HorizontalBarChartModel myModelUser;

	private String nameRest, namesUser, date, ratingsOutOf;

	////////////////////////// OnLoad ///////////////////////////////////
	public void proxyOnLoad() {
		currentVisit = backingBeanVisit.getCurrent();
		currentUser = backingBeanUser.getCurrent();
		currentRestaurant = currentVisit.getRestaurantChosen();
		getAllDishes();
		generateRetrospectVisit();

		// Rating Objekt des angemeldeten Users aus dem Visit hervorholen. Falls es noch kein Rating gibt, wird es angelegt.
		currentRating =  currentVisit.getRatings().stream()
						.filter(e -> e.getRatingUser().getPrim().equals(currentUser.getPrim()))
						.findAny().orElse(new Rating(currentVisit, currentUser));

		currentRating.setRestaurantRated(currentRestaurant);

		myModelRest = produceHorizontalBarModel("Restaurant", 0,"007bff", 0.9); // blau
		myModelVisit = produceHorizontalBarModel("Visit", 0,"e53552", 0.9); // rot
		myModelUser = produceHorizontalBarModel("User", 0,"ffc107", 0.9); // gelb

		if(currentRating.getStars() != 0)
			generateReport();


	}


	private List<Dish> getAllDishes() {
		allDishesProxy = appServer.findAll(Dish.class);
		return allDishesProxy;
	}

	public List<Dish> getAllDishesProxy() {
		return allDishesProxy;
	}

	private String generateRetrospectVisit() {

		nameRest = currentRestaurant.getName();
		namesUser = currentVisit.getParticipantsAsString(currentUser);
		date = currentVisit.getVisitingDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG));



		ratingsOutOf = currentVisit.getRatings().size()+ " von "+currentVisit.getParticipants().size();




		retrospectVisit = "Zu deinem Besuch im Restaurant " + nameRest + namesUser + " am " + date + " " +
						"sind "+ratingsOutOf+" Bewertungen eingegangen. \n" +
						"Bitte bewerte jetzt dein Hauptgericht.";

		int index = retrospectVisit.lastIndexOf(",");
		if(index > 0)
			retrospectVisit = " mit " +retrospectVisit.substring(0 , index) + " und " + retrospectVisit.substring(index+1, retrospectVisit.length()) + ". ";


		return retrospectVisit;
	}





	//////////////////////// Autocompletion Dishes ///////////////////////
	public List<Dish> dishesAutocomplete(String query) {
		String queryLowerCase = query.toLowerCase();
		return allDishesProxy.stream().filter(t -> t.getDishName().toLowerCase().contains(queryLowerCase))
						.sorted()
						.collect(Collectors.toList());
	}

	private String dishString;

	public Object getDishString()
	{
		return dishString;
	}

	public void setDishString(Object value) {

	}



	//////////////////////// Basic Crud //////////////////////////////////
	public void saveVisitBackingBean() {

		if(currentRating.getPrim() != null)
		{
			appServer.update(currentRating);
			currentVisit.getRatings().remove(currentRating);
			currentVisit.getRatings().add(currentRating);
		}
		else
		{
			int i = appServer.persist(currentRating);
			currentRating.setPrim(i);
			currentVisit.getRatings().add(currentRating);
		}

		/*
		Vermutlich ist das Rating für den Entitymanager auch nach dem expliziten Persist oben immer noch ein neues Objekt.
		Man muss evtl. den Primärschlüssel setzen, der von der DB zurückkommt, damit er es merkt, dass es kein neues o ist
		und dies den backingBeans bekannt machen?
		 */
		backingBeanVisit.setCurrent(currentVisit);
		backingBeanRestaurant.setCurrent(currentRestaurant);
		backingBeanVisit.save();
		backingBeanRestaurant.save();
		backingBeanUser.saveUser(currentUser);
	}




	/////////////////////// Star Rating //////////////////////////////////
	public String[] getStarsString() {
		// Produziert Strings 1 - N für RadioButtons
		String[] arr = new String[numberOfStars];
		for (Byte i = 1; i <= numberOfStars; i++)
		{
			// Strings 1 - N müssen 'falsch herum' in der Liste stehen,
			// d.h. höchstwertiges Element an erster Stelle
			arr[numberOfStars - i] = i.toString();
		}
		return arr;
	}

	public void setStarRating() {
		// Star Rating wird als Http Request-Parameter übertragen.
		String rating = Faces.getRequestParameterMap().get("paramStarRating");
		currentRating.setStars(Byte.parseByte(rating));
		currentVisit.getRatings().add(currentRating);
		currentUser.getRatings().add(currentRating);
		currentRestaurant.getRatings().add(currentRating);
		generateReport();
	}




	/////////////////////// Statictics Diagram//////////////////////////////////
	public HorizontalBarChartModel produceHorizontalBarModel(String label, double dataValue, String noHash_HexStringColor, Double opacity) {

		Integer redByte = Integer.parseInt(noHash_HexStringColor.substring(0,2),16);
		Integer greenByte = Integer.parseInt(noHash_HexStringColor.substring(2,4),16);
		Integer blueByte = Integer.parseInt(noHash_HexStringColor.substring(4,6),16);

		///////////////////////////// Boilerplate //////////////////////////////
		HorizontalBarChartModel model = new HorizontalBarChartModel();
		ChartData data = new ChartData();
		HorizontalBarChartDataSet hbarDataSet = new HorizontalBarChartDataSet();


		///////////////////////////// Farb-Legende Bars ////////////////////////
		hbarDataSet.setLabel(label); // u.a. Popup Label

		///////////////////////////// Daten ////////////////////////////////////
		List<Number> values = new ArrayList<>();
		values.add(dataValue);
		hbarDataSet.setData(values);
		//hbarDataSet.getData()
		//((HorizontalBarChartDataSet) model.getData().getDataSet().get(0)).getData().set(0, newValue);

		///////////////////////////// Farbe Bars /////////////////////
		List<String> bgColor = new ArrayList<>();
		bgColor.add("rgba("+redByte+", "+greenByte+", "+blueByte+", "+opacity+")"); // gelb
		hbarDataSet.setBackgroundColor(bgColor);


		///////////////////////////// Outline Bars //////////////////////////////
		List<String> borderColor = new ArrayList<>();
		borderColor.add("rgb(201, 203, 207)");
		hbarDataSet.setBorderColor(borderColor);
		hbarDataSet.setBorderWidth(0);

		///////////////////////////// Labels Ordinate (Y) ////////////////////////
		List<String> labels = new ArrayList<>();
		labels.add("  ");

		///////////////////////////// Zusammenfügen //////////////////////////////
		data.addChartDataSet(hbarDataSet);
		data.setLabels(labels);
		model.setData(data);

		///////////////////////////// Optionen //////////////////////////////
		BarChartOptions options = new BarChartOptions();
		CartesianScales cScales = new CartesianScales();

		CartesianLinearAxes linearAxes = new CartesianLinearAxes();
		linearAxes.setOffset(false);
		//linearAxes.setStacked(true);

		CartesianLinearTicks ticks = new CartesianLinearTicks();
		ticks.setBeginAtZero(true);
		ticks.setMax(6.0);
		ticks.setMin(0.0);
		ticks.setStepSize(1.0);

		linearAxes.setTicks(ticks);
		cScales.addXAxesData(linearAxes);
		options.setScales(cScales);
		options.setBarThickness(1);
		//        Title title = new Title();
		//        title.setDisplay(true);
		//        title.setText("Horizontal Bar Chart");
		//        options.setTitle(title);
		model.setOptions(options);
		// h:outputscript enthält weitere Einstellmöglichkeiten
		model.setExtender("chartExtender");

		return model;
	}

	public void generateReport() {
		// Ermittelt Durchschnittswerte für Bar Model
		Set<Rating> rest = new HashSet<>();
		Set<Rating> user = new HashSet<>();

		// Alle historischen Ratings prüfen, hier als aktuelles Ergebnis von der DB
		// Verzicht auf Rechnen mit aggregierten Werten.
		// Vorteil: Avg wird immer einzeln berechnet und stützt sich nicht auf alte Ergebnisse.

		appServer.findAll(Rating.class).stream().map(e-> (Rating) e).forEach(e -> {
			if(e.getRestaurantRated().getPrim() == currentRating.getRestaurantRated().getPrim())
				rest.add(e);
			if(e.getRatingUser().getPrim() == currentRating.getRatingUser().getPrim())
				user.add(e);
		});


		if(user.add(currentRating))
		{
			// Aktuell gesetztes Rating ist noch nicht in der DB, daher nachträglich setzen
			// (User will sein Rating erstmalig erstellen)
			rest.add(currentRating);
		}
		else
		{
			// currentRating ist bereits im Set, kommt also mit einem alten Wert von der DB
			// (User will sein Rating ändern)
			rest.remove(currentRating);
			user.remove(currentRating);
			rest.add(currentRating);
			user.add(currentRating);
		}

		// Durchschnittswerte berechnen
		float restMean = (float) rest.stream().mapToInt(e->e.getStars()).average().orElseGet(()->0.0);
		float userMean = (float) user.stream().mapToInt(e->e.getStars()).average().orElseGet(()->0.0);
		float visitMean = (float) currentVisit.getRatings().stream().flatMapToInt(e -> IntStream.of(e.getStars())).average().orElseGet(()->0.0);

		// Durchschnittswerte in die Diagram-Modelle einsetzen
		((HorizontalBarChartDataSet) getMyModelRest().getData().getDataSet().get(0)).getData().set(0, restMean);
		((HorizontalBarChartDataSet) getMyModelUser().getData().getDataSet().get(0)).getData().set(0, userMean);
		((HorizontalBarChartDataSet) getMyModelVisit().getData().getDataSet().get(0)).getData().set(0, visitMean);

		// Durchschnittswerte in Rest. setzen
		currentRestaurant.setAvgRating(restMean);

	}



	//////////////////////// Getter Setter //////////////////////////////
	public String getRating()
	{
		return rating;
	}

	public void setRating(String rating)
	{
		this.rating = rating;
	}

	public IRestaurantEJB getAppServer()
	{
		return appServer;
	}

	public void setAppServer(IRestaurantEJB appServer)
	{
		this.appServer = appServer;
	}

	public void setAllDishesProxy(List<Dish> allDishesProxy)
	{
		this.allDishesProxy = allDishesProxy;
	}

	public String getRetrospectVisit()
	{
		return retrospectVisit;
	}

	public void setRetrospectVisit(String retrospectVisit)
	{
		this.retrospectVisit = retrospectVisit;
	}

	public Rating getCurrentRating()
	{
		return currentRating;
	}

	public void setCurrentRating(Rating currentRating)
	{
		this.currentRating = currentRating;
	}

	public User getCurrentUser()
	{
		return currentUser;
	}

	public void setCurrentUser(User currentUser)
	{
		this.currentUser = currentUser;
	}

	public RestaurantVisit getCurrentVisit()
	{
		return currentVisit;
	}

	public void setCurrentVisit(RestaurantVisit currentVisit)
	{
		this.currentVisit = currentVisit;
	}

	public byte getNumberOfStars()
	{
		return numberOfStars;
	}

	public void setNumberOfStars(byte numberOfStars)
	{
		this.numberOfStars = numberOfStars;
	}

	public HorizontalBarChartModel getMyModelRest()
	{
		return myModelRest;
	}

	public void setMyModelRest(HorizontalBarChartModel myModelRest)
	{
		this.myModelRest = myModelRest;
	}

	public HorizontalBarChartModel getMyModelVisit()
	{
		return myModelVisit;
	}

	public void setMyModelVisit(HorizontalBarChartModel myModelVisit)
	{
		this.myModelVisit = myModelVisit;
	}

	public HorizontalBarChartModel getMyModelUser()
	{
		return myModelUser;
	}

	public void setMyModelUser(HorizontalBarChartModel myModelUser)
	{
		this.myModelUser = myModelUser;
	}

	public String getNameRest()
	{
		return nameRest;
	}
}
