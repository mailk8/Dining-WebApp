package de.marcel.restaurant.web.backingBeans;

import de.marcel.restaurant.ejb.model.Address;
import de.marcel.restaurant.ejb.model.Culinary;
import de.marcel.restaurant.ejb.model.Restaurant;
import de.marcel.restaurant.ejb.model.RestaurantVisit;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.SessionScoped;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/*

Injectables
	BackingBeanVisit
	BackingBeanRestaurant

Benötigte Objekte
	Teilnehmer User: Unabänderbar aus Prozessschritt Create Visit
	Alle Restaurants: Bei Pageload Liste in BbVisit aktualisieren, Liste vom Getter holen
			Liste scannen und Treffer für gewählten Umkreis mit Entferung in TreeMap einfügen?
			Geht wohl nicht, da View eine List erwartet.
			Die Elemente können nur nach ihren Attributen sortiert werden, Entfernung müsste daher als transientes Feld in Restaurant eingefügt werden.

	Kulinarik Matchingbucket
			Initial befüllt durch teilnehmende User.
			Muss dynamisch gefüllt und aktualisiert werden, da das Dropdown in der View Suggestions Änderungen zulässt.

Variablen, sonstige
	Entfernungsradius double, dynamisch änderbar bei Userinteraktion


Verhalten
	Bei allen Änderungen Prozess komplett neu anstoßen wäre Verschwendugn und nicht performant.
	Daher werden Einzelschritte benötigt, die je nach Änderung (Neuer Ort, Neue Kulinaries, Neuer Umkreis) anstoßbar sind und zu einer neuen Restaurant Liste führen.

	Exceptions
		Was passiert wenn ich alle Kulinaries entferne
		Was passiert bei neuer manueller Ortsvorgabe
		Was passiert, wenn ein Teilnehmemr plötzlich nicht mehr existiert (gelöscht)?

Funktionen
	Schnittpunkt berechnen (Durchschnitt aus Koordinaten der Teilnehmer)
		Ausbau: http://www.geomidpoint.com/ https://rechneronline.de/geo-koordinaten/durchschnitt.php und http://www.jennessent.com/downloads/graphics_shapes_poster_full.pdf
		Was passiert, wenn der im Wasser landet? Dann sind keine Restaurants in der Nähe. Der Radius sollte dann erhöht werden und eine Faces Message geworfen werden.

	Entfernungsradius von UI entgegennehmen
	Entfernung für Restaurants berechnen, (Kompf naive Algorithmus, COS korrigiert)
		bei Treffern im Umreis speichern, Rest verwerfen? ( evtl. benötigt man noch weitere für "mehr" oder Umkreisänderung )
	Matching mit Kulinarikbucket, Ergebnis bilden oder aktualisieren


Offene Punkte
	Ort: Normalerweise werden die Orte als Addressen gehandelt. Visit hat eine Addresse, die wird dann für den Treffpunkt / Schnittpunkt missbraucht ?
		Wenn bereits bei Visit Create vorgegeben,  keinen Schnittpunkt errechnen? Mit welchen Koordinaten, durch WgsClient ermitteln?
		WgsCliet könnte generisch hinsichtlich der BaseEntity sein und nur mit setAddress arbeiten. Dazu müssten alle Addressen aber gleiche Variablennamen haben.
		Vorgabe in Visit Create sollte Straße, Hausnummer, Ortsteil (schwierig !), Koordinaten vorsehen.
		Oder Auswahl über Google Maps in Visit Suggestions. Dazu müsste etwas von der JS Api von Google zurückkommen... https://www.primefaces.org/showcase/ui/data/gmap/addMarkers.xhtml?jfwid=4ea20

	User: Gibt es die Teilnehmer noch?
		Evtl. auch vorher im Prozess bei Auswahl.

		User verschwinden klammheimlich aus den Visits. Wenn ein Teilnehmer sich in einen Visit einträgt und dann den User löscht, steht er zunächst
		einmal weiterhin als TN im Visit. Auch in einer neuen Session und Reload von der DB durch Seitenaufruf (??).
		Nach Redeploy ist er jedoch verschwunden. Ohne Exception.
		Aus der JoinTable verschwindet er umgehend, bei Löschung.

		Sollzustand: User wird beim Löschen auch aus Visit entfernt.

 */
