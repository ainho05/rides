package domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Review implements Serializable{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	private String passengerEmail;
    private String driverEmail;
    private int rating;


    public Review(String passengerEmail, String driverEmail, int rating) {
        this.passengerEmail = passengerEmail;
        this.driverEmail = driverEmail;
        this.rating = rating;
    }

    public String getPassengerEmail() {
        return passengerEmail;
    }

    public String getDriverEmail() {
        return driverEmail;
    }

    public int getRating() {
        return rating;
    }


}