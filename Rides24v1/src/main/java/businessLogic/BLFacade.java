package businessLogic;

import java.util.Date;
import java.util.List;

//import domain.Booking;
import domain.Ride;
import domain.Booking;
import domain.Driver;
import domain.Review;
import exceptions.RideMustBeLaterThanTodayException;
import exceptions.SaldoInsuficienteException;
import exceptions.RideAlreadyExistException;

import javax.jws.WebMethod;
import javax.jws.WebService;
 
/**
 * Interface that specifies the business logic.
 */
@WebService
public interface BLFacade  {
	  
	/**
	 * This method returns all the cities where rides depart 
	 * @return collection of cities
	 */
	@WebMethod public List<String> getDepartCities();
	
	/**
	 * This method returns all the arrival destinations, from all rides that depart from a given city  
	 * 
	 * @param from the depart location of a ride
	 * @return all the arrival destinations
	 */
	@WebMethod public List<String> getDestinationCities(String from);


	/**
	 * This method creates a ride for a driver
	 * 
	 * @param from the origin location of a ride
	 * @param to the destination location of a ride
	 * @param date the date of the ride 
	 * @param nPlaces available seats
	 * @param driver to which ride is added
	 * 
	 * @return the created ride, or null, or an exception
	 * @throws RideMustBeLaterThanTodayException if the ride date is before today 
 	 * @throws RideAlreadyExistException if the same ride already exists for the driver
	 */
   @WebMethod
   public Ride createRide( String from, String to, Date date, int nPlaces, float price, String driverEmail) throws RideMustBeLaterThanTodayException, RideAlreadyExistException;
	
	
	/**
	 * This method retrieves the rides from two locations on a given date 
	 * 
	 * @param from the origin location of a ride
	 * @param to the destination location of a ride
	 * @param date the date of the ride 
	 * @return collection of rides
	 */
	@WebMethod public List<Ride> getRides(String from, String to, Date date);
	
	/**
	 * This method retrieves from the database the dates a month for which there are events
	 * @param from the origin location of a ride
	 * @param to the destination location of a ride 
	 * @param date of the month for which days with rides want to be retrieved 
	 * @return collection of rides
	 */
	@WebMethod public List<Date> getThisMonthDatesWithRides(String from, String to, Date date);
	
	/**
	 * This method calls the data access to initialize the database with some events and questions.
	 * It is invoked only when the option "initialize" is declared in the tag dataBaseOpenMode of resources/config.xml file
	 */	
	@WebMethod public void initializeBD();
	
	// IT 1
	boolean login(String username, String password);
	boolean register(String username, String password, String email);
	
	// para que una Driver vea sus reservas
	List<Booking> viewBookingsForDriver(String driverEmail);
	// para que una Driver cree un viaje
	boolean createRide(String driverEmail, String origin, String destination, Date date, int nPlaces, float price);
	// para que una User (pasajera) reserve un viaje existente
	boolean requestBooking(String passengerEmail, int rideId, int numSeats, double unitPrice);
	// devuelve los viajes que aún tengan asientos disponibles
	List<Ride> getAllAvailableRides();
	boolean isDriver(String email);

	@WebMethod boolean acceptBookingRequest(String driverEmail, String passengerEmail);
	@WebMethod boolean rejectBookingRequest(String driverEmail, String passengerEmail);

	@WebMethod
	Driver getDriver(String email);
	
	boolean hasPassengerTraveledWithDriver(String passengerEmail,
	String driverEmail);
	void submitReview(Review review);
	List<Driver> getDriversForPassenger(String passengerEmail);
	boolean hasPassengerTraveled(String passengerEmail);

	//it3
	public List<Ride> getRidesWithMaxPrice(double maxPrice);
	
	// IT3
	void cargarSaldo(String userEmail, float cantidad);
    boolean realizarPago(String userEmail, float cantidad);
    void recibirPago(String userEmail, float cantidad);
    float consultarSaldo(String userEmail);

	void simularCargarSaldoConTarjeta(String userEmail, float cantidad, String numeroTarjeta, String cvc,
			String titular);

	@WebMethod
    boolean acceptBooking(int bookingId) throws SaldoInsuficienteException;

	public Booking getBookingByPassengerAndDriver(String driverEmail, String passengerEmail);
	public Ride getRide(int rideId);
	
	public boolean cancelBooking(int bookingId);
	public List<Booking> getPendingBookingsForUser(String userEmail);

}
