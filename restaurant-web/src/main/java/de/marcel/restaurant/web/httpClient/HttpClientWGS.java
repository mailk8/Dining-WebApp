package de.marcel.restaurant.web.httpClient;

import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
import de.marcel.restaurant.ejb.model.Address;
import de.marcel.restaurant.ejb.model.User;
import de.marcel.restaurant.web.jsfFramework.WebSocketObserver;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class HttpClientWGS implements Serializable
{
	private static final long serialVersionUID = 1L;

	private  int maxApiCalls = 10;
	private  int maxReqeuestTomtom = 5;

	private java.net.http.HttpClient client;
	@Inject private  IRestaurantEJB appServer;
	@Inject private WebSocketObserver websocket;
	@Resource private  ManagedExecutorService executor;
	private  final LinkedBlockingQueue<Address> addressQueue = new LinkedBlockingQueue<>(100);

	@PostConstruct
	private void prepare() {
	// Der mit @Resource annotierte Executor darf erst spät bei @PostConstruct verwendet werden.
	// Sonst gibt es eine NPE bei Objektinitialisierung.

	client = HttpClient.newBuilder()
					.connectTimeout(Duration.ofSeconds(3))
					.executor(executor)
					// .followRedirects(HttpClient.Redirect.NEVER)
					// .priority(1) //HTTP/2 priority
					// .version(HttpClient.Version.HTTP_2)
					// .authenticator(Authenticator.getDefault())
					// .cookieHandler(CookieHandler.getDefault())
					// .proxy(ProxySelector.getDefault())
					// .sslContext(SSLContext.getDefault())
					// .sslParameters(new SSLParameters())
					.build();
	}

	// Die Tom Tom Api akzeptiert bis zu 5 Anfragen pro Sekunde und bis zu 2.500 Anfragen pro Tag.
	// Werden mehr als 5 Anfragen in einer Sekdunde gestellt, erhält man den Code 429 zurück.
	private  final String REQUEST_URL ="https://api.tomtom.com/search/2/structuredGeocode.json?"
					+ "countryCode=CountryCodeLocation&"
					+ "limit=5&"
					+ "streetNumber=Housenumber&"
					+ "streetName=Streetname&"
					+ "municipality=City&"
					+ "postalCode=ZipCode&"
					+ "language=en-US&"
					+ "key=sVYA6qRCAQW0AKOmZQgLvFkQUs73xSfv"; // todo: Api-Key entfernen


	public HttpClientWGS(){}

	public void enqueueNewRequest(Address adr)
	{
		// Entities stellen ihre Anfrage hier mit Übergabe ihrer Adresse ein

		String uriString = REQUEST_URL.replace("CountryCodeLocation", "DE")
						.replace("Housenumber", adr.getHouseNumber())
						.replace("Streetname", adr.getStreet())
						.replace("City", adr.getCity())
						.replace("ZipCode", adr.getZipCode())
						.replace(" ", "%20");

		try
		{
			URI uri = new URI(uriString);
			adr.setWgsRestApiCall(uri);
			addressQueue.offer(adr);
		}
		catch (URISyntaxException e)
		{
			e.printStackTrace();
		}

		if(addressQueue.size() == 1)
		{
			// Start des Clients, falls er noch nicht läuft
			executor.execute(()-> runClient());
		}
		return;
	}


	private void runClient()
	{
		while(!addressQueue.isEmpty())
		{
			// Auftrag- und Result-Container für diese Iteration
			Map<Address, CompletableFuture<Boolean>> mapAddressFuture = new HashMap<>();


			// Erstellt maxRequest = 5 Sendeaufträge für den Http Client
			for (int i = 0; i < maxReqeuestTomtom; i++)
			{
				Address adr = null;
				if((adr = addressQueue.poll()) == null)
					break;
				// Für jede Adresse maximal 10 Requests pro Anfrage
				int counterApiCallsAdress = adr.getCounterApiCalls() +1;
				if(counterApiCallsAdress < maxApiCalls)
				{
					adr.setCounterApiCalls(counterApiCallsAdress);
					mapAddressFuture.put(adr, sendRequest(adr));
				}
			}

			Set<Map.Entry<Address, CompletableFuture<Boolean>>> set = mapAddressFuture.entrySet(); // vermeidet von 2x .entrySet

			// Auftrags-Container durchlaufen und warten, bis alle Aufträge (incl. Parsing) abgeschlossen sind.
			mapAddressFuture.entrySet().stream().forEach(e -> e.getValue().join() );


			// Auftrags-Container, Ergebnisabfrage
			for (Map.Entry<Address, CompletableFuture<Boolean>> entry : set)
			{
				Address adr = entry.getKey();

				if ( ! entry.getValue().getNow(false) )
				{
					addressQueue.offer(adr);
					// Faces Message ?
				}
				else
				{
					entry.getKey().setCounterApiCalls(0);
					websocket.sendMessage(adr.getSessionId());
				}
			}
		}
	}

	private CompletableFuture<Boolean> sendRequest(Address adr)
	{
		// HttpRequest asynchron abschicken und Parsing veranlassen
		HttpRequest request = HttpRequest.newBuilder().GET().uri(adr.getWgsRestApiCall()).build();
		return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
						.thenComposeAsync(response -> HttpResponseParser.parseResponse(response,adr), executor);
	}
}
