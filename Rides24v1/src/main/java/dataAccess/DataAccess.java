package dataAccess;

import java.io.File;
import java.net.NoRouteToHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.jdo.Query;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import configuration.ConfigXML;
import configuration.UtilDate;
import domain.Booking;
import domain.Driver;
import domain.Ride;
import domain.User;
import exceptions.RideAlreadyExistException;
import exceptions.RideMustBeLaterThanTodayException;

/**
 * It implements the data access to the objectDb database
 */
public class DataAccess  {
    private EntityManager db;
    private EntityManagerFactory emf;
    private ConfigXML c = ConfigXML.getInstance();

    public DataAccess() {
        emf = Persistence.createEntityManagerFactory("objectdb:mydb.odb;drop");
        db = emf.createEntityManager();
        open();
        // No llamamos a initializeDB aquí
    }

     
    public DataAccess(EntityManager db) {
    	this.db=db;
    }

   
	
	/**
	 * This is the data access method that initializes the database with some events and questions.
	 * This method is invoked by the business logic (constructor of BLFacadeImplementation) when the option "initialize" is declared in the tag dataBaseOpenMode of resources/config.xml file
	 */	
	public void initializeDB(){
		
		db.getTransaction().begin();

		try {
			
		   Calendar today = Calendar.getInstance();
		   
		   int month=today.get(Calendar.MONTH);
		   int year=today.get(Calendar.YEAR);
		   if (month==12) { month=1; year+=1;}  
	    
		   
		    //Create drivers 
			Driver driver1=new Driver("driver1@gmail.com", "d1", "Aitor Fernandez");
			Driver driver2=new Driver("driver2@gmail.com", "d2", "Ane Gaztañaga");
			Driver driver3=new Driver("driver3@gmail.com","d3", "Test driver");

			
			//Create rides
			driver1.addRide("Donostia", "Bilbo", UtilDate.newDate(year,month,15), 4, 7);
			driver1.addRide("Donostia", "Gazteiz", UtilDate.newDate(year,month,6), 4, 8);
			driver1.addRide("Bilbo", "Donostia", UtilDate.newDate(year,month,25), 4, 4);

			driver1.addRide("Donostia", "Iruña", UtilDate.newDate(year,month,7), 4, 8);
			
			driver2.addRide("Donostia", "Bilbo", UtilDate.newDate(year,month,15), 3, 3);
			driver2.addRide("Bilbo", "Donostia", UtilDate.newDate(year,month,25), 2, 5);
			driver2.addRide("Eibar", "Gasteiz", UtilDate.newDate(year,month,6), 2, 5);

			driver3.addRide("Bilbo", "Donostia", UtilDate.newDate(year,month,14), 1, 3);
			
			// IT 1 (crear usuarios pasajeros y reservar viajes para usuarios)
			User user1 = new User("user1@gmail.com", "u1","user1");
			User user2 = new User("user2@gmail.com" , "u2","user2");
			User user3 = new User("user3@gmail.com", "u3","user3");

	        // ✅ Reservar viajes para usuarios
	        Booking booking1 = new Booking(user1, driver1.getRides().get(0)); 
	        Booking booking2 = new Booking(user2, driver1.getRides().get(1));
	        Booking booking3 = new Booking(user3, driver2.getRides().get(0));
			
						
			db.persist(driver1);
			db.persist(driver2);
			db.persist(driver3);
			
			db.persist(user1);
	        db.persist(user2);
	        db.persist(user3);

	        db.persist(booking1);
	        db.persist(booking2);
	        db.persist(booking3);

	
			db.getTransaction().commit();
			System.out.println("Db initialized");
		}
		catch (Exception e){
			if (db.getTransaction().isActive()) {
			    db.getTransaction().rollback();
			}
			e.printStackTrace();
		}
	}
	
