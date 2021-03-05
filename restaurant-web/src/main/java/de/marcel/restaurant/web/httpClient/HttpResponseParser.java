package de.marcel.restaurant.web.httpClient;

import de.marcel.restaurant.ejb.model.Address;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.json.Json;
import javax.json.stream.JsonParser;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Named
@ApplicationScoped

/* Java Docu
{ START_OBJECT
   "firstName"KEY_NAME: "John"VALUE_STRING, "lastName"KEY_NAME: "Smith"VALUE_STRING, "age"KEY_NAME: 25VALUE_NUMBER,
   "phoneNumber"KEY_NAME : [START_ARRAY
       {START_OBJECT "type"KEY_NAME: "home"VALUE_STRING, "number"KEY_NAME: "212 555-1234"VALUE_STRING }END_OBJECT,
       {START_OBJECT "type"KEY_NAME: "fax"VALUE_STRING, "number"KEY_NAME: "646 555-4567"VALUE_STRING }END_OBJECT
    ]END_ARRAY
 }END_OBJECT
 */
public class HttpResponseParser
{

	protected static CompletableFuture<Boolean> parseResponse(HttpResponse<String> response, Address adr)
	{
		//System.out.println("+# HttpResponseParser: Response erhalten mit Status " + response.statusCode());

		if((response.statusCode() != 200) || (response.body().length() < 5))
		{
			//System.out.println("+# HttpResponseParser: Statuscode war nicht 200 oder Inhalt des Messagebodys kleiner 5. Statuscode: " + response.statusCode());
			return exitExceptionally(response);
		}

		try(JsonParser parser = Json.createParser(new StringReader(response.body())))
		{

			while (parser.hasNext())
			{
				JsonParser.Event event = parser.next();

				switch (event)
				{
					case START_OBJECT:
					{
						event = parser.next();

						if(parser.getString().equals("lat"))
						{
							parser.next();

							double lat = parser.getBigDecimal().doubleValue();
							adr.setWgs84Latitude(lat);
							//System.out.println("+# HttpResponseParser: Lat " + lat + " gesetzt in " + adr.getStreet() + " " + adr.getHouseNumber());
							parser.next();
							if(parser.getString().equals("lon")) // Annahme: lon folgt nach lat
							{
								parser.next();
								double lon = parser.getBigDecimal().doubleValue();
								adr.setWgs84Longitude(lon);
								//System.out.println("+# HttpResponseParser: Lon " + lon + " gesetzt in " + adr.getStreet()+ " " + adr.getHouseNumber());
								return exitSuccessfully(response);
							}
						}
					}

					case KEY_NAME:
					{
						String s = parser.getString();
						//System.out.println("+#Parser " + s + " Event " + event);

						if(s.equals("numResults"))
						{
							parser.next();
							if(parser.getInt() <= 0) // Keine Ergebnisse wurden geliefert
							{
								//System.out.println("+# Parser: numResults war 0");
								return exitExceptionally(response);
							}
							//parser.skipObject();
							break;
						}

						if(s.equals("type"))
						{
							parser.next();
							//System.out.println("+# Parser hat  Type gefunden" + parser.getString());
							if(!parser.getString().equals("Point Address")) // Keine Ergebnisse wurden geliefert
							{
								//System.out.println("+# Parser: in type war etwas anderes als Point Address");
								return exitExceptionally(response);
							}
							parser.skipObject();
							break;
						}
					}
					//					case START_ARRAY:
					//					case END_ARRAY:
					//					case END_OBJECT:
					//					case VALUE_FALSE:
					//					case VALUE_NULL:
					//					case VALUE_TRUE:
					//					case VALUE_STRING:
					//					case VALUE_NUMBER:
				}
			}
		}
		catch (Exception e)
		{
			//System.out.println("+# HttpResponseParser: Allgemeiner Fehler beim Parsen des Json Dokuments.");
			e.printStackTrace();
		}

		//System.out.println("+# Parser: ist zuende gelaufen");
		return exitExceptionally(response);
	}

	private static CompletableFuture<Boolean> exitExceptionally(HttpResponse<String> resp)
	{
		//resp.headers().map().put("+#+#fail", null);
		CompletableFuture<Boolean> cfFail = new CompletableFuture<>();
		cfFail.complete(false);
		//System.out.println("Parser gibt CF exceptionally zurück " + cfFail);
		return cfFail;
	}

	private static CompletableFuture<Boolean> exitSuccessfully(HttpResponse<String> resp)
	{
		CompletableFuture<Boolean> cfSucc = new CompletableFuture<>();
		//resp.headers().map().put("+#+#success", null);
		cfSucc.complete(true);
		return cfSucc;
	}
}
