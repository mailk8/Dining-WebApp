package de.marcel.restaurant.web.jsfFramework;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class SessionListenerWithMetrics implements HttpSessionListener
{
	private Logger logger = Logger.getLogger(getClass().getSimpleName());


	private final AtomicInteger activeSessions;

	public SessionListenerWithMetrics() {
		super();

		activeSessions = new AtomicInteger();
	}

	public int getTotalActiveSession() {
		return activeSessions.get();
	}

	public void sessionCreated(final HttpSessionEvent event) {
		activeSessions.incrementAndGet();
		logger.severe("+# Session " + event.getSession().getId() + " created. Aktive Sessions " + activeSessions);

		Iterator<String> it = event.getSession().getAttributeNames().asIterator();
		while(it.hasNext())
		{
			String s = it.next();
			logger.severe("+# Session Attribute" + s + " mit Value " + event.getSession().getAttribute(s));
		}

	}
	public void sessionDestroyed(final HttpSessionEvent event) {
		activeSessions.decrementAndGet();
		logger.severe("+# Session " + event.getSession().getId() + " destroyed. Aktive Sessions " + activeSessions);

		Iterator<String> it = event.getSession().getAttributeNames().asIterator();
		while(it.hasNext())
		{
			String s = it.next();
			logger.severe("+# Session Attribute" + s + " mit Value " + event.getSession().getAttribute(s));
		}
	}
}