package org.jay.ticketingservice.service.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.jay.ticketingservice.event.profile.SeatHoldProfile;
import org.jay.ticketingservice.event.profile.SeatReservedProfile;

/**
 * 
 * @author jayanthp
 *
 */
public class MockDataBase {

  protected Map<Integer,SeatHoldProfile> allExpiredSeatHolds = new ConcurrentSkipListMap<>();
  
  protected Map<String,SeatReservedProfile> reservedSeatsProfiles = new ConcurrentSkipListMap<>();
  
  public void putExpiredSeatHold(SeatHoldProfile seatHold) {
    allExpiredSeatHolds.put(seatHold.getSeatHoldId(),seatHold);
  }
  
  public List<SeatHoldProfile> getAllExpiredSeatHolds(Integer eventId) {
    return new ArrayList<>(allExpiredSeatHolds.values());
  }
  
  public int numSeatHoldsExpired(Integer eventId) {
    return allExpiredSeatHolds.size();
  }
  
  public void putReservedSeatProfile(SeatReservedProfile reservedSeatProfile) {
    reservedSeatsProfiles.put(reservedSeatProfile.getConfirmationId(), reservedSeatProfile);
  }
  
  public List<SeatReservedProfile> getAllReservedSeatsProfiles(Integer eventId) {
    return new ArrayList<>(reservedSeatsProfiles.values());
  }
  
  public int numReservedSeatsProfiles(Integer eventId) {
    return reservedSeatsProfiles.size();
  }
}
