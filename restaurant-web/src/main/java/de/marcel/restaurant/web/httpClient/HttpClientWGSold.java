//package de.marcel.restaurant.web.httpClient;
//
//
//import de.marcel.restaurant.ejb.model.Address;
//
//import javax.annotation.ManagedBean;
//import javax.ejb.LocalBean;
//import javax.enterprise.context.ApplicationScoped;
//import javax.inject.Inject;
//import javax.json.stream.JsonParser;
//import java.io.StringReader;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.time.Duration;
//import java.util.LinkedHashMap;
//import java.util.Map;
//import java.util.concurrent.*;
//import javax.json.Json;
//
//@ManagedBean
//@LocalBean
//@ApplicationScoped
//public class HttpClientWGS
//{
//
//	//@Inject nicht auf static Fields
//	private static RestaurantEJB appServer;
//
//	private static ThreadPoolExecutor executor = new ThreadPoolExecutor(1,
//					20,
//					10,
//					TimeUnit.SECONDS,
//					new ArrayBlockingQueue<>(100),
//					new ThreadPoolExecutor.AbortPolicy());
//
//	private static java.net.http.HttpClient client = HttpClient.newBuilder()
//					.connectTimeout(Duration.ofSeconds(3))
//					.executor(executor)
//					//						.followRedirects(HttpClient.Redirect.NEVER)
//					//						.priority(1) //HTTP/2 priority
//					//						.version(HttpClient.Version.HTTP_2)
//					//						.authenticator(Authenticator.getDefault())
//					//						.cookieHandler(CookieHandler.getDefault())
//					//						.proxy(ProxySelector.getDefault())
//					//						.sslContext(SSLContext.getDefault())
//					//						.sslParameters(new SSLParameters())
//					.build();
//
//	private static LinkedHashMap<URI, Address> myQueue = new LinkedHashMap<>();
//	private static LinkedHashMap<URI, Address> myResults = new LinkedHashMap<>();
//
//	private static final String REQUEST_URL ="https://api.tomtom.com/search/2/structuredGeocode.json?"
//					+ "countryCode=CountryCodeLocation&"
//					+ "limit=5&"
//					+ "streetNumber=Housenumber&"
//					+ "streetName=Streetname&"
//					+ "municipality=City&"
//					+ "postalCode=ZipCode&"
//					+ "language=en-US&"
//					+ "key=sVYA6qRCAQW0AKOmZQgLvFkQUs73xSfv";
//
//
//	public HttpClientWGS(){}
//
//	public void enqueueNewRequest(Address a, RestaurantEJB ref)
//	{
//		appServer = ref;
//
//		String uri = REQUEST_URL.replace("CountryCodeLocation", "DE")
//						.replace("Housenumber", a.getHouseNumber())
//						.replace("Streetname", a.getStreet())
//						.replace("City", a.getCity())
//						.replace("ZipCode", a.getZipCode())
//						.replace(" ", "%20");
//
//		try
//		{
//			addRequest(new URI(uri), a);
//			System.out.println("+#+# HttpClient: Neue URI hinzugefügt " + uri + " Umfang der Queue " + myQueue.size());
//		}
//		catch (URISyntaxException e)
//		{
//			e.printStackTrace();
//		}
//
//		if(myQueue.size() == 1)
//			new Thread(()-> runClient()).start();
//		return;
//	}
//
//	private static void runClient()
//	{
//		System.out.println("+#+# HttpClient: Client gestartet!");
//
//		while(!myQueue.isEmpty())
//		{
//			// TODO: In Entitys herausfiltern, die schon mehr als 10 (?) mal abgefragt wurden.
//
//
//
//			CompletableFuture<Map.Entry<URI, Address>>[] futures = myQueue.entrySet().stream()
//							.map(entry -> sendRequest(entry))
//							.toArray(CompletableFuture[]::new);
//
//
//			CompletableFuture.allOf(futures).join();
//			// persistieren nachdem jobs abgelaufen sind
//			// neuer Job beim Speichern, evtl. kurz vor regulärem persist
//			// letzerer muss aber auf jeden Fall erfolgen.
//
//			for (CompletableFuture<Map.Entry<URI, Address>> cf : futures)
//			{
//				if (cf.isDone())
//				{
//					if (cf.isCompletedExceptionally())
//					{
//						System.out.println("+#+# HttpClient: Exception für  " + cf.getNow(null) + " exceptionally beendet! Im if");
//					}
//					else
//					{
//						System.out.println("+#+# HttpClient: CF für Adresse " + cf.getNow(null).getValue().getPrim() + " erfolgreich!");
//						//appServer.persist(cf.getNow(null).getValue());}
//					}
//					if (cf.isCompletedExceptionally())
//					{
//						System.out.println("+#+# HttpClient: Exception für  " + cf.getNow(null) + " exceptionally beendet! Außerhalb if");
//					}
//
//				}
//			}
//		}
//	}
//
//	private static CompletableFuture<Void> sendRequest(Map.Entry<URI, Address> entry)
//	{
//		LinkedBlockingQueue m;
//		// TODO: In Entity den Versuch der Abfrage in einem Zähler vermerken.
//
//		// Request Daten aus Q entfernen, wenn sie bereits raus sind, wird ein gescheiterter CF zurückgegeben, der nicht weiter berücksichtigt wird.
//		if (myQueue.remove(entry.getKey()) == null)
//		{
//			System.out.println("+#+# HttpClient: Entry sollte aus Q entfernt werden und war nicht enthalten.");
//			return exitExceptionally();
//		}
//
//		// HttpRequest asynch abschicken
//		HttpRequest request = HttpRequest.newBuilder()
//						.GET()
//						.uri(entry.getKey())
//						.build();
//
//		System.out.println("+#+# HttpClient: Request erstellt!");
//
//		// Future zurückgeben an Aufrufmethode, vorher das Parsing starten
//		return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
//						.thenAcceptAsync(response -> HttpResponseParser.parseResponse(response, entry));
//
//	}
//
//
//
//
//
//	private static synchronized void addRequest(URI u, Address a)
//	{
//		myQueue.put(u, a);
//	}
//
//	private static synchronized void removeRequest(URI u)
//	{
//		myQueue.remove(u);
//	}
//
//	private static CompletableFuture<Void> exitExceptionally()
//	{
//		CompletableFuture<Void> cfNull = new CompletableFuture<>();
//		cfNull.completeExceptionally(new RuntimeException());
//		return cfNull;
//	}
//
//	private static CompletableFuture<Void> exitSuccessfully()
//	{
//		CompletableFuture<Void> suc = new CompletableFuture<>();
//		suc.complete(null);
//		return suc;
//	}
//}
