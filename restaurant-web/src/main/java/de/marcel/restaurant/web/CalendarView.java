package de.marcel.restaurant.web;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;

//@ManagedBean
@Named("calendarView")
@ViewScoped
public class CalendarView implements Serializable
{

	private Date date1;
	private Date date2;
	private Date date3;
	private Date date4;
	private Date date5 = new Date();
	private Date date6;
	private Date date7;
	private Date date8;
	private Date date9;
	private Date date10;
	private Date date11 = new Date();

	LocalDate ld;
	LocalTime lt;
	LocalDateTime ldt;


	public void onDateSelect(SelectEvent event) {

		ld = ((Date)(event.getObject())).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

		if(lt != null)
		{
			ldt = LocalDateTime.of(ld, lt);
			ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();
			String sessionId = ectx.getSession(false).toString();
			ectx.getSessionMap().put(sessionId + "chosenDate", ldt);
			//Logger.getLogger(getClass().getSimpleName()).log(Level.WARNING, "+# Calendar: Legt in den Kontext: " + ldt);
		}
	}


	public void onTimeSelect(SelectEvent event) {

		lt = ((Date)(event.getObject())).toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

		if(ld != null)
		{
			ldt = LocalDateTime.of(ld, lt);
			ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();
			String sessionId = ectx.getSession(false).toString();
			ectx.getSessionMap().put(sessionId + "chosenDate", ldt);
		}
		//Logger.getLogger(getClass().getSimpleName()).log(Level.WARNING, "+# Calendar: Legt in den Kontext: " + ldt);
	}

	public void click() {
		PrimeFaces.current().ajax().update("form:display");
		PrimeFaces.current().executeScript("PF('dlg').show()");
	}

	public Date getDate1() {
		return date1;
	}

	public void setDate1(Date date1) {
		this.date1 = date1;
	}

	public Date getDate2() {
		return date2;
	}

	public void setDate2(Date date2) {
		this.date2 = date2;
	}

	public Date getDate3() {
		return date3;
	}

	public void setDate3(Date date3) {
		this.date3 = date3;
	}

	public Date getDate4() {
		return date4;
	}

	public void setDate4(Date date4) {
		this.date4 = date4;
	}

	public Date getDate5() {
		return date5;
	}

	public void setDate5(Date date5) {
		this.date5 = date5;
	}

	public Date getDate6() {
		return date6;
	}

	public void setDate6(Date date6) {
		this.date6 = date6;
	}

	public Date getDate7() {
		return date7;
	}

	public void setDate7(Date date7) {
		this.date7 = date7;
	}

	public Date getDate8() {
		return date8;
	}

	public void setDate8(Date date8) {
		this.date8 = date8;
	}

	public Date getDate9() {
		return date9;
	}

	public void setDate9(Date date9) {
		this.date9 = date9;
	}

	public Date getDate10() {
		return date10;
	}

	public void setDate10(Date date10) {
		this.date10 = date10;
	}

	public Date getDate11() {
		return date11;
	}

	public void setDate11(Date date11) {
		this.date11 = date11;
	}
}