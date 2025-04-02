package businessLogic;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.jws.WebMethod;
import javax.jws.WebService;

import configuration.ConfigXML;
import dataAccess.DataAccess;
import domain.Ride;
import domain.User;
import domain.Booking;
import domain.Driver;
import domain.Review;
import exceptions.RideMustBeLaterThanTodayException;
import exceptions.RideAlreadyExistException;

/**
 * It implements the business logic as a web service.
 */
@WebService(endpointInterface = "businessLogic.BLFacade")
public class BLFacadeImplementation  implements BLFacade {
	private DataAccess dbManager;
	private final RideValidator rideValidator;
	

	public BLFacadeImplementation()  {		
		System.out.println("Creating BLFacadeImplementation instance");
		this.dbManager=new DataAccess();
		this.rideValidator = new RideValidator();
		    
		//dbManager.close();

		
	}
	
    public BLFacadeImplementation(DataAccess da)  {
		
		System.out.println("Creating BLFacadeImplementation instance with DataAccess parameter");
		ConfigXML c=ConfigXML.getInstance();
		this.rideValidator = new RideValidator();
		dbManager=da;		
	}
    
    
    /**
     * {@inheritDoc}
     */
    @WebMethod public List<String> getDepartCities(){
    	dbManager.open();	
		
		 List<String> departLocations=dbManager.getDepartCities();		

		dbManager.close();
		
		return departLocations;
    	
    }
    /**
     * {@inheritDoc}
     */
	@WebMethod public List<String> getDestinationCities(String from){
		dbManager.open();	
		
		 List<String> targetCities=dbManager.getArrivalCities(from);		

		dbManager.close();
		
		return targetCities;
	}

	/**
	 * {@inheritDoc}
	 */
   @Override
	@WebMethod
   public Ride createRide( String from, String to, Date date, int nPlaces, float price, String driverEmail ) throws RideMustBeLaterThanTodayException, RideAlreadyExistException{
	   	rideValidator.validateRideDate(date);
		dbManager.open();
		
		Driver driver = dbManager.getDriver(driverEmail);
	    if (driver == null) {
	        dbManager.close();
	        throw new RideAlreadyExistException("Driver not found");
	    }
	    
		Ride ride=dbManager.createRide(from, to, date, nPlaces, price, driver.getEmail());		
		dbManager.close();
		return ride;
   };
	
   /**
    * {@inheritDoc}
    */
	@WebMethod 
	public List<Ride> getRides(String from, String to, Date date){
		dbManager.open();
		List<Ride>  rides=dbManager.getRides(from, to, date);
		dbManager.close();
		return rides;
	}

    
	/**
	 * {@inheritDoc}
	 */
	@WebMethod 
	public List<Date> getThisMonthDatesWithRides(String from, String to, Date date){
		dbManager.open();
		List<Date>  dates=dbManager.getThisMonthDatesWithRides(from, to, date);
		dbManager.close();
		return dates;
	}
	
	
	public void close() {
		DataAccess dB4oManager=new DataAccess();

		dB4oManager.close();

	}

	/**
	 * {@inheritDoc}
	 */
    @WebMethod	
	 public void initializeBD(){
    	dbManager.open();
		dbManager.initializeDB();
		dbManager.close();
	}
    
    @Override
    public boolean login(String email, String password) {
        User user = dbManager.getUser(email);
        if (user != null && user.getPassword().equals(password)) {
            return true; // Login exitoso
        }
        Driver driver = dbManager.getDriver(email);
        if (driver != null && driver.getPassword().equals(password)) {
            return true;
        }
        return false; // Credenciales incorrectas
    }

    @Override
    public boolean register(String username, String password, String email) {
        User existingUser = dbManager.getUser(email);
        if (existingUser == null) {
            User newUser = new User(username, email, password);
            return dbManager.saveUser(newUser);
        }
        return false; // Usuario ya existe
    }
    