	/**
	 * This method returns all the cities where rides depart 
	 * @return collection of cities
	 */
	public List<String> getDepartCities(){
			TypedQuery<String> query = db.createQuery("SELECT DISTINCT r.from FROM Ride r ORDER BY r.from", String.class);
			List<String> cities = query.getResultList();
			return cities;
		
	}
	/**
	 * This method returns all the arrival destinations, from all rides that depart from a given city  
	 * 
	 * @param from the depart location of a ride
	 * @return all the arrival destinations
	 */
	public List<String> getArrivalCities(String from){
		TypedQuery<String> query = db.createQuery("SELECT DISTINCT r.to FROM Ride r WHERE r.from=?1 ORDER BY r.to",String.class);
		query.setParameter(1, from);
		List<String> arrivingCities = query.getResultList(); 
		return arrivingCities;
		
	}
	/**
	 * This method creates a ride for a driver
	 * 
	 * @param from the origin location of a ride
	 * @param to the destination location of a ride
	 * @param date the date of the ride 
	 * @param nPlaces available seats
	 * @param driverEmail to which ride is added
	 * 
	 * @return the created ride, or null, or an exception
	 * @throws RideMustBeLaterThanTodayException if the ride date is before today 
 	 * @throws RideAlreadyExistException if the same ride already exists for the driver
	 */
	public Ride createRide(String from, String to, Date date, int nPlaces, float price, String driverEmail) throws  RideAlreadyExistException, RideMustBeLaterThanTodayException {
		System.out.println(">> DataAccess: createRide=> from= "+from+" to= "+to+" driver="+driverEmail+" date "+date);
		try {
			if(new Date().compareTo(date)>0) {
				throw new RideMustBeLaterThanTodayException(ResourceBundle.getBundle("Etiquetas").getString("CreateRideGUI.ErrorRideMustBeLaterThanToday"));
			}
			db.getTransaction().begin();
			
			Driver driver = db.find(Driver.class, driverEmail);
			if (driver.doesRideExists(from, to, date)) {
				db.getTransaction().commit();
				throw new RideAlreadyExistException(ResourceBundle.getBundle("Etiquetas").getString("DataAccess.RideAlreadyExist"));
			}
			Ride ride = driver.addRide(from, to, date, nPlaces, price);
			//next instruction can be obviated
			db.persist(driver); 
			db.getTransaction().commit();

			return ride;
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			db.getTransaction().commit();
			return null;
		}
		
		
	}
	
	/**
	 * This method retrieves the rides from two locations on a given date 
	 * 
	 * @param from the origin location of a ride
	 * @param to the destination location of a ride
	 * @param date the date of the ride 
	 * @return collection of rides
	 */
	public List<Ride> getRides(String from, String to, Date date) {
		System.out.println(">> DataAccess: getRides=> from= "+from+" to= "+to+" date "+date);

		List<Ride> res = new ArrayList<>();	
		TypedQuery<Ride> query = db.createQuery("SELECT r FROM Ride r WHERE r.from=?1 AND r.to=?2 AND r.date=?3",Ride.class);   
		query.setParameter(1, from);
		query.setParameter(2, to);
		query.setParameter(3, date);
		List<Ride> rides = query.getResultList();
	 	 for (Ride ride:rides){
		   res.add(ride);
		  }
	 	return res;
	}
	
