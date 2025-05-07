package domain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class User {
   
    private String username;
    private String password;
    
    @Id
    private String email;
    
    // IT3 (nuevo atributo)
    @OneToOne(cascade = CascadeType.ALL) // Define la relación uno a uno con Monedero
    private Monedero monedero;
    private byte[] profilePicture; // Nuevo atributo
    
    
    public User() {
    	// IT3 
    	this.monedero = new Monedero();
    }

    public User(String username, String email, String password) {
    	if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email inválido");
        }
    	this.username = username;
        this.password = password;
        this.email = email;
     // IT3
        this.monedero = new Monedero(email);
    }
    // IT3
    public User(String username, String email, String password, byte[] profilePicture) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.monedero = new Monedero(email);
        this.profilePicture = profilePicture;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    // IT3 (añadir getter y setter para el atributo nuevo 'monedero')
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

