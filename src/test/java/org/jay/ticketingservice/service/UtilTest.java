package org.jay.ticketingservice.service;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jay.ticketingservice.event.profile.SeatReservedProfile;
import org.jay.ticketingservice.service.util.Util;
import org.junit.Test;

public class UtilTest {

  @Test
  public void testAreSeaIdsInSameRow() {
   assertTrue(Util.areSeatIdsInSameRow(Arrays.asList("A-1","A-2","A-3")));
   assertFalse(Util.areSeatIdsInSameRow(Arrays.asList("A-1","A-2","B-1")));
   assertTrue(Util.areSeatIdsInSameRow(Arrays.asList("A-1","A-3","A-2")));
   assertTrue(Util.areSeatIdsInSameRow(Arrays.asList("A-1","A-111","A-11")));
  }
  
  @Test
  public void testNumReservedSeatsInSameRow() {
    List<SeatReservedProfile> seatReservedProfiles = new ArrayList<>();
    seatReservedProfiles.add( new SeatReservedProfile("confid1", 
        Arrays.asList("A-1","A-2","A-3"), "cust1"));
    seatReservedProfiles.add( new SeatReservedProfile("confid1", 
        Arrays.asList("A-1","A-2","B-1"), "cust1"));
    seatReservedProfiles.add( new SeatReservedProfile("confid1", 
        Arrays.asList("A-1","A-3","A-2"), "cust1"));
    seatReservedProfiles.add( new SeatReservedProfile("confid1", 
        Arrays.asList("A-1","A-111","A-11"), "cust1"));
    int[] result = Util.numReservedSeatsInSameRow(seatReservedProfiles);
    assertEquals(4, result[0]);
    assertEquals(3, result[1]);
  }  
  
  @Test
   public void testAreSeatsContinous() {
    assertTrue(Util.areSeatIdsContinous(Arrays.asList("A-1","A-2","A-3")));
    assertFalse(Util.areSeatIdsContinous(Arrays.asList("A-1","A-2","B-1")));
    assertTrue(Util.areSeatIdsContinous(Arrays.asList("A-1","A-3","A-2")));
    assertFalse(Util.areSeatIdsContinous(Arrays.asList("A-1","A-111","A-11")));
   }
  
  @Test
  public void testNumReservedSeatsContinous() {
    List<SeatReservedProfile> seatReservedProfiles = new ArrayList<>();
    seatReservedProfiles.add( new SeatReservedProfile("confid1", 
        Arrays.asList("A-1","A-2","A-3"), "cust1"));
    seatReservedProfiles.add( new SeatReservedProfile("confid1", 
        Arrays.asList("A-1","A-2","B-1"), "cust1"));
    seatReservedProfiles.add( new SeatReservedProfile("confid1", 
        Arrays.asList("A-1","A-3","A-2"), "cust1"));
    seatReservedProfiles.add( new SeatReservedProfile("confid1", 
        Arrays.asList("A-1","A-111","A-11"), "cust1"));
    int[] result = Util.numReservedSeatsContinous(seatReservedProfiles);
    assertEquals(4, result[0]);
    assertEquals(2, result[1]);
  }
}
