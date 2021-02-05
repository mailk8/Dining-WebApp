//package de.marcel.restaurant.web.validators;
//
//import de.marcel.restaurant.ejb.RestaurantEJB;
//
//import javax.enterprise.context.RequestScoped;
//import javax.enterprise.context.SessionScoped;
//import javax.inject.Inject;
//import javax.inject.Named;
//import java.io.Serializable;
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.Date;
//
//@Named
//@RequestScoped
//public class ValidationView implements Serializable
//{
//
//
//	private String text;
//	private String description;
//	private Integer integer;
//	private Double doubleNumber;
//	private BigDecimal bigDecimal;
//
//	private Double money;
//	private String regexText;
//	private Date date;
//	private Date date2;
//	private Date date3;
//	private LocalDate localDate;
//	private LocalDate localDate2;
//	private LocalDate localDate3;
//
//	public String getText() {
//		return text;
//	}
//	public void setText(String text) {
//		this.text = text;
//	}
//
//	public String getDescription() {
//		return description;
//	}
//	public void setDescription(String description) {
//		this.description = description;
//	}
//
//	public Integer getInteger() {
//		return integer;
//	}
//	public void setInteger(Integer integer) {
//		this.integer = integer;
//	}
//
//	public Double getDoubleNumber() {
//		return doubleNumber;
//	}
//	public void setDoubleNumber(Double doubleNumber) {
//
//		this.doubleNumber = doubleNumber;
//	}
//
//	public Double getTestDouble() {
//		return doubleNumber;
//	}
//	public void setTestDouble(String s) {
//
//		s = s.replace(",", ".");
//		setDoubleNumber(Double.parseDouble(s.trim()));
//	}
//
//
//	public Double getMoney() {
//		return money;
//	}
//	public void setMoney(Double money) {
//		this.money = money;
//	}
//
//	public String getRegexText() {
//		return regexText;
//	}
//	public void setRegexText(String regexText) {
//		this.regexText = regexText;
//	}
//
//	public Date getDate() {
//		return date;
//	}
//	public void setDate(Date date) {
//		this.date = date;
//	}
//
//	public Date getDate2() {
//		return date2;
//	}
//	public void setDate2(Date date) {
//		this.date2 = date;
//	}
//
//	public Date getDate3() {
//		return date3;
//	}
//	public void setDate3(Date date) {
//		this.date3 = date;
//	}
//
//	public LocalDate getLocalDate() {
//		return localDate;
//	}
//	public void setLocalDate(LocalDate localDate) {
//		this.localDate = localDate;
//	}
//
//	public LocalDate getLocalDate2() {
//		return localDate2;
//	}
//	public void setLocalDate2(LocalDate localDate) {
//		this.localDate2 = localDate;
//	}
//
//	public LocalDate getLocalDate3() {
//		return localDate3;
//	}
//	public void setLocalDate3(LocalDate localDate) {
//		this.localDate3 = localDate;
//	}
//
//	public BigDecimal getBigDecimal()
//	{
//		return bigDecimal;
//	}
//
//	public void setBigDecimal(BigDecimal bigDecimal)
//	{
//		this.bigDecimal = bigDecimal;
//	}
//}