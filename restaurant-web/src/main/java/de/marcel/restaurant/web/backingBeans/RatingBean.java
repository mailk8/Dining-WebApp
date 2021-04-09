package de.marcel.restaurant.web.backingBeans;

import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import de.marcel.restaurant.ejb.model.Dish;
import de.marcel.restaurant.ejb.model.Rating;
import de.marcel.restaurant.ejb.model.RestaurantVisit;
import de.marcel.restaurant.ejb.model.User;
import org.apache.shiro.SecurityUtils;
import org.omnifaces.util.Faces;
import org.primefaces.component.chart.Chart;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearTicks;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.hbar.HorizontalBarChartDataSet;
import org.primefaces.model.charts.hbar.HorizontalBarChartModel;

import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Named
@SessionScoped
public class RatingBean implements Serializable
{

	@Inject private IRestaurantEJB appServer;
	@Inject BackingBeanVisit backingBeanVisit;
	@Inject BackingBeanUser backingBeanUser;

	private List<Dish> allDishesProxy;

	private Rating currentRating;
	private User currentUser;
	private RestaurantVisit currentVisit;

	private String retrospectVisit;
	private byte numberOfStars = 6;
	private String rating;

	private HorizontalBarChartModel myModelRest;
	private HorizontalBarChartModel myModelVisit;
	private HorizontalBarChartModel myModelUser;

	////////////////////////// OnLoad ///////////////////////////////////
	public String proxyOnLoad() {
		currentVisit = backingBeanVisit.getCurrent();
		currentUser = backingBeanUser.getCurrent();
		getAllDishes();
		generateRetrospectVisit();
		// Rating Objekt des angemeldeten Users aus dem Visit hervorholen. Falls es noch kein Rating gibt, wird eins angelegt.
		currentRating =  currentVisit.getRatingsVisit().stream()
						.filter(e -> e.getRatingUser().getPrim().equals(currentUser.getPrim()))
						.findAny().orElse(new Rating(currentVisit, currentUser));

		currentRating.setRestaurantRated(currentVisit.getRestaurantChosen());
//currentVisit.getRestaurantChosen().getAverageRating()

		myModelRest = produceHorizontalBarModel("Restaurant", 3.3f,"007bff", 0.9); // blau
		myModelVisit = produceHorizontalBarModel("Visit", 6.0f,"e53552", 0.9); // rot
		myModelUser = produceHorizontalBarModel("User", 1.46846138484183841545f,"ffc107", 0.9); // gelb

		Logger.getLogger(getClass().getSimpleName()).severe("+# nach proxyOnLoad. currentVisit ist \n+# " + currentVisit + " currentUser ist \n+# " +currentUser + " currentRating ist \n+# " + currentRating);

		return "";
	}

	private List<Dish> getAllDishes() {
		allDishesProxy = appServer.findAll(Dish.class);
		return allDishesProxy;
	}

	public List<Dish> getAllDishesProxy() {
		return allDishesProxy;
	}

