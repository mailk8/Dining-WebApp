package de.marcel.restaurant.ejb.model;

import de.marcel.restaurant.ejb.interfaces.IState;

public enum State implements IState
{
	UNVOLLSTÄNDIG, ANGELEGT, GEPLANT, ERFOLGT, BEWERTUNG_OFFEN, BEWERTET
}
