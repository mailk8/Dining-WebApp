package de.marcel.restaurant.web.jsfFramework;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public class SessionListener implements HttpSessionListener
{


	private final AtomicInteger activeSessions;

	public SessionListener() {
		super();
		activeSessions = new AtomicInteger();
	}

	public int getTotalActiveSession() {
		return activeSessions.get();
	}

	public void sessionCreated(final HttpSessionEvent event) {
		activeSessions.incrementAndGet();
		Iterator<String> it = event.getSession().getAttributeNames().asIterator();
		while(it.hasNext())
		{
			String s = it.next();
		}

	}
	public void sessionDestroyed(final HttpSessionEvent event) {
		activeSessions.decrementAndGet();

		Iterator<String> it = event.getSession().getAttributeNames().asIterator();
		while(it.hasNext())
		{
			String s = it.next();
		}
	}
}