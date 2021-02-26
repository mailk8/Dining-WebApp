package de.marcel.restaurant.ejb.model;

import de.marcel.restaurant.ejb.interfaces.IState;

public enum State implements IState
{
	OBJEKT_ERZ, ERWÜNSCHT, GEPLANT, ERFOLGT, BEWERTUNG_OFFEN, BEWERTET
}
