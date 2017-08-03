package org.jay.ticketingservice.service.seatconfirmation;


import org.jay.ticketingservice.event.profile.SeatHoldProfile;
import org.jay.ticketingservice.event.profile.SeatReservedProfile;

/**
 * 
 * @author jayanthp
 *
 */
public interface ISeatConfirmationService {

  SeatReservedProfile reserveSeats(Integer eventId, SeatHoldProfile seatHold, String customerId);

  int numSeatsReserved(Integer eventId);
   
}