	/**
	 * This method retrieves from the database the dates a month for which there are events
	 * @param from the origin location of a ride
	 * @param to the destination location of a ride 
	 * @param date of the month for which days with rides want to be retrieved 
	 * @return collection of rides
	 */
	public List<Date> getThisMonthDatesWithRides(String from, String to, Date date) {
		System.out.println(">> DataAccess: getEventsMonth");
		List<Date> res = new ArrayList<>();	
		
		Date firstDayMonthDate= UtilDate.firstDayMonth(date);
		Date lastDayMonthDate= UtilDate.lastDayMonth(date);
				
		
		TypedQuery<Date> query = db.createQuery("SELECT DISTINCT r.date FROM Ride r WHERE r.from=?1 AND r.to=?2 AND r.date BETWEEN ?3 and ?4",Date.class);   
		
		query.setParameter(1, from);
		query.setParameter(2, to);
		query.setParameter(3, firstDayMonthDate);
		query.setParameter(4, lastDayMonthDate);
		List<Date> dates = query.getResultList();
	 	 for (Date d:dates){
		   res.add(d);
		  }
	 	return res;
	}
	

public void open(){
		
		String fileName=c.getDbFilename();
		if (c.isDatabaseLocal()) {
			emf = Persistence.createEntityManagerFactory("objectdb:"+fileName);
			db = emf.createEntityManager();
		} else {
			Map<String, String> properties = new HashMap<>();
			  properties.put("javax.persistence.jdbc.user", c.getUser());
			  properties.put("javax.persistence.jdbc.password", c.getPassword());

			  emf = Persistence.createEntityManagerFactory("objectdb://"+c.getDatabaseNode()+":"+c.getDatabasePort()+"/"+fileName, properties);
			  db = emf.createEntityManager();
    	   }
		System.out.println("DataAccess opened => isDatabaseLocal: "+c.isDatabaseLocal());

		
	}

	public void close(){
	    if (db != null && db.isOpen()) {
	        db.close();
	    }
	    if (emf != null) {
	        emf.close();
	    }
	}

	
	// IT 1
	public User getUser(String email) {
	    EntityManager em = emf.createEntityManager();
	    User user = em.find(User.class, email);
	    em.close();
	    return user;
	}
	public boolean saveUser(User user) {
	    EntityManager em = emf.createEntityManager();
	    try {
	        em.getTransaction().begin();
	        em.persist(user);
	        em.getTransaction().commit();
	        return true;
	    } catch (Exception e) {
	        em.getTransaction().rollback();
	        return false;
	    } finally {
	        em.close();
	    }
	}
	
	public List<Ride> getRidesForDriver(String driverId) {
	    EntityManager em = emf.createEntityManager();
	    List<Ride> rides = em.createQuery("SELECT r FROM Ride r WHERE r.driverId = :driverId", Ride.class)
	                          .setParameter("driverId", driverId)
	                          .getResultList();
	    em.close();
	    return rides;
	}
	
	public boolean saveRide(Ride ride) {
	    EntityManager em = emf.createEntityManager();
	    try {
	        em.getTransaction().begin();
	        em.persist(ride);
	        em.getTransaction().commit();
	        return true;
	    } catch (Exception e) {
	        em.getTransaction().rollback();
	        return false;
	    } finally {
	        em.close();
	    }
	}
	
	public Driver getDriver(String email) {
	    EntityManager em = emf.createEntityManager();
	    Driver driver = null;
	    try {
	        driver = em.find(Driver.class, email); // Buscar por email, que es la PK
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        em.close();
	    }
	    return driver;
	}
	
	// para guardar una reserva
	public boolean saveBooking(Booking booking) {
	    EntityManager em = emf.createEntityManager();
	    try {
	        em.getTransaction().begin();
	        em.persist(booking);
	        em.getTransaction().commit();
	        return true;
	    } catch (Exception e) {
	        em.getTransaction().rollback();
	        return false;
	    } finally {
	        em.close();
	    }
	}
	
	// obtener un viaje existente
	public Ride getRide(int rideId) {
	    EntityManager em = emf.createEntityManager();
	    Ride ride = em.find(Ride.class, rideId);
	    em.close();
	    return ride;
	}
	
	// obtener reservar para una Driver
	public List<Booking> getBookingsForDriver(String driverEmail) {
	    EntityManager em = emf.createEntityManager();
	    List<Booking> bookings = em.createQuery(
	        "SELECT b FROM Booking b WHERE b.ride.driver.email = :driverEmail", Booking.class)
	        .setParameter("driverEmail", driverEmail)
	        .getResultList();
	    em.close();
	    return bookings;
	}
	
