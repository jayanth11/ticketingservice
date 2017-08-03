package org.jay.ticketingservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.log4j.Logger;
import org.jay.ticketingservice.event.profile.SeatHoldProfile;
import org.jay.ticketingservice.event.profile.SeatProfile;
import org.jay.ticketingservice.event.profile.SeatStatus;
import org.jay.ticketingservice.service.seatholding.ISeatHoldExpirationListener;
import org.jay.ticketingservice.service.seatholding.SeatHoldingService;
import org.junit.Before;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 
 * @author jayanthp
 *
 */
public class SeatHoldingServiceTest {
  
  protected static final Logger LOGGER = Logger.getLogger(SeatHoldingServiceTest.class);

  SeatHoldingService tested;
  ISeatHoldExpirationListener seatHoldExpirationListener;
  List<SeatHoldProfile> seatHoldsExpired;
  
  @Before
  public void setUp() throws Exception {
    tested = new SeatHoldingService();
    Whitebox.setInternalState(tested, "timer", Mockito.mock(ScheduledThreadPoolExecutor.class));
    
    tested.setSeatHoldTimeIntervalSecs(4);
    tested.setTimerIntervalInSecs(1);
    tested.setTimerInitDelayInSecs(1);
    
    seatHoldExpirationListener = new ISeatHoldExpirationListener() {
      @Override
      public void onSeatHoldExpiration(List<SeatHoldProfile> seatHolds) {
        seatHoldsExpired = seatHolds;
        LOGGER.info("seatHoldsExpired: " + seatHoldsExpired);
      } 
    };
    tested.addExpirationListener(seatHoldExpirationListener);
    
    tested.init();
    Integer seatHoldTimeIntervalInClockTicks = (Integer) Whitebox.getInternalState(tested, "seatHoldTimeIntervalInClockTicks");
    assertEquals(new Integer(4),seatHoldTimeIntervalInClockTicks);
  }
  
  @Test
  public void testHoldSeats() {
    List<SeatProfile> seatIds;
    List<SeatHoldProfile> seatHolds = new ArrayList<>();
    
    seatIds = new ArrayList<>();
    seatIds.add(new SeatProfile("A-1", SeatStatus.AVAILABLE));
    seatIds.add(new SeatProfile("A-2", SeatStatus.AVAILABLE ));
    seatHolds.add( tested.holdSeats(seatIds) );
    
    seatIds = new ArrayList<>();
    seatIds.add(new SeatProfile("B-11", SeatStatus.AVAILABLE));
    seatIds.add(new SeatProfile("B-12", SeatStatus.AVAILABLE));
    seatHolds.add( tested.holdSeats(seatIds) );
    
    tested.handleTimerEvent();
    assertNull(seatHoldsExpired);
    
    seatIds = new ArrayList<>();
    seatIds.add(new SeatProfile("C-5", SeatStatus.AVAILABLE));
    seatIds.add(new SeatProfile("C-18", SeatStatus.AVAILABLE));
    seatIds.add(new SeatProfile("C-20", SeatStatus.AVAILABLE));
    seatHolds.add( tested.holdSeats(seatIds) );
    
    for(SeatHoldProfile seatHold : seatHolds) {
      assertEquals(seatHold,tested.getSeatHold(seatHold.getSeatHoldId()));
    }
    
    for(SeatHoldProfile seatHold : seatHolds) {
      assertEquals(seatHold,tested.removeSeatHold(seatHold.getSeatHoldId()));
    }
  }
  
  @Test
  public void testExpiredSeatHolds() {
    List<SeatProfile> seatIds;
    List<SeatHoldProfile> seatHolds = new ArrayList<>();
    
    seatIds = new ArrayList<>();
    seatIds.add(new SeatProfile("A1", SeatStatus.AVAILABLE));
    seatIds.add(new SeatProfile("A2", SeatStatus.AVAILABLE));
    seatHolds.add( tested.holdSeats(seatIds) );

    tested.handleTimerEvent();
    assertNull(seatHoldsExpired);
    
    seatIds = new ArrayList<>();
    seatIds.add(new SeatProfile("B11", SeatStatus.AVAILABLE));
    seatIds.add(new SeatProfile("B12", SeatStatus.AVAILABLE));
    seatHolds.add( tested.holdSeats(seatIds) );

    seatIds = new ArrayList<>();
    seatIds.add(new SeatProfile("C5", SeatStatus.AVAILABLE));
    seatIds.add(new SeatProfile("C18", SeatStatus.AVAILABLE));
    seatIds.add(new SeatProfile("C20", SeatStatus.AVAILABLE));
    seatHolds.add( tested.holdSeats(seatIds) );
    
    tested.handleTimerEvent();
    assertNull(seatHoldsExpired);
 
    seatIds = new ArrayList<>();
    seatIds.add(new SeatProfile("D4", SeatStatus.AVAILABLE));
    seatIds.add(new SeatProfile("D8", SeatStatus.AVAILABLE));
    seatIds.add(new SeatProfile("D9", SeatStatus.AVAILABLE));
    seatHolds.add( tested.holdSeats(seatIds) );

    tested.handleTimerEvent();
    assertNull(seatHoldsExpired);
    
    seatIds = new ArrayList<>();
    seatIds.add(new SeatProfile("A5", SeatStatus.AVAILABLE));
    seatIds.add(new SeatProfile("B7", SeatStatus.AVAILABLE));
    seatIds.add(new SeatProfile("C10", SeatStatus.AVAILABLE));
    seatHolds.add( tested.holdSeats(seatIds) );
    
    tested.handleTimerEvent();
    assertNull(seatHoldsExpired);
    
    tested.handleTimerEvent();
    assertEquals(seatHolds.subList(0,1), seatHoldsExpired);
    
    tested.handleTimerEvent();
    assertEquals(seatHolds.subList(1,3), seatHoldsExpired);
    
    tested.handleTimerEvent();
    assertEquals(seatHolds.subList(3,4), seatHoldsExpired);
    
    tested.handleTimerEvent();
    assertEquals(seatHolds.subList(4,5), seatHoldsExpired); 
  }
  
}
