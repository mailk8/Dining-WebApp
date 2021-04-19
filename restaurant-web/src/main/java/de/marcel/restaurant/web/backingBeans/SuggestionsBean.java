package de.marcel.restaurant.web.backingBeans;

import de.marcel.restaurant.ejb.model.Address;
import de.marcel.restaurant.ejb.model.Culinary;
import de.marcel.restaurant.ejb.model.Restaurant;
import de.marcel.restaurant.ejb.model.RestaurantVisit;
import org.primefaces.event.SlideEndEvent;
import org.primefaces.event.map.MarkerDragEvent;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.map.*;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Named
@SessionScoped
public class SuggestionsBean implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Inject BackingBeanRestaurant backingBeanRestaurant;
	@Inject BackingBeanUser backingBeanUser;
	@Inject BackingBeanVisit backingBeanVisit;

	private RestaurantVisit currentVisit;
	private List<Restaurant> restaurantsRadius = new ArrayList<>();     // Zwischenergebnis nach Entferungsfilter
	private List<Restaurant> restaurantsFiltered;   // Endergebnis nach Culinaryfilter
	private int distanceSearchRadius = 20; // Standard Suchradius
	private double radius = 6371.000785; // Erdradius
	private MapModel gmapModel = new DefaultMapModel();
	private Marker marker;
	private String centerString;
	private int googleZoomLevel;
	private Circle circle;
	private StringBuilder resourcePath = new StringBuilder();

	@PostConstruct
	public void preparing() {
		resourcePath.append(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath());
		resourcePath.append("/resources/shapes/CenterMarker_grau60_43y_31x_px.svg");
	}

	/////////////////////////////////// On Load Methods //////////////////////////////////////////
	public void proxyOnLoad() {

		backingBeanRestaurant.getAllRestaurants();
		gmapModel.getMarkers().clear();
		currentVisit = backingBeanVisit.getCurrent();

		Address adr = currentVisit.getAddressVisit();
		if( adr != null && adr.getWgs84Latitude() != null && (!adr.getWgs84Latitude().equals(0.000) && !adr.getWgs84Longitude().equals(0.000)) )
		{
			// Überpringen der Treffpunktermittlung, falls eine Adresse vorhanden ist UND diese irgendwo liegt, außer an den Punkten 0.00 0.00
			// ( Es hat bereits jemand eine Treffpunkt eingegeben, es soll kein neuer ermittelt werden. )
			centerString = adr.getWgs84Latitude().toString()+", "+adr.getWgs84Longitude().toString();
		}
		else
		{
			// Berechnung eines Treffpunkts aus Addressen der User
			adr.setWgs84Latitude(0.0); adr.setWgs84Longitude(0.0);
			List<Address> locationParticipants = currentVisit.getParticipants().stream().map(e -> e.getAddressLiving()).collect(Collectors.toList());
			Address center = determineCentralPointSearch(locationParticipants);
			currentVisit.setAddressVisit(center);
			centerString = center.getWgs84Latitude().toString()+", "+center.getWgs84Longitude().toString();
		}

		restaurantsRadius = filterByRadius(backingBeanRestaurant.getAllRestaurantsProxy(), distanceSearchRadius);
		restaurantsFiltered = filterByCulinary(restaurantsRadius, currentVisit.getChosenCulinaries());

		googleZoomLevel = calculateZoomLevel();
		initMap(restaurantsFiltered);
	}




	/////////////////////////////// Proxy Methods for Eventlisteners //////////////////////////////
	public void proxyRadiusChanged(int newValue) {
		distanceSearchRadius = newValue;
		reDrawCircle(currentVisit.getAddressVisit());
		googleZoomLevel = calculateZoomLevel();
		restaurantsRadius = filterByRadius(backingBeanRestaurant.getAllRestaurantsProxy(), newValue);
		restaurantsFiltered.clear();
		restaurantsFiltered = filterByCulinary(restaurantsRadius, currentVisit.getChosenCulinaries());
	}

	public void proxyRadiusChangedSlider(SlideEndEvent event) {
		proxyRadiusChanged((int)event.getValue());
	}

	public void proxyRadiusChangedText(ValueChangeEvent event) {
		proxyRadiusChanged((int)event.getNewValue());
	}

	public void proxyCulinariesChanged(ValueChangeEvent event) {
		restaurantsFiltered.clear();
		List<Culinary> list = Arrays.asList((Culinary[])event.getNewValue());
		currentVisit.setChosenCulinaries(list);
		restaurantsFiltered = filterByCulinary(restaurantsRadius, list);
		drawMarkers(restaurantsFiltered, true);
	}

	public void proxyMarkerDrag(MarkerDragEvent event) {
		marker = event.getMarker();
		LatLng coord = marker.getLatlng();
		Address adr = currentVisit.getAddressVisit();
		adr.setWgs84Latitude(coord.getLat());
		adr.setWgs84Longitude(coord.getLng());

		reDrawCircle(adr);
		restaurantsRadius = filterByRadius(backingBeanRestaurant.getAllRestaurantsProxy(), distanceSearchRadius);
		restaurantsFiltered.clear();
		restaurantsFiltered = filterByCulinary(restaurantsRadius, currentVisit.getChosenCulinaries());
		drawMarkers(restaurantsFiltered, false);
	}





	/////////////////////////////// Methods for Restaurant Filtering ///////////////////////////////
	public List<Restaurant> filterByCulinary(List<Restaurant> restaurants, List<Culinary> matchingBucket) {
		List<Restaurant> result = restaurants.stream().filter(e -> {
			return matchingBucket.contains(e.getCulinary());
		}).collect(Collectors.toList());

		return result;
	}





	/////////////////////////////// Methods for geospatial Means ///////////////////////////////
	public List<Restaurant> filterByRadius(List<Restaurant> list, int distance) {
		// https://www.daniel-braun.com/technik/distanz-zwischen-zwei-gps-koordinaten-in-java-berchenen/
		restaurantsRadius.clear();
		double latitudeMeeting = currentVisit.getAddressVisit().getWgs84Latitude();
		double longitudeMeeting = currentVisit.getAddressVisit().getWgs84Longitude();
		double restaurantLatitude = 0;
		double resultUser = 0;

		double latitude = 0, longitude = 0, sinLat = 0, sinLong = 0, a = 0, c = 0, result = 0;
		for(Restaurant rest : list){
			result = calculateDistance(rest, latitudeMeeting, longitudeMeeting);
			if(result < distance)
			{
				rest.setDistanceMeetingPoint(result);
				restaurantsRadius.add(rest);

				resultUser = calculateDistance(rest, backingBeanUser.getCurrent().getAddressLiving().getWgs84Latitude(), backingBeanUser.getCurrent().getAddressLiving().getWgs84Longitude());
				rest.setDistanceUser(resultUser);
			}
		}

		return restaurantsRadius;
	}

	public Address determineCentralPointSearch(List<Address> addressesParticipants) {
		// https://stackoverflow.com/questions/6671183/calculate-the-center-point-of-multiple-latitude-longitude-coordinate-pairs
		// https://www.biancahoegel.de/wissen/navigation/kugelkoordinaten.html
		double x = 0, y = 0, z = 0, cl = 0, latitude = 0, longitude = 0;
		for (Address coordinate : addressesParticipants)
		{
			// Grad nach Bogenmaß
			latitude = Math.toRadians(coordinate.getWgs84Latitude());
			longitude = Math.toRadians(coordinate.getWgs84Longitude());
			// Kartesische Koordinaten werden ermittelt
			cl = radius * Math.cos(latitude);
			x += cl * Math.cos(longitude);
			y += cl * Math.sin(longitude);
			z += radius * Math.sin(latitude);
		}
		// Durchschnittswert wird berechnet
		int total = addressesParticipants.size();
		x = x / total;
		y = y / total;
		z = z / total;
		// Konvertierung zurück in Kugelkoordinaten
		double centralLongitude = Math.atan2(y, x);
		double r_new = Math.sqrt(x * x + y * y + z * z);
		double centralLatitude = Math.asin(z / r_new);

		return new Address(Math.toDegrees(centralLatitude) , Math.toDegrees(centralLongitude));
	}


	private double calculateDistance(Restaurant destination, double latitudeFrom, double longitudeFrom) {
		double restaurantLatitude = destination.getAddressRestaurant().getWgs84Latitude();
		double latitude = Math.toRadians(restaurantLatitude - latitudeFrom);
		double longitude = Math.toRadians(destination.getAddressRestaurant().getWgs84Longitude() - longitudeFrom);
		double sinLat = Math.sin(latitude / 2);
		double sinLong = Math.sin(longitude / 2);
		double a = sinLat * sinLat + Math.cos(Math.toRadians(latitudeFrom)) * Math.cos(Math.toRadians(restaurantLatitude)) * sinLong * sinLong;
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return radius * c;
	}


	//////////////////////////// Map Methods ///////////////////////////////////////////////////
	public void initMap(List<Restaurant> poiList) {
		MapModel mapModel = getGmapModel();
		mapModel.getCircles().clear();
		drawMarkers(poiList, true);
		Address adr = currentVisit.getAddressVisit();
		LatLng coord = new LatLng(adr.getWgs84Latitude(), adr.getWgs84Longitude());
		circle = new Circle(coord, distanceSearchRadius * 1000 );
		circle.setStrokeColor("#d93c3c");
		circle.setFillColor("#d93c3c");
		circle.setStrokeOpacity(0.4);
		circle.setFillOpacity(0.4);
		mapModel.addOverlay(circle);
	}

	public void reDrawCircle(Address middle) {
		getGmapModel().getCircles().clear();
		circle.setCenter(new LatLng(middle.getWgs84Latitude(), middle.getWgs84Longitude()));
		circle.setRadius(distanceSearchRadius * 1000);
		getGmapModel().addOverlay(circle);
	}

	public void onMarkerSelect(OverlaySelectEvent event) {
		marker = (Marker) event.getOverlay();
	}

	public int calculateZoomLevel() {
		// https://medium.com/google-design/google-maps-cb0326d165f5#:~:text=Google%20Maps%20has%20a%20varying,by%20256%20pixel%20square%20tile.
		// Zoomstufe = log( Erdumfang * ( 150 / RadiusKreisMeter ) / TileSize )  /  log( 2 )
		double googleTileSize = 256, earthCirc = 40075016, regulator = 150;
		return (int) Math.round(Math.log(earthCirc * ( regulator / (distanceSearchRadius * 1000) ) / googleTileSize ) / Math.log(2) );

	}

	public void drawMarkers(List<Restaurant> poiList, boolean effect) {
		MapModel mapModel = getGmapModel();
		mapModel.getMarkers().clear();
		poiList.stream().forEach((e) ->
				mapModel.addOverlay(
						new Marker(
								new LatLng(e.getAddressRestaurant().getWgs84Latitude(), e.getAddressRestaurant().getWgs84Longitude()),
								e.getName()
						))
		);

		if(effect)
			mapModel.getMarkers().stream().forEach(e-> {
				e.setAnimation(Animation.DROP);
				//e.setShadow("");
			});

		// http://kml4earth.appspot.com/icons.html
		// https://developers.google.com/maps/documentation/javascript/heatmaplayer

		Marker center = new Marker(
						new LatLng(currentVisit.getAddressVisit().getWgs84Latitude(), currentVisit.getAddressVisit().getWgs84Longitude()),
						"Zentrum der Suche", "Data-Feld",
						resourcePath.toString()
		);

		center.setDraggable(true);
		center.setId("Center");
		mapModel.addOverlay(center);
	}



	////////////////////////////////// Basic Crud //////////////////////////////////////////
	public void saveVisitBackingBean() {
		backingBeanVisit.setCurrent(currentVisit);
		backingBeanVisit.save();
	}


	////////////////////////////////// Getter Setter //////////////////////////////////////////
	public List<Restaurant> getRestaurantsFiltered()
	{
		return restaurantsFiltered;
	}

	public void setRestaurantsFiltered(List<Restaurant> restaurantsFiltered)
	{
		this.restaurantsFiltered = restaurantsFiltered;
	}

	public String getCenterString()
	{
		return centerString;
	}

	public void setCenterString(String centerString)
	{
		this.centerString = centerString;
	}

	public int getGoogleZoomLevel()
	{
		return googleZoomLevel;
	}

	public int getDistanceSearchRadius()
	{
		return distanceSearchRadius;
	}

	public void setDistanceSearchRadius(int distanceSearchRadius)
	{
		this.distanceSearchRadius = distanceSearchRadius;
	}

	public Marker getMarker() {
		return marker;
	}

	public MapModel getGmapModel()
	{
		return gmapModel;
	}

	public RestaurantVisit getCurrentVisit()
	{
		return currentVisit;
	}

	public void setCurrentVisit(RestaurantVisit currentVisit)
	{
		this.currentVisit = currentVisit;
	}
}


