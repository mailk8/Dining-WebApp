package de.marcel.restaurant.web.messages;

import javax.annotation.ManagedBean;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.util.logging.Logger;

@Named
@RequestScoped
public class GrowlView {

	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void saveMessage() {
		FacesContext context = FacesContext.getCurrentInstance();

		context.addMessage(null, new FacesMessage("Successful",  "Your message: " + message) );
		context.addMessage(null, new FacesMessage("Second Message", "Additional Message Detail"));
	}


}