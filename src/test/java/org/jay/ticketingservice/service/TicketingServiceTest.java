package org.jay.ticketingservice.service;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jay.ticketingservice.api.SeatHold;
import org.jay.ticketingservice.event.profile.SeatProfile;
import org.jay.ticketingservice.event.profile.SeatStatus;
import org.jay.ticketingservice.service.TicketingService;
import org.jay.ticketingservice.service.bestseatvalue.LeastRowId;
import org.jay.ticketingservice.service.seatallocation.FirstBestSeatAvailableStrategy;
import org.jay.ticketingservice.service.seatallocation.ISeatAllocationService;
import org.jay.ticketingservice.service.seatconfirmation.ISeatConfirmationService;
import org.jay.ticketingservice.service.seatconfirmation.SeatConfirmationService;
import org.jay.ticketingservice.service.seatholding.ISeatHoldingService;
import org.jay.ticketingservice.service.seatholding.SeatHoldingService;
import org.jay.ticketingservice.service.util.Util;
import org.jay.ticketingservice.venue.Row;
import org.jay.ticketingservice.venue.Venue;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author jayanthp
 *
 */
public class TicketingServiceTest {
  
  protected static final Logger LOGGER = Logger.getLogger(FirstBestSeatAvailableStrategyTest.class);

  TicketingService tested;
  Venue venue;
  ISeatAllocationService seatAllocationService;
  ISeatHoldingService seatHoldingService;
  ISeatConfirmationService seatConfirmationService;  
  int eventId = 123;
  
  String customerId = "cust@email.com";
  List<SeatProfile> seats, expectedSeats;
  
  List<String> expectedSeatIds;
  SeatHold seatHold;
  
  @Before
  public void setUp() throws Exception {
    tested = new TicketingService();
    setUpVenue();
    seatAllocationService = new FirstBestSeatAvailableStrategy();
    seatAllocationService.setBestSeatValueService(new LeastRowId());
    
    seatHoldingService = new SeatHoldingService();
    seatHoldingService.setSeatHoldTimeIntervalSecs(4);
    seatHoldingService.setTimerIntervalInSecs(1);
    seatHoldingService.setTimerInitDelayInSecs(1);
    
    seatConfirmationService = new SeatConfirmationService();
    tested.setVenue(venue);
    tested.setSeatAllocationService(seatAllocationService);
    tested.setSeatConfirmationService(seatConfirmationService);
    tested.setSeatHoldingService(seatHoldingService);
    tested.setCurrentEventIdForBooking(eventId);
    
    expectedSeatIds = new ArrayList<>();
    expectedSeats  = new ArrayList<>();
  }
  
  @Test
  public void tesInit() throws Exception {
    tested.init();
    assertEquals(venue.numSeatsInVenue(),tested.numSeatsAvailable());
  }
  
  @Test
  public void testFindSeats() throws Exception {
    tesInit();
    seatHold = tested.findAndHoldSeats(3, customerId);
    LOGGER.info("seatHold: " + seatHold);
    
    assertEquals(venue.numSeatsInVenue() - 3,tested.numSeatsAvailable());
    expectedSeatIds.add("A-0");
    expectedSeatIds.add("A-1");
    expectedSeatIds.add("A-2");
    assertEquals(expectedSeatIds,seatHold.getSeatIds());
    
    seatHold = tested.findAndHoldSeats(2, customerId);
    assertEquals(venue.numSeatsInVenue() - 5,tested.numSeatsAvailable());
    expectedSeatIds.add("A-3");
    expectedSeatIds.add("A-4");
    assertEquals(expectedSeatIds.subList(3,5),seatHold.getSeatIds());
    
    seatHold = tested.findAndHoldSeats(2, customerId);
    assertEquals(venue.numSeatsInVenue() - 7,tested.numSeatsAvailable());
    expectedSeatIds.add("A-5");
    expectedSeatIds.add("A-6");
    assertEquals(expectedSeatIds.subList(5,7),seatHold.getSeatIds());
    
    seatHold = tested.findAndHoldSeats(3, customerId);
    assertEquals(venue.numSeatsInVenue() - 10,tested.numSeatsAvailable()); 
    expectedSeatIds.add("A-7");
    expectedSeatIds.add("B-0");
    expectedSeatIds.add("B-1");
    assertEquals(expectedSeatIds.subList(7, 10),seatHold.getSeatIds());
    
    seatAllocationService.unHoldSeats(eventId, Util.createSeatProfiles(expectedSeatIds.subList(3, 5), 
        SeatStatus.ON_HOLD));
    assertEquals(venue.numSeatsInVenue() - 8,tested.numSeatsAvailable());
    
    seatHold = tested.findAndHoldSeats(3, customerId);
    assertEquals(venue.numSeatsInVenue() - 11,tested.numSeatsAvailable());    
    expectedSeatIds.add("A-3");
    expectedSeatIds.add("A-4");
    expectedSeatIds.add("B-2");
    assertEquals(expectedSeatIds.subList(10, 13),seatHold.getSeatIds());
  }  
  
  private void setUpVenue() {
    venue = new Venue();
    List<Row> rows = new ArrayList<>();
    venue.setRows(rows);
    
    Row row;
    
    row = new Row();
    row.setId("A");
    row.setNumOfSeats(8);
    rows.add(row);
    
    row = new Row();
    row.setId("B");
    row.setNumOfSeats(7);
    rows.add(row);
    
    row = new Row();
    row.setId("C");
    row.setNumOfSeats(6);
    rows.add(row);
    
    row = new Row();
    row.setId("D");
    row.setNumOfSeats(5);    
    rows.add(row);
  }
  
}