	public boolean updateRide(Ride ride) {
	    EntityManager em = emf.createEntityManager();
	    try {
	        em.getTransaction().begin();
	        em.merge(ride); // Actualiza el viaje en la base de datos
	        em.getTransaction().commit();
	        return true;
	    } catch (Exception e) {
	        em.getTransaction().rollback();
	        e.printStackTrace();
	        return false;
	    } finally {
	        em.close();
	    }
	}
	
	public List<Ride> getAvailableRides() {
	    EntityManager em = emf.createEntityManager();
	    List<Ride> rides = em.createQuery(
	        "SELECT r FROM Ride r WHERE r.nPlaces > 0", Ride.class) // Solo viajes con asientos disponibles
	        .getResultList();
	    em.close();
	    return rides;
	}
	
	public Booking getPendingBooking(String driverEmail, String passengerEmail) {
	    EntityManager em = emf.createEntityManager();
	    Booking booking = null;

	    try {
	        TypedQuery<Booking> query = em.createQuery(
	            "SELECT b FROM Booking b WHERE b.ride.driver.email = :driverEmail AND b.user.email = :passengerEmail AND b.confirmed = false", 
	            Booking.class);
	        query.setParameter("driverEmail", driverEmail);
	        query.setParameter("passengerEmail", passengerEmail);

	        List<Booking> results = query.getResultList();
	        if (!results.isEmpty()) {
	            booking = results.get(0); // Tomamos la primera reserva encontrada
	        }
	    } finally {
	        em.close();
	    }

	    return booking;
	}
	
	public boolean updateBooking(Booking booking) {
	    EntityManager em = emf.createEntityManager();
	    try {
	        em.getTransaction().begin();
	        em.merge(booking); // Se usa merge() para actualizar la reserva
	        em.getTransaction().commit();
	        return true;
	    } catch (Exception e) {
	        em.getTransaction().rollback();
	        e.printStackTrace();
	        return false;
	    } finally {
	        em.close();
	    }
	}

	public boolean deleteBooking(Booking booking) {
	    EntityManager em = emf.createEntityManager();
	    try {
	        em.getTransaction().begin();
	        Booking b = em.merge(booking); // Asegurar que la entidad está en el contexto de persistencia
	        em.remove(b); // Eliminar la reserva
	        em.getTransaction().commit();
	        return true;
	    } catch (Exception e) {
	        em.getTransaction().rollback();
	        e.printStackTrace();
	        return false;
	    } finally {
	        em.close();
	    }
	}
	
	 public void addUsertobd(User us) {
	        db.getTransaction().begin();
	        try {
	            if (!userExists(us.getEmail())) {  // Verificar si el usuario ya existe
	                db.persist(us);
	                db.getTransaction().commit();
	                System.out.println("User  added: " + us.getEmail());
	            } else {
	                System.out.println("User  already exists: " + us.getEmail());
	                db.getTransaction().rollback();
	            }
	        } catch (Exception e) {
	            if (db.getTransaction().isActive()) {
	                db.getTransaction().rollback();
	            }
	            e.printStackTrace();
	        }
	    }

	    public void addDriver(Driver driver) {
	        db.getTransaction().begin();
	        try {
	            if (!userExists(driver.getEmail())) {  // Verificar si el conductor ya existe
	                db.persist(driver);
	                db.getTransaction().commit();
	                System.out.println("Driver added: " + driver.getEmail());
	            } else {
	                System.out.println("Driver already exists: " + driver.getEmail());
	                db.getTransaction().rollback();
	            }
	        } catch (Exception e) {
	            if (db.getTransaction().isActive()) {
	                db.getTransaction().rollback();
	            }
	            e.printStackTrace();
	        }
	    }


	public boolean userExists(String email) {
        Driver driver = db.find(Driver.class, email);
        if (driver != null) {
            return true;
        }
        User user = db.find(User.class, email);
        return user != null;
    }

	







	
}
