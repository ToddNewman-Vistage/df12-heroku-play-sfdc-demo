package models;

import play.db.jpa.Model;

import javax.persistence.Entity;

@Entity
public class Person extends Model {

	public String googleId;
	public String sfdcId;

	public String firstName;
	public String lastName;
	public String nickname;
	public String title;
	public String company;
	public String email;
	public String mobile;
	public String city;
	public String state;

	public Person() { }

	public String getFullName() {
		if (firstName == null) return lastName;
		return firstName + " " + lastName;
	}
    
}
