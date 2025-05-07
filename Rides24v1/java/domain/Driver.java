package domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class Driver implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@XmlID
	@Id 
	private String email;
	private String name;
	private String password;
	@XmlIDREF
	//@OneToMany(fetch=FetchType.EAGER, cascade=CascadeType.PERSIST)
	@OneToMany(mappedBy = "driver", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<Ride> rides=new Vector<Ride>();
	
	// IT3 (nuevo atributo)
	@OneToOne(cascade = CascadeType.ALL) // Define la relación uno a uno con Monedero
    private Monedero monedero;
	private byte[] profilePicture;

	public Driver() {
		super();
		this.monedero = new Monedero();
	}

	public Driver(String email, String password, String name) {
		this.email = email;
		this.name = name;
		this.password = password;
		// IT3
		this.monedero = new Monedero(email);
	}
	public Driver(String email, String name) {
		this.email = email;
		this.name = name;
		// IT3
		this.monedero = new Monedero(email);
	}
	// IT3
	public Driver(String email, String password, String name, byte[] profilePicture) {
		this.email = email;
		this.name = name;
		this.password = password;
		this.monedero = new Monedero(email);
		this.profilePicture = profilePicture;
	}
	
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		if (email == null || !email.contains("@")) {
	           throw new IllegalArgumentException("Email inválido");
	    }
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public List<Ride> getRides(){
		return rides;
	}

	
	
	public String toString(){
		return email+";"+name+rides;
	}
	
	/**
	 * This method creates a bet with a question, minimum bet ammount and percentual profit
	 * 
	 * @param question to be added to the event
	 * @param betMinimum of that question
	 * @return Bet
	 */
	public Ride addRide(String from, String to, Date date, int nPlaces, float price)  {
        Ride ride=new Ride(from,to,date,nPlaces,price, this);
        rides.add(ride);
        return ride;
	}

	/**
	 * This method checks if the ride already exists for that driver
	 * 
	 * @param from the origin location 
	 * @param to the destination location 
	 * @param date the date of the ride 
	 * @return true if the ride exists and false in other case
	 */
	public boolean doesRideExists(String from, String to, Date date)  {	
		for (Ride r:rides)
			if ( (java.util.Objects.equals(r.getFrom(),from)) && 
				 (java.util.Objects.equals(r.getTo(),to)) && 
				 (java.util.Objects.equals(r.getDate(),date))&&
				 (this.equals(r.getDriver())))
			 return true;
		
		return false;
	}
		
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Driver other = (Driver) obj;
		if (email != other.email)
			return false;
		return true;
	}

	public Ride removeRide(String from, String to, Date date) {
		boolean found=false;
		int index=0;
		Ride r=null;
		while (!found && index<=rides.size()) {
			r=rides.get(++index);
			if ( (java.util.Objects.equals(r.getFrom(),from)) && (java.util.Objects.equals(r.getTo(),to)) && (java.util.Objects.equals(r.getDate(),date)) )
			found=true;
		}
			
		if (found) {
			rides.remove(index);
			return r;
		} else return null;
	}

	public String getPassword() {
		// TODO Auto-generated method stub
		return password;
	}
	
	// IT3 (getter y setter)
	public void setMonedero(Monedero monedero) {
    	this.monedero = monedero;
    }
    public Monedero getMonedero() {
    	return this.monedero;
    }
    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }

}
