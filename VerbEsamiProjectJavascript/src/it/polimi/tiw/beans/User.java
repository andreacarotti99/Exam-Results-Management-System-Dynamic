package it.polimi.tiw.beans;

public class User {

	private int id;
	private String username;
	private String name;
	private String surname;
	private String password;
	private boolean isProfessor;
	private String mail;
	
	public String getPassword(){
		return this.password;
	}
	
	public boolean isProfessor() {
		return isProfessor;
	}


	public void setProfessor(boolean isProfessor) {
		this.isProfessor = isProfessor;
	}

	public String getMail() {
		return this.mail;
	}
	
	public int getId() {
		return id;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	
	
	public void setMail(String mail) {
		this.mail = mail;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

}