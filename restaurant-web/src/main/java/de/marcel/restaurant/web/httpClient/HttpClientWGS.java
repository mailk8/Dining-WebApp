package de.marcel.restaurant.web.httpClient;

import de.marcel.restaurant.ejb.RestaurantEJB;
import de.marcel.restaurant.ejb.model.Address;

import javax.annotation.ManagedBean;
import javax.ejb.LocalBean;
import javax.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

@ManagedBean
@LocalBean
@ApplicationScoped
public class HttpClientWGS
{
	//@Inject nicht auf static Fields
	private static RestaurantEJB appServer;
	// Anzahl Threads im Pool
	private static int maxThreads = 20;
	private static int maxApiCalls = 10;
	private static int maxReqeuestTomtom = 5;
	/////////
	private static long start;
	private static long temp;
	private static byte allApiCalls;
	////////
	private static final ThreadPoolExecutor exec = new ThreadPoolExecutor(1,maxThreads,10,TimeUnit.SECONDS,
					new ArrayBlockingQueue<>(100),
					new ThreadPoolExecutor.AbortPolicy());

	private static final java.net.http.HttpClient client = HttpClient.newBuilder()
											.connectTimeout(Duration.ofSeconds(3))
											.executor(exec)
					//						.followRedirects(HttpClient.Redirect.NEVER)
					//						.priority(1) //HTTP/2 priority
					//						.version(HttpClient.Version.HTTP_2)
					//						.authenticator(Authenticator.getDefault())
					//						.cookieHandler(CookieHandler.getDefault())
					//						.proxy(ProxySelector.getDefault())
					//						.sslContext(SSLContext.getDefault())
					//						.sslParameters(new SSLParameters())
											.build();

	private static final LinkedBlockingQueue<Address> myQueue = new LinkedBlockingQueue<>(1000);

	// Die Tom Tom Api akzeptiert bis zu 5 Anfragen pro Sekunde und bis zu 2.500 Anfragen pro Tag.
	// Werden mehr als 5 Anfragen in einer Sekdunde gestellt, erhält man den Code 429 zurück.
	private static final String REQUEST_URL ="https://api.tomtom.com/search/2/structuredGeocode.json?"
					+ "countryCode=CountryCodeLocation&"
					+ "limit=5&"
					+ "streetNumber=Housenumber&"
					+ "streetName=Streetname&"
					+ "municipality=City&"
					+ "postalCode=ZipCode&"
					+ "language=en-US&"
					+ "key=sVYA6qRCAQW0AKOmZQgLvFkQUs73xSfv";


	public HttpClientWGS(){}

	public void enqueueNewRequest(Address adr, RestaurantEJB ref)
	{
		appServer = ref;

		String uriString = REQUEST_URL.replace("CountryCodeLocation", "DE")
						.replace("Housenumber", adr.getHouseNumber())
						.replace("Streetname", adr.getStreet())
						.replace("City", adr.getCity())
						.replace("ZipCode", adr.getZipCode())
						.replace(" ", "%20");

		try
		{
			URI uri = new URI(uriString);
						////System.out.println("+#+# HttpClient: URL wird der Queue offeriert. Größe Q " + myQueue.size());
			myQueue.offer(adr);
			////System.out.println("+#+# HttpClient: Neue URI hinzugefügt " + uri + " Umfang der Queue " + myQueue.size());
		}
		catch (URISyntaxException e)
		{
			e.printStackTrace();
		}

		if(myQueue.size() == 1)
		{
			new Thread(() -> runClient()).start();
		}
		return;
	}

	private static synchronized void runClient()
	{
		System.out.println("+#+# HttpClient: Client gestartet!");


		while(!myQueue.isEmpty())
		{
			Map<Address, CompletableFuture<Boolean>> futures = new HashMap<>();


			start = System.currentTimeMillis();
			for (int i = 0; i < maxReqeuestTomtom; i++)
			{
				Address adr = null;
				if((adr = myQueue.poll()) == null)
					break;
				int counterApi = adr.getCounterApiCalls() +1;
				if(counterApi < maxApiCalls)
				{
					// Ist die maximale Anzahl der Calls erreicht, wird davon ausgegangen,
					// dass eine weitere Anfrage ebenfalls keinen Ergebnislos bleibt.
					adr.setCounterApiCalls(counterApi);
					futures.put(adr, sendRequest(adr));
				}
			}

			// nur zur Vermeidung von zweimal .entrySet
			Set<Map.Entry<Address, CompletableFuture<Boolean>>> set = futures.entrySet();

			////System.out.println("joining CFs");

			futures.entrySet().stream().forEach(e -> {
				e.getValue().join();
			});

			////System.out.println("done with joining CFs");

			// persistieren nachdem x jobs abgelaufen sind
			// neuer Job beim Speichern, evtl. kurz vor regulärem persist
			// letzerer muss aber auf jeden Fall erfolgen.

			for (Map.Entry<Address, CompletableFuture<Boolean>> entry : set)
			{

				////System.out.println("CF in Loop " + entry.getValue());

				if ( ! entry.getValue().getNow(null))
				{
					// Etwas ist schiefgegangen, es wird erneut versucht bis maxApiCalls erreicht ist
					//System.out.println("+#+# HttpClient: Exception für  " + entry.getKey() + " exceptionally beendet! ");
					//System.out.println("neuer Auftrag wird erstellt  für " + entry.getKey());
					myQueue.offer(entry.getKey());
				}
				else
				{
					// Persistieren
					System.out.println("+#+# HttpClient: CF für Adresse " + entry.getKey() + " erfolgreich!");
					//System.out.println("Addresse würde gespeichert werden");

					// Api Call war erfolgreich, daher kann für ein nächstes Mal der Counter zurückgesetzt werden
					entry.getKey().setCounterApiCalls(0);
					//appServer.persist(cf.getNow(null).getValue());}
				}
			}
//// In Abhängigkeit von der Queue und bereits abgearbeiteten Aufrägen schlafen gehen?
//			if(allApiCalls % 5 == 0)
//			{
//				if ((temp = (System.currentTimeMillis() - start)) < 1100)
//				{
//					//System.out.println("Client schläft für" + (1100 - temp ) + " Millisekunden");
//					try
//					{
//						Thread.sleep(1100 - temp);
//					}
//					catch (InterruptedException e)
//					{
//						e.printStackTrace();
//					}
//				}
//			}
		}
		allApiCalls=0;
		System.out.println("+#+# HttpClient: Exiting ...");
	}

	private static CompletableFuture<Boolean> sendRequest(Address adr)
	{
		// HttpRequest asynch abschicken
		HttpRequest request = HttpRequest.newBuilder()
						.GET()
						.uri(adr.getWgsRestApiCall())
						.build();

		////System.out.println("+#+# HttpClient: Request erstellt!");

//		if(allApiCalls % maxReqeuestTomtom == 0)
//		{
//			if ((temp = (System.currentTimeMillis() - start)) < 1100)
//			{
//				//System.out.println("Client schläft für" + (1100-temp) + " Millisekunden");
//				try
//				{
//					Thread.sleep(1100 - temp);
//				}
//				catch (InterruptedException e)
//				{
//					e.printStackTrace();
//				}
//			}
//		}

		allApiCalls++;
		// Parsing veranlassen und CF zurück erhalten
		return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
						.thenComposeAsync(response -> HttpResponseParser.parseResponse(response,adr), exec);

	}
}
