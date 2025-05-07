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
    private int id;
	
	private String passengerEmail;
    private String driverEmail;
    private int rating;
    
    // IT3
    private int punctualityRating;
    private int carComfortRating;
    private int driverAttitudeRating;
    private String comment;
    
    public Review() {
        this.punctualityRating = 0;
        this.carComfortRating = 0;
        this.driverAttitudeRating = 0;
        this.comment = "";
    }

    public Review(String passengerEmail, String driverEmail, int rating) {
        this.passengerEmail = passengerEmail;
        this.driverEmail = driverEmail;
        this.rating = rating;
        this.punctualityRating = 0;
        this.carComfortRating = 0;
        this.driverAttitudeRating = 0;
        this.comment = "";
    }
    
    public Review(String passengerEmail, String driverEmail, int rating, int punctualityRating, int carComfortRating, int driverAttitudeRating, String comment) {
        this.passengerEmail = passengerEmail;
        this.driverEmail = driverEmail;
        this.rating = rating;
        this.punctualityRating = punctualityRating;
        this.carComfortRating = carComfortRating;
        this.driverAttitudeRating = driverAttitudeRating;
        this.comment = comment;
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
    
    // IT3
    public int getId() {
        return id;
    }
    
    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getPunctualityRating() {
        return punctualityRating;
    }

    public void setPunctualityRating(int punctualityRating) {
        this.punctualityRating = punctualityRating;
    }

    public int getCarComfortRating() {
        return carComfortRating;
    }

    public void setCarComfortRating(int carComfortRating) {
        this.carComfortRating = carComfortRating;
    }

    public int getDriverAttitudeRating() {
        return driverAttitudeRating;
    }

    public void setDriverAttitudeRating(int driverAttitudeRating) {
        this.driverAttitudeRating = driverAttitudeRating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


}