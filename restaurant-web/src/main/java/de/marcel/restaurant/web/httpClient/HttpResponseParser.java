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

		if((response.statusCode() != 200) || (response.body().length() < 5))
		{
			Logger.getLogger(HttpResponseParser.class.getSimpleName()).severe("+# Responseparser hat Antwort mit Status != 200 erhalten. Code " + response.statusCode());
			return exitExceptionally(response);
		}

		try(JsonParser parser = Json.createParser(new StringReader(response.body())))
		{
			Logger.getLogger(HttpResponseParser.class.getSimpleName()).severe("+# Responseparser verarbeitet Antwort" );

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
							Logger.getLogger(HttpResponseParser.class.getSimpleName()).severe("+# Responseparser setzt Adresse auf lat " + adr.getWgs84Latitude());
							parser.next();
							if(parser.getString().equals("lon")) // Annahme: lon folgt nach lat
							{
								parser.next();
								double lon = parser.getBigDecimal().doubleValue();
								adr.setWgs84Longitude(lon);
								Logger.getLogger(HttpResponseParser.class.getSimpleName()).severe("+# Responseparser änderte Adresse auf lon" + adr.getWgs84Longitude());
								return exitSuccessfully(response);
							}
						}
					}

					case KEY_NAME:
					{
						String s = parser.getString();

						if(s.equals("numResults"))
						{
							parser.next();
							if(parser.getInt() <= 0) // Keine Ergebnisse geliefert
							{
								return exitExceptionally(response);
							}
							//parser.skipObject();
							break;
						}

						if(s.equals("type"))
						{
							parser.next();
							if(!parser.getString().equals("Point Address")) // Keine Ergebnisse geliefert
							{
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
			e.printStackTrace();
		}

		return exitExceptionally(response);
	}

	private static CompletableFuture<Boolean> exitExceptionally(HttpResponse<String> resp)
	{
		Logger.getLogger(HttpResponseParser.class.getSimpleName()).severe("+# Responseparser fail");
		CompletableFuture<Boolean> cfFail = new CompletableFuture<>();
		cfFail.complete(false);
		return cfFail;
	}

	private static CompletableFuture<Boolean> exitSuccessfully(HttpResponse<String> resp)
	{
		Logger.getLogger(HttpResponseParser.class.getSimpleName()).severe("+# Responseparser success");
		CompletableFuture<Boolean> cfSucc = new CompletableFuture<>();
		cfSucc.complete(true);
		return cfSucc;
	}
}
