//package de.marcel.restaurant.web.httpClient;
//
//import de.marcel.restaurant.ejb.interfaces.IRestaurantEJB;
//import de.marcel.restaurant.ejb.model.Address;
//
//import javax.annotation.ManagedBean;
//import javax.ejb.LocalBean;
//import javax.enterprise.context.ApplicationScoped;
//
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.time.Duration;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.*;
//import java.util.logging.Logger;
//
//@ManagedBean
//@LocalBean
//@ApplicationScoped
//
////@Singleton // aus .ejb funktioniert
//public class HttpClientWGS_original
//{
//	private static IRestaurantEJB appServer;
//
//	private static int maxThreads = 20;
//	private static int maxApiCalls = 10;
//	private static int maxReqeuestTomtom = 5;
//
//
//	private static long start;
//	private static long temp;
//	private static byte allApiCalls;
//
////	@Resource private static ManagedExecutorService myExecutor;
//	private static final ThreadPoolExecutor myExecutor = new ThreadPoolExecutor(1,maxThreads,10, TimeUnit.SECONDS,
//					new ArrayBlockingQueue<>(100),
//					new ThreadPoolExecutor.AbortPolicy());
//
//	private static final HttpClient client = HttpClient.newBuilder()
//											.connectTimeout(Duration.ofSeconds(3))
//											.executor(myExecutor)
//					//						.followRedirects(HttpClient.Redirect.NEVER)
//					//						.priority(1) //HTTP/2 priority
//					//						.version(HttpClient.Version.HTTP_2)
//					//						.authenticator(Authenticator.getDefault())
//					//						.cookieHandler(CookieHandler.getDefault())
//					//						.proxy(ProxySelector.getDefault())
//					//						.sslContext(SSLContext.getDefault())
//					//						.sslParameters(new SSLParameters())
//											.build();
//
//	private static final LinkedBlockingQueue<Address> addressQueue = new LinkedBlockingQueue<>(1000);
//
//	// Die Tom Tom Api akzeptiert bis zu 5 Anfragen pro Sekunde und bis zu 2.500 Anfragen pro Tag.
//	// Werden mehr als 5 Anfragen in einer Sekdunde gestellt, erhält man den Code 429 zurück.
//	private static final String REQUEST_URL ="https://api.tomtom.com/search/2/structuredGeocode.json?"
//					+ "countryCode=CountryCodeLocation&"
//					+ "limit=5&"
//					+ "streetNumber=Housenumber&"
//					+ "streetName=Streetname&"
//					+ "municipality=City&"
//					+ "postalCode=ZipCode&"
//					+ "language=en-US&"
//					+ "key=sVYA6qRCAQW0AKOmZQgLvFkQUs73xSfv"; // todo: Api-Key entfernen
//
//
//	public HttpClientWGS_original(){}
//
//	public void enqueueNewRequest(Address adr, IRestaurantEJB ref)
//	{
//		appServer = ref;
//
//		String uriString = REQUEST_URL.replace("CountryCodeLocation", "DE")
//						.replace("Housenumber", adr.getHouseNumber())
//						.replace("Streetname", adr.getStreet())
//						.replace("City", adr.getCity())
//						.replace("ZipCode", adr.getZipCode())
//						.replace(" ", "%20");
//
//		try
//		{
//			URI uri = new URI(uriString);
//			adr.setWgsRestApiCall(uri);
//			Logger.getLogger(HttpClientWGS_original.class.getSimpleName()).severe("+# HttpClient: URL wird der Queue offeriert. Größe AddressQ " + addressQueue.size());
//			addressQueue.offer(adr);
//			Logger.getLogger(HttpClientWGS_original.class.getSimpleName()).severe("+# HttpClient: Neue URI hinzugefügt " + uri + " Umfang der AddressQ " + addressQueue.size());
//		}
//		catch (URISyntaxException e)
//		{
//			e.printStackTrace();
//		}
//
//		if(addressQueue.size() == 1)
//		{
//			new Thread(() -> runClient()).start();
//		}
//		return;
//	}
//
//	private static synchronized void runClient()
//	{
//		Logger.getLogger(HttpClientWGS_original.class.getSimpleName()).severe("+# HttpClient: Client gestartet!");
//
//
//		while(!addressQueue.isEmpty())
//		{
//			Map<Address, CompletableFuture<Boolean>> mapAddressFuture = new HashMap<>();
//
//
//			// Erstellt 5 (maxRequest pro Sekunde) Sendeaufträge für den Http Client
//			for (int i = 0; i < maxReqeuestTomtom; i++)
//			{
//				Address adr = null;
//				if((adr = addressQueue.poll()) == null)
//					break;
//				int counterApiCallsAdress = adr.getCounterApiCalls() +1;
//				if(counterApiCallsAdress < maxApiCalls)
//				{
//					// Ist die maximale Anzahl der Calls erreicht, wird davon ausgegangen,
//					// dass eine weitere Anfrage ebenfalls keinen Ergebnislos bleibt.
//					adr.setCounterApiCalls(counterApiCallsAdress);
//					mapAddressFuture.put(adr, sendRequest(adr));
//				}
//			}
//
//
//			Set<Map.Entry<Address, CompletableFuture<Boolean>>> set = mapAddressFuture.entrySet(); // nur zur Vermeidung von zweimal .entrySet
//
//			// Auftrags-Queue durchlaufen und warten, bis ALLE Aufträge fertig sind.
//			mapAddressFuture.entrySet().stream().forEach(e -> {
//				e.getValue().join();
//			});
//
//
//
//			// Auftrags-Queue, Ergebnisabfrage
//			for (Map.Entry<Address, CompletableFuture<Boolean>> entry : set) // persistieren nachdem x jobs abgelaufen sind // neuer Job beim Speichern, evtl. kurz vor regulärem persist // letzerer muss aber auf jeden Fall erfolgen.
//			{
//				if ( ! entry.getValue().getNow(false)) // null -> false
//				{
//					// Etwas ist schiefgegangen, es wird erneut versucht bis maxApiCalls erreicht ist
//					// Logger.getLogger(HttpClientWGS.class.getSimpleName()).severe("+# HttpClient: Exception für  " + entry.getKey() + " exceptionally beendet! "+"neuer Auftrag wird erstellt  für " + entry.getKey());
//					addressQueue.offer(entry.getKey());
//				}
//				else
//				{
//					// Persistieren
//					Logger.getLogger(HttpClientWGS_original.class.getSimpleName()).severe("+# HttpClient: CF für Adresse " + entry.getKey() + " erfolgreich!");
//
//					// Api Call war erfolgreich, daher in der Adresse der Counter zurückgesetzt werden
//					entry.getKey().setCounterApiCalls(0);
//					//appServer.persist(cf.getNow(null).getValue());}
//
//					// UPDATE VIEW ?
//				}
//			}
////// In Abhängigkeit von der Queue und bereits abgearbeiteten Aufrägen schlafen gehen?
////			if(allApiCalls % 5 == 0)
////			{
////				if ((temp = (System.currentTimeMillis() - start)) < 1100)
////				{
////					//System.out.println("Client schläft für" + (1100 - temp ) + " Millisekunden");
////					try
////					{
////						Thread.sleep(1100 - temp);
////					}
////					catch (InterruptedException e)
////					{
////						e.printStackTrace();
////					}
////				}
////			}
//		}
//		allApiCalls=0;
//		Logger.getLogger(HttpClientWGS_original.class.getSimpleName()).severe("+# HttpClient: Exiting ...");
//	}
//
//	private static CompletableFuture<Boolean> sendRequest(Address adr)
//	{
//		// HttpRequest asynch abschicken
//		HttpRequest request = HttpRequest.newBuilder()
//						.GET()
//						.uri(adr.getWgsRestApiCall())
//						.build();
//
//		Logger.getLogger(HttpClientWGS_original.class.getSimpleName()).severe("+# HttpClient: Request erstellt!");
//
////		if(allApiCalls % maxReqeuestTomtom == 0)
////		{
////			if ((temp = (System.currentTimeMillis() - start)) < 1100)
////			{
////				//System.out.println("Client schläft für" + (1100-temp) + " Millisekunden");
////				try
////				{
////					Thread.sleep(1100 - temp);
////				}
////				catch (InterruptedException e)
////				{
////					e.printStackTrace();
////				}
////			}
////		}
//
////		allApiCalls++;
//
//
//		// Parsing veranlassen und CF zurück erhalten
//		return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
//						.thenComposeAsync(response -> HttpResponseParser.parseResponse(response,adr), myExecutor);
//
//	}
//}
