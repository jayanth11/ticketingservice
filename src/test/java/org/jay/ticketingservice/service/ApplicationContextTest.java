package org.jay.ticketingservice.service;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.jay.ticketingservice.service.ApplicationContext;
import org.jay.ticketingservice.service.TicketingService;
import org.jay.ticketingservice.venue.Venue;
import org.jay.ticketingservice.venue.Zone.DIRECTION;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

/**
 * 
 * @author jayanthp
 *
 */
public class ApplicationContextTest {

  ApplicationContext tested;
  Venue venue;
  
  @Before
  public void setUp() throws Exception {
    tested = new ApplicationContext("application.properties.junit");
  }
  
  @Test
  public void testLoad() throws Exception {
    TicketingService ticketingService = tested.loadApplication();
    venue = (Venue) Whitebox.getInternalState(tested, "venue");
    
    assertEquals("venue-config-junit.json",tested.getVenueJSONConfigFile()); 
    assertEquals("Venue Name",venue.getName());
    
    assertEquals("AA",venue.getRows().get(0).getId());
    assertEquals(6,venue.getRows().get(0).getNumOfSeats());
    assertEquals("VIP-1",venue.getRows().get(0).getZoneId());
    assertEquals(1,venue.getRows().get(0).getDistanceFromStage());
    
    assertEquals("VIP-1",venue.getZones().get(0).getId());
    assertEquals( new BigDecimal("100.00"),venue.getZones().get(0).getPrice());
    assertEquals(DIRECTION.STAGE_FRONT,venue.getZones().get(0).getDirection());    
    
    assertEquals(12,ticketingService.numSeatsInVenue());  
    assertEquals(0,ticketingService.numSeatsAvailable());
    
    assertEquals("org.jay.ticketingservice.service.seatallocation.FirstBestSeatAvailableStrategy",
        tested.getSeatAllocationServiceStrategyClass());        
    assertEquals("org.jay.ticketingservice.service.bestseatvalue.LeastRowId",
        tested.getBestSeatValueServiceStrategyClass());
    
    assertEquals((Integer)123,tested.getCurrentEventIdForBooking());
    assertEquals((Integer)4,tested.getSeatHoldTimeIntervalSecs());    
    assertEquals((Integer)1,tested.geTimerIntervalInSecs());
    assertEquals((Integer)1,tested.getTimerInitDelayInSecs());    

  }
  
}
