package org.jay.ticketingservice.service.bestseatvalue;

import org.jay.ticketingservice.venue.Row;
import org.jay.ticketingservice.venue.Venue;

public interface IBestSeatValueService {

  int calculateBestValue(Row row, int seatNumber);
  
  void setVenue(Venue venue);
  
//  void setWeightagePolicy(WeightagePolicy weightagePolicy);
}
