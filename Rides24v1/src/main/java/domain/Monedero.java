package domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Monedero {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Estrategia de generación de ID (auto-incremento en muchos DB)
	private int idMonedero;
	
    private String userEmail;
    private float saldo;
    //private String fechaCreacion;
    private String ultimaTransaccion;
    
    // constructor vacío necesario para JPA
    public Monedero() {
    	this.saldo = (float) 0.0;
    }
    
    // constructor principal
    public Monedero(String userEmail) {
        this.userEmail = userEmail;
        this.saldo = (float)0.0;
    }
    
    
    public int getIdMonedero() {
        return idMonedero;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public float getSaldo() {
        return saldo;
    }



    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setSaldo(float saldo) {
        this.saldo = saldo;
    }

    public void anadirSaldo(float cantidad) {
    	this.saldo = this.saldo + cantidad;
    }
    public void quitarSaldo(float cantidad) {
    	this.saldo = this.saldo - cantidad;
    }
  
    @Override
    public String toString() {
        return "Monedero{" +
               "idMonedero = " + idMonedero +
               ", userEmail = " + userEmail +
               ", saldo = " + saldo +
               '}';
    }
    
    

}
