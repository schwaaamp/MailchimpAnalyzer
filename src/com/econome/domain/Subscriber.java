package com.econome.domain;

import java.time.LocalDate;

public class Subscriber {
	
	private String emailAddress;
	private LocalDate subscriptionDate;
	private int openCount;
	private int unopenedCount;
	private LocalDate latestOpenDate;
	
	public Subscriber(String emailAddress, LocalDate subscriptionDate) {
		super();
		this.emailAddress = emailAddress;
		this.subscriptionDate = subscriptionDate;
	}
	
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	public LocalDate getSubscriptionDate() {
		return subscriptionDate;
	}
	public void setSubscriptionDate(LocalDate subscriptionDate) {
		this.subscriptionDate = subscriptionDate;
	}
	public int getOpenCount() {
		return openCount;
	}
	public void setOpenCount(int openCount) {
		this.openCount = openCount;
	}
	public int getUnopenedCount() {
		return unopenedCount;
	}
	public void setUnopenedCount(int unopenedCount) {
		this.unopenedCount = unopenedCount;
	}
	public LocalDate getLatestOpenDate() {
		return latestOpenDate;
	}
	public void setLatestOpenDate(LocalDate latestOpenDate) {
		this.latestOpenDate = latestOpenDate;
	}
	
	

}
