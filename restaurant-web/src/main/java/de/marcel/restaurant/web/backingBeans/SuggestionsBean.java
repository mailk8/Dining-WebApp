package de.marcel.restaurant.web.backingBeans;

import de.marcel.restaurant.ejb.model.Address;
import de.marcel.restaurant.ejb.model.Restaurant;
import org.omnifaces.util.Faces;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

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

@ViewScoped
public class SuggestionsBean implements Serializable
{

	@Inject BackingBeanRestaurant backingBeanRestaurant;
	@Inject BackingBeanVisit backingBeanVisit;

	private List<Restaurant> restaurantsRadius;
	private List<Restaurant> restaurantsCulinary;

	private int radius = 50; // 50 km as default

	/////////////////////////////// Proxy Methods for Eventlisteners //////////////////////////////

	public void proxyRadiusChanged() {}

	public void proxyCulinariesChanges() {}

	public void proxyCentralPointChanged() {}

	/////////////////////////////// Methods for Restaurant Matching ///////////////////////////////

	public void filterByRadius() {}

	public void filterByCulinary() {}

	/////////////////////////////// Methods for geospatial Means ///////////////////////////////

	public Address determineCentralPoint() { return null;}

	private double calculateDistance() {return 0.0;}







}