    @Override
    public boolean createRide(String driverEmail, String origin, String destination, Date date, int nPlaces, float price) {
        Driver driver = dbManager.getDriver(driverEmail);
        if (driver == null) {
            return false; // No se puede crear el viaje sin un conductor válido
        }

        Ride newRide = new Ride(origin, destination, date, nPlaces, price, driver);
        return dbManager.saveRide(newRide);
    }

    @Override
    public boolean requestBooking(String passengerEmail, int rideId,int numSeats) {
        User passenger = dbManager.getUser(passengerEmail);
        if (passenger == null) return false; // El usuario no está registrado

        Ride ride = dbManager.getRide(rideId);
        if (ride == null) return false; // El viaje no existe

        // Verificar si hay asientos disponibles
        if (ride.getAvailableSeats() <= numSeats) return false; 

        // Crear la reserva
        Booking booking = new Booking(passenger, ride,numSeats);
        
        // Guardar la reserva en la base de datos
        boolean success = dbManager.saveBooking(booking);
        
        if (success) {
            if (ride.decreaseAvailableSeats(numSeats)) // Reducir número de asientos disponibles
            	dbManager.updateRide(ride); // Guardar cambios en el viaje
            else // no hay sitios avai
            	success = false;
        }
        
        return success;
    }


    @Override
    public List<Booking> viewBookingsForDriver(String driverEmail) {
        return dbManager.getBookingsForDriver(driverEmail);
    }
    
    @Override
    public List<Ride> getAllAvailableRides() {
        return dbManager.getAvailableRides(); // Llamamos a DataAccess
    }
    
    @Override
    public boolean isDriver(String email) {
        return dbManager.getDriver(email) != null; // Si encuentra un driver con ese email, es un Driver
    }
    
    @Override
    @WebMethod
    public boolean acceptBookingRequest(String driverEmail, String passengerEmail) {
        dbManager.open();

        // Obtener la reserva pendiente
        Booking booking = dbManager.getPendingBooking(driverEmail, passengerEmail);
        if (booking == null) {
            dbManager.close();
            return false; // No se encontró la reserva
        }

        Ride ride = booking.getRide();
        if (ride.getAvailableSeats() <= booking.getNumSeats()) {
            dbManager.close();
            return false; // No hay asientos disponibles
        }

        // Confirmar la reserva
        booking.setConfirmed(true);
        boolean success = dbManager.updateBooking(booking);

        if (success) {
            ride.decreaseAvailableSeats(booking.getNumSeats());
            dbManager.updateRide(ride);
        }

        dbManager.close();
        return success;
    }

    @Override
    @WebMethod
    public boolean rejectBookingRequest(String driverEmail, String passengerEmail) {
        dbManager.open();

        // Obtener la reserva pendiente
        Booking booking = dbManager.getPendingBooking(driverEmail, passengerEmail);
        if (booking == null) {
            dbManager.close();
            return false; // No se encontró la reserva
        }

        // Eliminar la reserva de la base de datos
        boolean success = dbManager.deleteBooking(booking);

        dbManager.close();
        return success;
    }

    @Override
    @WebMethod
    public Driver getDriver(String email) {
        dbManager.open();
        Driver driver = dbManager.getDriver(email);
        dbManager.close();
        return driver;
    }
    

	@Override
    public boolean hasPassengerTraveledWithDriver(String passengerEmail, String driverEmail) {
    	return dbManager.hasPassengerTraveledWithDriver(passengerEmail, driverEmail);
    }
    
    @Override
    public void submitReview(Review review) {
        // Implementa la lógica para guardar la reseña en la base de datos
    	dbManager.saveReview(review);
    }
    
    @Override
    public List<Driver> getDriversForPassenger(String passengerEmail) {
        return dbManager.getDriversForPassenger(passengerEmail);
    }
    
    @Override
    public boolean hasPassengerTraveled(String passengerEmail) {
        return dbManager.hasPassengerTraveled(passengerEmail);
    }



}

