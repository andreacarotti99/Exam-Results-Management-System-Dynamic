package it.polimi.tiw.beans;

import java.util.Date;

public class Round {

	private int roundId;
	private Date date;
	private String className;
	private boolean studentRegistered;
	
	
	public int getRoundId() {
		return roundId;
	}
	
	public Date getDate() {
		return date;
	}
	
	public String getClassName() {
		return className;
	}

	public boolean isStudentRegistered() {
		return studentRegistered;
	}

	public void setStudentRegistered(boolean studentRegistered) {
		this.studentRegistered = studentRegistered;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	public void setRoundId(int roundId) {
		this.roundId = roundId;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}
	
}
