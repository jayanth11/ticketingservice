package org.jay.ticketingservice.service;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.jay.ticketingservice.event.profile.SeatProfile;
import org.jay.ticketingservice.event.profile.SeatStatus;
import org.jay.ticketingservice.service.bestseatvalue.IBestSeatValueService;
import org.jay.ticketingservice.service.bestseatvalue.LeastRowId;
import org.jay.ticketingservice.service.seatallocation.FirstBestSeatAvailableStrategy;
import org.jay.ticketingservice.service.seatallocation.ISeatAllocationService;
import org.jay.ticketingservice.service.util.PriorityQueueEntry;
import org.jay.ticketingservice.venue.Row;
import org.jay.ticketingservice.venue.Venue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

/**
 * 
 * @author jayanthp
 *
 */
public class FirstBestSeatAvailableStrategyTest {
  
  protected static final Logger LOGGER = Logger.getLogger(FirstBestSeatAvailableStrategyTest.class);
  
  ISeatAllocationService tested;
  IBestSeatValueService bestSeatValueService;
  Queue<PriorityQueueEntry<SeatProfile>> availableSeats;
  
  Venue venue = new Venue();
  int eventId = 123;
  String customerId = "cust@email.com";
  
  List<SeatProfile> seats, expectedSeats;
  
  @SuppressWarnings({"unchecked"})
  @Before
  public void setUp() throws Exception {
    setUpVenue();
    
    bestSeatValueService = new LeastRowId();
    bestSeatValueService.setVenue(venue);
    
    tested = new FirstBestSeatAvailableStrategy(); 
    tested.setBestSeatValueService(bestSeatValueService);
    
    availableSeats = (Queue<PriorityQueueEntry<SeatProfile>>) Whitebox.getInternalState(tested, "availableSeats");
    expectedSeats = new ArrayList<>();
  }
  
  @Test
  public void tesInit() {
    tested.intializeForNewEvent(eventId, venue);
    assertEquals(venue.numSeatsInVenue(),tested.numSeatsAvailable(eventId));
    LOGGER.info("availableSeats: " + availableSeats);
  }
  
  @Test
  public void testFindSeats() {
    tesInit();
    seats = tested.findAndHoldSeats(eventId, 3, customerId);
    LOGGER.info("seats: " + seats);
    
    assertEquals(venue.numSeatsInVenue() - 3,tested.numSeatsAvailable(eventId));
    expectedSeats.add(new SeatProfile("A-0",SeatStatus.ON_HOLD));
    expectedSeats.add(new SeatProfile("A-1",SeatStatus.ON_HOLD));
    expectedSeats.add(new SeatProfile("A-2",SeatStatus.ON_HOLD));
    assertEquals(expectedSeats,seats);
    
    seats = tested.findAndHoldSeats(eventId, 2, customerId);
    assertEquals(venue.numSeatsInVenue() - 5,tested.numSeatsAvailable(eventId));
    expectedSeats.add(new SeatProfile("A-3", SeatStatus.ON_HOLD));
    expectedSeats.add(new SeatProfile("A-4", SeatStatus.ON_HOLD));
    assertEquals(expectedSeats.subList(3, 5),seats);
    
    seats = tested.findAndHoldSeats(eventId, 2, customerId);
    assertEquals(venue.numSeatsInVenue() - 7,tested.numSeatsAvailable(eventId));
    expectedSeats.add(new SeatProfile("A-5",SeatStatus.ON_HOLD));
    expectedSeats.add(new SeatProfile("A-6",SeatStatus.ON_HOLD));
    assertEquals(expectedSeats.subList(5, 7),seats);
    
    seats = tested.findAndHoldSeats(eventId, 3, customerId);
    assertEquals(venue.numSeatsInVenue() - 10,tested.numSeatsAvailable(eventId));    
    expectedSeats.add(new SeatProfile("A-7",SeatStatus.ON_HOLD));
    expectedSeats.add(new SeatProfile("B-0",SeatStatus.ON_HOLD));
    expectedSeats.add(new SeatProfile("B-1",SeatStatus.ON_HOLD));
    assertEquals(expectedSeats.subList(7, 10),seats);
    
    tested.unHoldSeats(eventId, expectedSeats.subList(3, 5));
    assertEquals(venue.numSeatsInVenue() - 8,tested.numSeatsAvailable(eventId));
    
    seats = tested.findAndHoldSeats(eventId, 3, customerId);
    assertEquals(venue.numSeatsInVenue() - 11,tested.numSeatsAvailable(eventId));    
    expectedSeats.add(new SeatProfile("A-3",SeatStatus.ON_HOLD));
    expectedSeats.add(new SeatProfile("A-4",SeatStatus.ON_HOLD));
    expectedSeats.add(new SeatProfile("B-2",SeatStatus.ON_HOLD));
    assertEquals(expectedSeats.subList(10, 13),seats);
    
    tested.reserveSeats(eventId, expectedSeats.subList(0, 3), customerId);
    assertEquals(expectedSeats.get(0).getStatus(),SeatStatus.RESERVED);
    assertEquals(expectedSeats.get(1).getStatus(),SeatStatus.RESERVED);
    assertEquals(expectedSeats.get(2).getStatus(),SeatStatus.RESERVED);
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
