package de.marcel.restaurant.web.backingBeans;

import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import de.marcel.restaurant.ejb.model.Dish;
import de.marcel.restaurant.ejb.model.Rating;
import de.marcel.restaurant.ejb.model.RestaurantVisit;
import de.marcel.restaurant.ejb.model.User;
import org.apache.shiro.SecurityUtils;
import org.omnifaces.util.Faces;
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

		myModelRest = produceHorizontalBarModel("Visit", 2.8,"007bff", 0.9);
		myModelVisit = produceHorizontalBarModel("Restaurant", 2.8,"e53552", 0.9);
		myModelUser = produceHorizontalBarModel("User", 2.8,"ffc107", 0.9);

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
	public void setStarRating() {
		// Hier die Klasse Faces von Omnifaces gewählt, da die Parameter in FacesContext.getExternalContext.getRequestParameterMap() nicht auftauchen.
		String rating = Faces.getRequestParameterMap().get("paramStarRating");
		Logger.getLogger(getClass().getSimpleName()).severe("+# setRating mit " + rating  );
		currentRating.setStars(Byte.parseByte(rating));
	}

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


	/////////////////////// Statictics Diagram//////////////////////////////////

	public HorizontalBarChartModel produceHorizontalBarModel(String label, double dataValue, String noHashHexStringColor, Double opacity) {

		Integer redByte = Integer.parseInt(noHashHexStringColor.substring(0,2),16);
		Integer greenByte = Integer.parseInt(noHashHexStringColor.substring(2,4),16);
		Integer blueByte = Integer.parseInt(noHashHexStringColor.substring(4,6),16); // :)

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

		///////////////////////////// Farbe Bars //////////////////////////////
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
		//        Title title = new Title();
		//        title.setDisplay(true);
		//        title.setText("Horizontal Bar Chart");
		//        options.setTitle(title);
		model.setOptions(options);
		// h:outputscript enthält weitere Einstellmöglichkeiten
		model.setExtender("chartExtender");

		return model;
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
