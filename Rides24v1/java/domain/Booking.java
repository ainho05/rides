package domain;

import javax.persistence.*;

import businessLogic.BLFacadeImplementation.BookingStatus;

import java.io.Serializable;
import java.text.SimpleDateFormat;

@Entity
public class Booking implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Ride ride;
    
    private int numSeats;
    
    private boolean confirmed;
    // IT3
    private BookingStatus status;
    private float price;
    

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
	
	// IT3
	public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
    
    public int getBookingId() {
    	return this.id;
    }
    
    public void setPrice(float price) {
    	this.price = price;
    }
    public float getPrice() {
    	return this.price;
    }
    
    @Override
    public String toString() {
        if (this.ride != null && this.ride.getDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String formattedDate = sdf.format(this.ride.getDate());
            return "De: " + this.ride.getFrom() + " - A: " + this.ride.getTo() +
                   ", Fecha: " + formattedDate + ", Asientos: " + this.numSeats;
        } else if (this.ride != null) {
            return "De: " + this.ride.getFrom() + " - A: " + this.ride.getTo() +
                   ", Fecha: Sin especificar" + ", Asientos: " + this.numSeats;
        } else {
            return "Reserva sin información de viaje" + ", Asientos: " + this.numSeats;
        }
    }
}

