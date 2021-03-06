package de.marcel.restaurant.web;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import java.util.logging.Logger;

public class LifeCycleListener implements PhaseListener
{

	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}

	public void beforePhase(PhaseEvent event) {
		Logger.getLogger(getClass().getSimpleName()).severe("+# Start der JSF Phase " + event.getPhaseId());
	}

	public void afterPhase(PhaseEvent event) {
		Logger.getLogger(getClass().getSimpleName()).severe("+# Ende der JSF Phase " + event.getPhaseId());
	}

}