@Named
@SessionScoped
@ManagedBean
public class SuggestionsBean implements Serializable
{

	@Resource(name = "DefaultManagedExecutorService") ManagedExecutorService executor;
	@Inject BackingBeanRestaurant backingBeanRestaurant;
	@Inject BackingBeanVisit backingBeanVisit;
	private RestaurantVisit currentVisit;

	private List<Restaurant> restaurantsRadius = new ArrayList<>();     // Zwischenergebnis nach Entferungsfilter
	private List<Restaurant> restaurantsFiltered;   // Endergebnis nach Culinaryfilter

	private int defaultDistance = 20; // Standardentfernung
	private double radius = 6371.000785; // Erdradius

	private MapModel gmapModel = new DefaultMapModel();
	private Marker marker;

	/////////////////////////////////// On Load Methods //////////////////////////////////////////


	// soll asynchron aufgerufen werden
	public void proxyOnLoad() {
//		currentVisit = backingBeanVisit.getCurrent();
//		gmapModel.getMarkers().clear();
//      backingBeanRestaurant.getAllRestaurants();

		Future<List<Restaurant>> list = executor.submit(()-> {
			Logger.getLogger(getClass().getSimpleName()).severe("+# proxyOnLoad async läuft, backingBeanRestaurant ist " + backingBeanRestaurant);
			return backingBeanRestaurant.getAllRestaurants();
		});

		executor.submit(()->
		{
			Logger.getLogger(getClass().getSimpleName()).severe("+# proxyOnLoad async läuft, backingBeanVisit ist " + backingBeanVisit);
			currentVisit = backingBeanVisit.getCurrent();

			gmapModel.getMarkers().clear();


			Address adr = currentVisit.getAddressVisit();
			if( adr != null && adr.getWgs84Latitude() != null && (adr.getWgs84Latitude().equals(0.000) && adr.getWgs84Longitude().equals(0.000)) )
			{
				// Überpringen der Treffpunktermittlung, falls eine Adresse vorhanden ist UND diese irgendwo liegt, außer an den Punkten 0.00 0.00
				// Übersetzt: Es hat bereits jemand eine Treffpunkt eingegeben, es soll kein neuer ermittelt werden.

				Logger.getLogger(getClass().getSimpleName()).severe("+# proxyOnLoad Überpringen der Treffpunktermittlung ");
			}
			else
			{
				// Berechnung eines Treffpunkts aus Addressen der User
				adr.setWgs84Latitude(0.0); adr.setWgs84Longitude(0.0);
				List<Address> addressesParticipants = currentVisit.getParticipants().stream().map(e -> e.getAddressActual()).collect(Collectors.toList());
				Address ad = determineCentralPoint(addressesParticipants);
				currentVisit.setAddressVisit(ad);
				Logger.getLogger(getClass().getSimpleName()).severe("+# proxyOnLoad Berechnung eines Treffpunkts: " + ad);
			}

			Logger.getLogger(getClass().getSimpleName()).severe("+# proxyOnLoad  getAllRestaurantsProxy " + backingBeanRestaurant.getAllRestaurantsProxy());
			restaurantsRadius = filterByRadius(backingBeanRestaurant.getAllRestaurantsProxy(), defaultDistance);
			Logger.getLogger(getClass().getSimpleName()).severe("+# proxyOnLoad filterByRadius ergebnis: " + restaurantsRadius);

			restaurantsFiltered = filterByCulinary(restaurantsRadius, currentVisit.getChosenCulinaries());
			Logger.getLogger(getClass().getSimpleName()).severe("+# proxyOnLoad filterByCulinary ergebnis: " + restaurantsFiltered);


			initMap(restaurantsFiltered);
		}
		);
	}

	/////////////////////////////// Proxy Methods for Eventlisteners //////////////////////////////

	public void proxyCentralPointChanged(double lat, double lon) {
		// Auf dem UI wurde ein Neuer Treffpunkt gesetzt
		// es soll NICHT aus Usern ermittelt werden

	}

	public void proxyRadiusChanged(int distanceVomUiOderAusBeanEinsammeln) {
		filterByRadius(backingBeanRestaurant.getAllRestaurantsProxy(), distanceVomUiOderAusBeanEinsammeln);
	}

