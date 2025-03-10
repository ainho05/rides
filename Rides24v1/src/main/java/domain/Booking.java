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

    public Booking() {}

    public Booking(User user, Ride ride) {
        this.user = user;
        this.ride = ride;
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
}
