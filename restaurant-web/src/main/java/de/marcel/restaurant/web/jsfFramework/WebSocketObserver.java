package de.marcel.restaurant.web.jsfFramework;

import de.marcel.restaurant.ejb.model.Rating;
import de.marcel.restaurant.ejb.model.Restaurant;
import de.marcel.restaurant.ejb.model.RestaurantVisit;
import de.marcel.restaurant.ejb.model.User;
import de.marcel.restaurant.web.httpClient.HttpClientWGS;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.faces.event.WebsocketEvent;
import javax.faces.push.Push;
import javax.faces.push.PushContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.logging.Logger;

@Named
@ApplicationScoped
public class WebSocketObserver implements Serializable
{
	@Inject @Push private PushContext channelUser;
	@Inject @Push private PushContext channelRest;
	@Inject @Push private PushContext channelVisit;
	@Inject @Push private PushContext channelRating;
	@Inject @Push private PushContext channelEdit;

	public void sendMessage(Class channelEntity) {

		Logger.getLogger(getClass().getSimpleName()).severe("+# vor sendMessage  " );
		if (channelEntity.isAssignableFrom(User.class))
			channelUser.send("channelUser");
		else if(channelEntity.isAssignableFrom(Restaurant.class))
			channelRest.send("channelRest");
		else if(channelEntity.isAssignableFrom(RestaurantVisit.class))
			channelVisit.send("channelVisit");
		else if(channelEntity.isAssignableFrom(Rating.class))
			channelRating.send("channelRating");
		else
			return;
	}

	public void sendMessage(String sessionId) {
		Logger.getLogger(HttpClientWGS.class.getSimpleName()).severe("+# sendMessage mit SessionID : " + sessionId);
		channelEdit.send("channelEdit", sessionId);
	}

	public void onOpen(@Observes @WebsocketEvent.Opened WebsocketEvent event) {
		String channel = event.getChannel();
		Logger.getLogger(getClass().getSimpleName()).severe("+# onOpen auf Channel: " + channel + " Event war: " + event + " User Id " + event.getUser());
	}

	public void onClose(@Observes @WebsocketEvent.Closed WebsocketEvent event) {
		Logger.getLogger(getClass().getSimpleName()).severe("+# onClose auf Channel: " + event.getChannel() + " Event war: " + event + " Close Code " + event.getCloseCode() + " User Id " + event.getUser());
	}

}