package org.jay.ticketingservice.service.seatallocation;

import java.util.List;

import org.jay.ticketingservice.service.bestseatvalue.IBestSeatValueService;
import org.jay.ticketingservice.venue.Venue;
import org.jay.ticketingservice.event.profile.SeatProfile;

public interface ISeatAllocationService {
  
  int numSeatsAvailable(Integer eventId);
  
  List<SeatProfile> findAndHoldSeats(Integer eventId, int numSeats, String customerId);
  
  void reserveSeats(Integer eventId, List<SeatProfile> seatProfiles, String customerId);

  void unHoldSeats(Integer eventId, List<SeatProfile> seatProfiles);

  void intializeForNewEvent(Integer eventId, Venue venue);
  
  void setBestSeatValueService(IBestSeatValueService bestSeatValueService);
  
}
