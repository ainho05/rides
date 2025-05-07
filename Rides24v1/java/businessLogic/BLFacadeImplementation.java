package businessLogic;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.jws.WebMethod;
import javax.jws.WebService;

import businessLogic.BLFacadeImplementation.BookingStatus;
import configuration.ConfigXML;
import dataAccess.DataAccess;
import domain.Ride;
import domain.User;
import domain.Booking;
import domain.Driver;
import domain.Monedero;
import domain.NotificationService;
import domain.Review;
import exceptions.RideMustBeLaterThanTodayException;
import exceptions.SaldoInsuficienteException;
import exceptions.NotificationException;
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
    public boolean requestBooking(String passengerEmail, int rideId,int numSeats, double unitPrice) {
        User passenger = dbManager.getUser(passengerEmail);
        if (passenger == null) return false; // El usuario no está registrado

        Ride ride = dbManager.getRide(rideId);
        if (ride == null) return false; // El viaje no existe

        // Verificar si hay asientos disponibles
        if (ride.getAvailableSeats() <= numSeats) return false;
        
        // IT3
        Monedero passengerWallet = dbManager.getMonederoByUserEmail(passengerEmail);
        if (passengerWallet == null)
            return false; // El monedero del usuario no existe (debería existir si el usuario está registrado)

        double totalPrice = unitPrice * numSeats; // Calcular el precio total

        // Verificar si el pasajero tiene suficiente saldo
        if (passengerWallet.getSaldo() < totalPrice) {
            // Informar al usuario que no tiene suficiente saldo
            System.out.println("Saldo insuficiente para el usuario: " + passengerEmail);
            return false;
        }
        
     // Debitar el saldo del monedero
        passengerWallet.quitarSaldo((float)totalPrice);
        boolean walletUpdated = dbManager.updateMonedero(passengerWallet);
        
        if (walletUpdated) {
            // Crear la reserva
            Booking booking = new Booking(passenger, ride, numSeats);
            booking.setStatus(BookingStatus.PENDING);
            booking.setPrice((float)totalPrice); // Guardar el precio total en la reserva

            // Guardar la reserva en la base de datos
            boolean bookingSaved = dbManager.saveBooking(booking);

            if (bookingSaved) {
                if (ride.decreaseAvailableSeats(numSeats)) {
                    dbManager.updateRide(ride);
                    return true; // Reserva solicitada y pago cargado
                } else {
                    // Si falla la actualización de asientos, deberías revertir el pago y eliminar la reserva
                    passengerWallet.anadirSaldo((float)totalPrice);
                    dbManager.updateMonedero(passengerWallet);
                    dbManager.deleteBooking(booking); // Asumiendo que tienes un método para eliminar
                    return false;
                }
            } else {
                // Si falla el guardado de la reserva, deberías revertir el pago
                passengerWallet.anadirSaldo((float)totalPrice);
                dbManager.updateMonedero(passengerWallet);
                return false;
            }
        } else {
            // Si falla la actualización del monedero
            return false;
        }
        
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
            
            //nuevo
           // sendAcceptanceNotification(passengerEmail,driverEmail);
        }

        dbManager.close();
        return success;
    }

    @Override
    @WebMethod
    public boolean rejectBookingRequest(String driverEmail, String passengerEmail) {
        dbManager.open();
        
        // IT3 (cambiar parte del método)
        boolean success = false;

        try {
            // Obtener la reserva pendiente
            Booking booking = dbManager.getPendingBooking(driverEmail, passengerEmail);
            if (booking == null) {
                return false; // No se encontró la reserva
            }

            // Obtener el email del pasajero y el precio de la reserva
            String passengerEmailForRefund = booking.getUser().getEmail();
            double bookingPrice = booking.getPrice();

            // Obtener el monedero del pasajero
            Monedero passengerWallet = dbManager.getMonederoByUserEmail(passengerEmailForRefund);
            if (passengerWallet == null) {
                // Log de error: no se encontró el monedero
                System.err.println("No se encontró el monedero para el pasajero: " + passengerEmailForRefund);
                return false;
            }

            // Devolver el dinero al monedero del pasajero
            passengerWallet.anadirSaldo((float)bookingPrice);
            boolean walletUpdated = dbManager.updateMonedero(passengerWallet);

            if (walletUpdated) {
                // Eliminar la reserva de la base de datos
            	booking.setStatus(BookingStatus.REJECTED);
                success = dbManager.deleteBooking(booking);
            } else {
                // Log de error: no se pudo actualizar el monedero
                System.err.println("Error al actualizar el monedero del pasajero: " + passengerEmailForRefund + " tras rechazar la reserva.");
                // Considera si quieres eliminar la reserva incluso si falla la devolución del dinero.
            }

        } finally {
            dbManager.close();
        }

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
    	dbManager.open();
        try {
            dbManager.saveReview(review); // Tu método para guardar la reseña en la base de datos
        } finally {
            dbManager.close();
        }
    }
    
    @Override
    public List<Driver> getDriversForPassenger(String passengerEmail) {
        return dbManager.getDriversForPassenger(passengerEmail);
    }
    
    @Override
    public boolean hasPassengerTraveled(String passengerEmail) {
        return dbManager.hasPassengerTraveled(passengerEmail);
    }

   /* private void sendAcceptanceNotification(String passengerEmail, String driverEmail) {
        NotificationService notificationService = new NotificationService();
        try {
            notificationService.sendRideAcceptedEmail(
                passengerEmail, 
                driverEmail,
                "Tu solicitud de viaje ha sido aceptada",
                String.format("El conductor %s ha aceptado tu reserva.", driverEmail)
            );
        } catch (NotificationException e) {
            System.err.println("Error enviando notificación: " + e.getMessage());
            // Puedes lanzar una excepción personalizada o simplemente loggear el error
        }*/
    //}
    //it3
    @Override
    public List<Ride> getRidesWithMaxPrice(double maxPrice) {
        List<Ride> allRides = null;
		try {
			allRides = DataAccess.getAllRides();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // Suponiendo que existe este método
        return allRides.stream()
                .filter(ride -> ride.getPrice() <= maxPrice)
                .collect(Collectors.toList());
    }
    
    // IT3 (todos lo métodos a partir de aquí) 
    public enum BookingStatus {
        PENDING, ACCEPTED, REJECTED, CANCELLED
    }
	@Override
	public void cargarSaldo(String userEmail, float cantidad) {
		Monedero monedero = dbManager.getMonederoByUserEmail(userEmail);		
		
		if (monedero != null) {
			monedero.anadirSaldo(cantidad);
			dbManager.updateMonedero(monedero);
            // ... lógica adicional (ej. registrar transacción) ...
        } else {
        	System.out.println("No tiene creado un monedero. Creando uno nuevo...");
            // Manejar el caso en que no se encuentra el monedero (quizás crear uno?)
            Monedero nuevoMonedero = new Monedero();
            nuevoMonedero.setUserEmail(userEmail);
            nuevoMonedero.setSaldo(cantidad);
            dbManager.saveMonedero(nuevoMonedero);
        }
	}

	@Override
	public boolean realizarPago(String userEmail, float cantidad) {
		Monedero monedero = dbManager.getMonederoByUserEmail(userEmail);
        if (monedero != null && monedero.getSaldo() >= cantidad) {
            monedero.quitarSaldo(cantidad);
            dbManager.updateMonedero(monedero);
            // ... lógica adicional ...
            return true;
        }
        System.out.println("No se pudo realizar el pago. No hay suficiente dinero en la tarjeta :(");
        return false;
	}

	@Override
	public void recibirPago(String userEmail, float cantidad) {
		Monedero monedero = dbManager.getMonederoByUserEmail(userEmail);
        if (monedero != null) {
            monedero.anadirSaldo(cantidad);
            dbManager.updateMonedero(monedero);
            // ... lógica adicional ...
        } else {
            // Manejar el caso en que no se encuentra el monedero (quizás crear uno?)
            System.out.println("No se ha encontrado ningún monedero. Creando uno...");
        	Monedero nuevoMonedero = new Monedero();
            nuevoMonedero.setUserEmail(userEmail);
            nuevoMonedero.setSaldo(cantidad);
            // ... inicializar otros atributos ...
            dbManager.saveMonedero(nuevoMonedero);
        }
		
	}

	@Override
	public float consultarSaldo(String userEmail) {
		Monedero monedero = dbManager.getMonederoByUserEmail(userEmail);
		if (monedero != null) {
			return monedero.getSaldo();
		}
		else {
			System.out.println("No hay un monedero disponible");
			return 0;
		}
	}

	@Override
	public void simularCargarSaldoConTarjeta(String userEmail, float cantidad, String numeroTarjeta, String cvc,
			String titular) {
			
		dbManager.simularCargarSaldoConTarjeta(userEmail, cantidad, numeroTarjeta, cvc, titular);		
	}
	
	@Override
	@WebMethod
	public boolean acceptBooking(int bookingId) throws SaldoInsuficienteException {
	    dbManager.open(); // Asegúrate de que la transacción se maneja correctamente
	    boolean success = false;
	    try {
	        Booking booking = dbManager.getBookingById(bookingId); // Debes crear este método en DataAccess
	        if (booking == null) {
	            dbManager.close();
	            throw new RuntimeException("Reserva no encontrada: " + bookingId);
	        }

	        if (booking.getStatus() != BookingStatus.PENDING) {
	            dbManager.close();
	            throw new RuntimeException("La reserva no está pendiente: " + bookingId);
	        }

	        User passenger = booking.getUser();
	        Ride ride = booking.getRide();
	        float costeDelViaje = ride.getPrice() * booking.getNumSeats(); // Calcular el coste total

	        // Intentar pagar con el monedero del pasajero
	        if (realizarPago(booking.getUser().getEmail(), costeDelViaje)) {
	            booking.setStatus(BookingStatus.ACCEPTED); // Cambiar el estado a ACCEPTED
	            success = dbManager.updateBooking(booking); // Debes crear este método en DataAccess

	            if (success) {
	                ride.decreaseAvailableSeats(booking.getNumSeats());
	                dbManager.updateRide(ride);
	                
	                // Obtener el monedero del conductor
	                Monedero driverWallet = dbManager.getMonederoByUserEmail(ride.getDriver().getEmail());
	                if (driverWallet != null) {
	                    // Añadir el coste del viaje al monedero del conductor
	                    driverWallet.anadirSaldo(costeDelViaje);
	                    if (!dbManager.updateMonedero(driverWallet)) {
	                        System.err.println("Error al añadir saldo al conductor: " + ride.getDriver().getEmail());
	                        // Considera deshacer la aceptación de la reserva y la actualización del viaje si falla
	                        // la actualización del monedero del conductor para mantener la integridad.
	                    }
	                } else {
	                    System.err.println("No se encontró el monedero para el conductor: " + ride.getDriver().getEmail());
	                    // Considera qué hacer si no se encuentra el monedero del conductor.
	                }
	            }
	        } else {
	            booking.setStatus(BookingStatus.REJECTED);
	            dbManager.updateBooking(booking); // Actualizar el estado en la base de datos
	            throw new SaldoInsuficienteException("Saldo insuficiente para el pasajero: " + booking.getUser().getEmail());
	        }
	    } catch (SaldoInsuficienteException e) {
	        dbManager.close();
	        throw e; // Relanzar la excepción para que se maneje en la GUI
	    } finally {
	        dbManager.close();
	    }
	    return success;
	}
	
	public Booking getBookingByPassengerAndDriver(String driverEmail, String passengerEmail) {
	    dbManager.open();
	    Booking booking = dbManager.getBookingByPassengerAndDriver(driverEmail, passengerEmail); // Implementa esto en DataAccess
	    dbManager.close();
	    return booking;
	}
	
	public Ride getRide(int rideId) {
		dbManager.open();
		Ride ride = dbManager.getRide(rideId);
		dbManager.close();
		return ride;
	}


    @WebMethod
    public boolean cancelBooking(int bookingId) {
        dbManager.open();
        
    	boolean success = false;

        try {
            // Obtener la reserva a cancelar por su ID
            Booking booking = dbManager.getBookingById(bookingId);
            if (booking == null) {
                return false; // No se encontró la reserva
            }

            // Verificar que la reserva esté en estado PENDING
            if (booking.getStatus() == BookingStatus.PENDING) {
            	// Obtener el Ride asociado a la reserva
                Ride ride = booking.getRide();
                int numSeatsBooked = booking.getNumSeats();

                // Incrementar el número de plazas disponibles en el Ride
                if (ride != null) {
                    ride.increaseAvailableSeats(numSeatsBooked);
                    boolean rideUpdated = dbManager.updateRide(ride); // Asegúrate de tener este método
                    if (!rideUpdated) {
                        System.err.println("Error al actualizar las plazas disponibles del viaje con ID: " + ride.getRideNumber());
                        return false; // No se pudo actualizar el Ride
                    }
                } else {
                    System.err.println("El viaje asociado a la reserva con ID " + bookingId + " es null.");
                    return false; // No hay viaje asociado
                }
                // Obtener el email del pasajero y el precio de la reserva
                String passengerEmailForRefund = booking.getUser().getEmail();
                double bookingPrice = booking.getPrice();

                // Obtener el monedero del pasajero
                Monedero passengerWallet = dbManager.getMonederoByUserEmail(passengerEmailForRefund);
                if (passengerWallet == null) {
                    System.err.println("No se encontró el monedero para el pasajero: " + passengerEmailForRefund + " al cancelar la reserva.");
                    return false;
                }

                // Devolver el dinero al monedero del pasajero
                passengerWallet.anadirSaldo((float) bookingPrice);
                boolean walletUpdated = dbManager.updateMonedero(passengerWallet);

                if (walletUpdated) {
                    // Cambiar el estado de la reserva a CANCELLED
                    booking.setStatus(BookingStatus.CANCELLED);
                    success = dbManager.updateBooking(booking); // Necesitas este método en DataAccess
                } else {
                    System.err.println("Error al actualizar el monedero del pasajero: " + passengerEmailForRefund + " tras cancelar la reserva.");
                    return false;
                }
            } else {
                System.err.println("No se puede cancelar la reserva con ID " + bookingId + " porque su estado es " + booking.getStatus());
                return false; // No se puede cancelar si no está en PENDING
            }

            if (success) {
                dbManager.close();
                return true;
            } else {
                return false;
            }

        } finally {
            dbManager.close();
        }
    }
    
    @Override
    @WebMethod
    public List<Booking> getPendingBookingsForUser(String userEmail) {
        dbManager.open();
        List<Booking> pendingBookings = dbManager.getBookingsByUserAndStatus(userEmail, BookingStatus.PENDING);
        dbManager.close();
        return pendingBookings;
    }

}