	private String generateRetrospectVisit() {
		retrospectVisit = "Dein Restaurantbesuch im " + currentVisit.getRestaurantChosen().getName() + 
						" mit " + currentVisit.getParticipantsAsString() +
						" am " + currentVisit.getVisitingDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)) + "\n" +
						"Bitte bewerte dein Hauptgericht. ";

		int index = retrospectVisit.lastIndexOf(",");
		if(index > 0)
		{
			retrospectVisit = retrospectVisit.substring(0 , index) + " und " + retrospectVisit.substring(index+1, retrospectVisit.length());
		}

		return retrospectVisit;
	}





	//////////////////////// Autocompletion Dishes ///////////////////////
	public List<Dish> dishesAutocomplete(String query) {
		String queryLowerCase = query.toLowerCase();
		return allDishesProxy.stream().filter(t -> t.getDishName().toLowerCase().contains(queryLowerCase)).collect(Collectors.toList());
	}

	private String dishString;

	public Object getDishString()
	{
		return dishString;
	}

	public void setDishString(Object value) {

		Logger.getLogger(getClass().getSimpleName()).severe("+# setDishString mit value " + value);
	}



	//////////////////////// Basic Crud //////////////////////////////////
	public void saveVisitBackingBean() {

		Logger.getLogger(getClass().getSimpleName()).severe("+# saveVisitBackingBean persistiert current Visit. " + currentVisit);
		currentVisit.getRatingsVisit().add(currentRating);
		backingBeanVisit.setCurrent(currentVisit);
		backingBeanVisit.saveVisit();
	}




	/////////////////////// Star Rating //////////////////////////////////
	public String[] getStarsString() {

		String[] arr = new String[numberOfStars];
		for (Byte i = 1; i <= numberOfStars; i++)
		{
			// Strings 1 - 6 müssen 'falsch herum' in der Liste stehen,
			// d.h. höchstwertiges Element an erster Stelle
			arr[numberOfStars - i] = i.toString();
		}
		return arr;
	}

	public void setStarRating() {
		// Hier die Klasse Faces von Omnifaces gewählt, da die Parameter in FacesContext.getExternalContext.getRequestParameterMap() nicht auftauchen.
		String rating = Faces.getRequestParameterMap().get("paramStarRating");
		Logger.getLogger(getClass().getSimpleName()).severe("+# setRating mit " + rating  );
		currentRating.setStars(Byte.parseByte(rating));
		currentVisit.getRatingsVisit().add(currentRating);
		currentUser.getRatingsSubmitted().add(currentRating);
		generateReport();
	}




	/////////////////////// Statictics Diagram//////////////////////////////////
	public HorizontalBarChartModel produceHorizontalBarModel(String label, double dataValue, String noHash_HexStringColor, Double opacity) {

		Integer redByte = Integer.parseInt(noHash_HexStringColor.substring(0,2),16);
		Integer greenByte = Integer.parseInt(noHash_HexStringColor.substring(2,4),16);
		Integer blueByte = Integer.parseInt(noHash_HexStringColor.substring(4,6),16); // :)

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
		Logger.getLogger(getClass().getSimpleName()).severe("+# generateReport läuft");
		List<Byte> rest = new ArrayList<>();
		List<Byte> user = new ArrayList<>();

		// Alle historischen Ratings prüfen, hier als aktuelles Ergebnis von der DB (Wert vom Zeitpunkt onLoad wäre evtl. veraltet).
		appServer.findAll(Rating.class).stream().map(e-> (Rating) e).forEach(e -> {
			if(e.getRestaurantRated() == currentRating.getRestaurantRated())
				rest.add(e.getStars());
			if(e.getRatingUser() == currentRating.getRatingUser())
				user.add(e.getStars());
		});

		// Aktuell gesetztes Rating ist noch nicht in der DB, daher nachträglich setzen
		rest.add(currentRating.getStars());
		user.add(currentRating.getStars());

		Logger.getLogger(getClass().getSimpleName()).severe("+# generateReport nach findAll und Sortieren");

		// Durchschnittswerte berechnen
		double restMean = rest.stream().mapToInt(e->e).average().orElseGet(()->0.0);
		double userMean = user.stream().mapToInt(e->e).average().orElseGet(()->0.0);
		double visitMean = currentVisit.getRatingsVisit().stream().flatMapToInt(e -> IntStream.of(e.getStars())).average().orElseGet(()->0.0);

		Logger.getLogger(getClass().getSimpleName()).severe("+# generateReport nach Average Streaming. restMean " + restMean +" userMean " + userMean +" visitMean "+visitMean );

		// Durchschnittswerte in die Diagram-Modelle einsetzen
		((HorizontalBarChartDataSet) getMyModelRest().getData().getDataSet().get(0)).getData().set(0, restMean);
		((HorizontalBarChartDataSet) getMyModelUser().getData().getDataSet().get(0)).getData().set(0, userMean);
		((HorizontalBarChartDataSet) getMyModelVisit().getData().getDataSet().get(0)).getData().set(0, visitMean);

		Logger.getLogger(getClass().getSimpleName()).severe("+# generateReport getMyModelRest" + ((HorizontalBarChartDataSet) getMyModelRest().getData().getDataSet().get(0)).getData().get(0));
		Logger.getLogger(getClass().getSimpleName()).severe("+# getMyModelUser getMyModelUser" +((HorizontalBarChartDataSet) getMyModelUser().getData().getDataSet().get(0)).getData().get(0));
		Logger.getLogger(getClass().getSimpleName()).severe("+# generateReport getMyModelVisit" +((HorizontalBarChartDataSet) getMyModelVisit().getData().getDataSet().get(0)).getData().get(0));
		Logger.getLogger(getClass().getSimpleName()).severe("+# generateReport nach Setting im Model");

	}


	//////////////////////// Getter Setter //////////////////////////////
	public String getRating()
	{
		return rating;
	}

	public void setRating(String rating)
	{
		Logger.getLogger(getClass().getSimpleName()).severe("+# setRating mit " + rating);
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
}
