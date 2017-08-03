package org.jay.ticketingservice.service;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.jay.ticketingservice.event.profile.SeatHoldProfile;
import org.jay.ticketingservice.event.profile.SeatProfile;
import org.jay.ticketingservice.event.profile.SeatReservedProfile;
import org.jay.ticketingservice.event.profile.SeatStatus;
import org.jay.ticketingservice.service.seatconfirmation.ISeatConfirmationService;
import org.jay.ticketingservice.service.seatconfirmation.SeatConfirmationService;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author jayanthp
 *
 */
public class SeatConfirmationServiceTest {

  ISeatConfirmationService tested;
  
  int eventId = 123, seatHoldId = 3;
  String customerId = "cust@email.com";
  
  List<SeatProfile> seats;
  List<SeatHoldProfile> seatHolds;
  SeatReservedProfile seatReserved, expectedSeatReserved;
  
  @Before
  public void setUp() throws Exception {
    tested = new SeatConfirmationService();
  }
  
  @Test
  public void testReserveSeats() {
    SeatHoldProfile seatHold;
    
    seats = new ArrayList<>();
    seats.add(new SeatProfile("A1", SeatStatus.ON_HOLD) );
    seats.add(new SeatProfile("A2", SeatStatus.ON_HOLD) );
    
    seatHold = new SeatHoldProfile(seatHoldId, seats);
    seatReserved = tested.reserveSeats(eventId, seatHold, customerId);
    
    expectedSeatReserved = new SeatReservedProfile("1", seatHold.getSeatIds(), customerId);
    
    assertEquals(seatHold.getSeatIds(),seatReserved.getSeatIds());
    assertEquals(customerId,seatReserved.getCustomerId());
    assertEquals("1",seatReserved.getConfirmationId());
  }
  
  
}