	public void proxyCulinariesChanged() {}

	/////////////////////////////// Methods for Restaurant Filtering ///////////////////////////////

	public List<Restaurant> filterByCulinary(List<Restaurant> restaurants, List<Culinary> matchingBucket) {
		return restaurants.stream().filter(e -> {
			Logger.getLogger(getClass().getSimpleName()).severe("+# filterByCulinary MatchingBucket enthält  " + matchingBucket);
			Logger.getLogger(getClass().getSimpleName()).severe("+# filterByCulinary getestet wird  " + e.getCulinary());
			Logger.getLogger(getClass().getSimpleName()).severe("+# filterByCulinary Ergebnis contains?  " + matchingBucket.contains(e.getCulinary()));

			return matchingBucket.contains(e.getCulinary());
		}).collect(Collectors.toList());
	}

	/////////////////////////////// Methods for geospatial Means ///////////////////////////////

	public List<Restaurant> filterByRadius(List<Restaurant> list, int distance) {
		// https://www.daniel-braun.com/technik/distanz-zwischen-zwei-gps-koordinaten-in-java-berchenen/
		restaurantsRadius.clear();
		double latitudeMeeting = currentVisit.getAddressVisit().getWgs84Latitude();
		double longitudeMeeting = currentVisit.getAddressVisit().getWgs84Longitude();
		double restaurantLatitude = 0;

		Logger.getLogger(getClass().getSimpleName()).severe("+# filterByRadius für Treffpunkt " + latitudeMeeting +" " + longitudeMeeting);

		double latitude = 0, longitude = 0, sinLat = 0, sinLong = 0, a = 0, c = 0, result = 0;
		for(Restaurant rest : list){
			restaurantLatitude = rest.getAddressRestaurant().getWgs84Latitude();
			latitude = Math.toRadians(restaurantLatitude - latitudeMeeting);
			longitude = Math.toRadians(rest.getAddressRestaurant().getWgs84Longitude() - longitudeMeeting);
			sinLat = Math.sin(latitude / 2);
			sinLong = Math.sin(longitude / 2);
			a = sinLat * sinLat + Math.cos(Math.toRadians(latitudeMeeting)) * Math.cos(Math.toRadians(restaurantLatitude)) * sinLong * sinLong;
			c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
			result = radius * c;
			Logger.getLogger(getClass().getSimpleName()).severe("+# filterByRadius Restaurant ermittelte Entfernung " + result);
			if(result < distance)
			{
				Logger.getLogger(getClass().getSimpleName()).severe("+# filterByRadius Restaurant : " + rest.getName() + " liegt in " + distance + " km Umreis zu Treffpunkt");
				rest.setDistanceMeetingPoint(result);
				restaurantsRadius.add(rest);
			}
			else
				Logger.getLogger(getClass().getSimpleName()).severe("+# filterByRadius Restaurant : " + rest.getName() + " liegt NICHT in " + distance + " km Umreis zu Treffpunkt");
		}

		return restaurantsRadius;
	}

	public Address determineCentralPoint(List<Address> addressesParticipants) {

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


	//////////////////////////// Gmap Methods ///////////////////////////////////////////////////
	public void initMap(List<Restaurant> poiList) {

		poiList.stream().forEach((e) ->
						getGmapModel().addOverlay(
										new Marker(
														new LatLng(e.getAddressRestaurant().getWgs84Latitude(), e.getAddressRestaurant().getWgs84Longitude()),
														e.getName()
										))
		);

		//		//Shared coordinates
		//		LatLng coord1 = new LatLng(36.879466, 30.667648);
		//
		//		//Icons and Data
		//		advancedModel.addOverlay(new Marker(coord1, "Konyaalti", "BildInfoPopup.png", "Icon für Mapsdarstellung  https://maps.google.com/mapfiles/ms/micons/blue-dot.png"));
	}


	public void onMarkerSelect(OverlaySelectEvent event) {
		marker = (Marker) event.getOverlay();
	}

	public Marker getMarker() {
		return marker;
	}

	public MapModel getGmapModel()
	{
		return gmapModel;
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
}

//@ViewScoped
