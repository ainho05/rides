package businessLogic;
import java.util.Date;
import exceptions.RideMustBeLaterThanTodayException;

public class RideValidator {
	public void validateRideDate(Date rideDate) throws RideMustBeLaterThanTodayException {
        Date today = new Date(); // Fecha actual
        
        if (rideDate.before(today)) {
            throw new RideMustBeLaterThanTodayException("La fecha del viaje no puede ser anterior a hoy.");
        }
        
    }
}
