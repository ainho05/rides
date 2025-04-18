package domain;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class User {
   
    private String username;
    private String password;
    
    @Id
    private String email;
    
    public User() {}

    public User(String username, String email, String password) {
    	if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email inválido");
        }
    	this.username = username;
        this.password = password;
        this.email = email;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

