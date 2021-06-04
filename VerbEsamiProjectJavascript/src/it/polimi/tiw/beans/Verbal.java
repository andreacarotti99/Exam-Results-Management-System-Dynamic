package it.polimi.tiw.beans;

import java.sql.Timestamp;

public class Verbal {
	
	private int verbalID;
	private Timestamp date;

	public int getVerbalID() {
		return this.verbalID;
	}
	
	public Timestamp getDate() {
		return this.date;
	}
	
	public void setVerbalID(int id) {
		this.verbalID = id;
	}
	
	public void setDate(Timestamp date) {
		this.date = date;
	}
}
