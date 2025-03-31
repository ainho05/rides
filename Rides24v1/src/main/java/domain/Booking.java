package domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class Booking implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Ride ride;
    
    private boolean confirmed;
    private int numSeats;

    public Booking() {}

    public Booking(User user, Ride ride, int numSeats) {
    	if (user == null || ride == null) {
            throw new IllegalArgumentException("User/Ride no pueden ser null");
        }
    	this.user = user;
        this.ride = ride;
        this.numSeats=numSeats;
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Ride getRide() { return ride; }
    public void setRide(Ride ride) { this.ride = ride; }

	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
		
	}

	public boolean isConfirmed() {
		return confirmed;
	}
	public int getNumSeats() {
		return numSeats;
	}
}
