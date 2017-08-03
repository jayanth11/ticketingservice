package org.jay.ticketingservice.service.seatconfirmation;

import java.util.concurrent.atomic.AtomicInteger;

import org.jay.ticketingservice.event.profile.SeatHoldProfile;
import org.jay.ticketingservice.event.profile.SeatReservedProfile;

/**
 * 
 * @author jayanthp
 *
 */
public class SeatConfirmationService implements ISeatConfirmationService {
  
  protected AtomicInteger confirmationIdCounter = new AtomicInteger();
  protected AtomicInteger numReservedSeats = new AtomicInteger();

  @Override
  public SeatReservedProfile reserveSeats(Integer eventId, SeatHoldProfile seatHold,
      String customerId) {
    Integer confCodeInt = confirmationIdCounter.incrementAndGet();
    String confCode = confCodeInt.toString();
    SeatReservedProfile reservedSeatProfile = new SeatReservedProfile( confCode, seatHold.getSeatIds(), customerId );
    numReservedSeats.addAndGet(seatHold.getSeatIds().size());
    return reservedSeatProfile;
  }
 
  @Override  
  public int numSeatsReserved(Integer eventId) {
    return numReservedSeats.get();
  }



  

}
