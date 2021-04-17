package de.marcel.restaurant.web.httpClient;

import de.marcel.restaurant.ejb.model.Address;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.json.Json;
import javax.json.stream.JsonParser;
import java.io.StringReader;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

@Named
@ApplicationScoped
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class HttpResponseParser
{

	protected static CompletableFuture<Boolean> parseResponse(HttpResponse<String> response, Address adr)
	{
		Logger.getLogger(HttpResponseParser.class.getSimpleName()).severe("+# Response erhalten mit Status " + response.statusCode());

		if((response.statusCode() != 200) || (response.body().length() < 5))
		{
			Logger.getLogger(HttpResponseParser.class.getSimpleName()).severe("+# Statuscode war nicht 200 oder Inhalt des Messagebodys kleiner 5. Statuscode: " + response.statusCode());
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
							//Logger.getLogger(HttpResponseParser.class.getSimpleName()).severe("+# HttpResponseParser: Lat " + lat + " gesetzt in " + adr.getStreet() + " " + adr.getHouseNumber());
							parser.next();
							if(parser.getString().equals("lon")) // Annahme: lon folgt nach lat
							{
								parser.next();
								double lon = parser.getBigDecimal().doubleValue();
								adr.setWgs84Longitude(lon);
								//Logger.getLogger(HttpResponseParser.class.getSimpleName()).severe("+# HttpResponseParser: Lon " + lon + " gesetzt in " + adr.getStreet()+ " " + adr.getHouseNumber());
								return exitSuccessfully(response);
							}
						}
					}

					case KEY_NAME:
					{
						String s = parser.getString();
						//Logger.getLogger(HttpResponseParser.class.getSimpleName()).severe("+#Parser " + s + " Event " + event);

						if(s.equals("numResults"))
						{
							parser.next();
							if(parser.getInt() <= 0) // Keine Ergebnisse geliefert
							{
								//Logger.getLogger(HttpResponseParser.class.getSimpleName()).severe("+# Parser: numResults war 0");
								return exitExceptionally(response);
							}
							//parser.skipObject();
							break;
						}

						if(s.equals("type"))
						{
							parser.next();
							//Logger.getLogger(HttpResponseParser.class.getSimpleName()).severe("+# Parser hat  Type gefunden" + parser.getString());
							if(!parser.getString().equals("Point Address")) // Keine Ergebnisse geliefert
							{
								//Logger.getLogger(HttpResponseParser.class.getSimpleName()).severe("+# Parser: in type war etwas anderes als Point Address");
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
			Logger.getLogger(HttpResponseParser.class.getSimpleName()).severe("+# Allgemeiner Fehler beim Parsen des Json Dokuments.");
			e.printStackTrace();
		}

		//Logger.getLogger(HttpResponseParser.class.getSimpleName()).severe("+# Parser: ist zuende gelaufen");
		return exitExceptionally(response);
	}

	private static CompletableFuture<Boolean> exitExceptionally(HttpResponse<String> resp)
	{
		//resp.headers().map().put("+#+#fail", null);
		CompletableFuture<Boolean> cfFail = new CompletableFuture<>();
		cfFail.complete(false);
		//System.out.println("Parser gibt CF exceptionally zurück " + cfFail);
		Logger.getLogger(HttpResponseParser.class.getSimpleName()).severe("+# Parser gibt CF exceptionally zurück " + cfFail);
		return cfFail;
	}

	private static CompletableFuture<Boolean> exitSuccessfully(HttpResponse<String> resp)
	{
		CompletableFuture<Boolean> cfSucc = new CompletableFuture<>();
		//resp.headers().map().put("+#+#success", null);
		cfSucc.complete(true);
		Logger.getLogger(HttpResponseParser.class.getSimpleName()).severe("+# Parser gibt CF success zurück " + cfSucc);
		return cfSucc;
	}
}
