package org.jay.ticketingservice.service.bestseatvalue;

import org.jay.ticketingservice.venue.Row;
import org.jay.ticketingservice.venue.Venue;

public class LeastRowId implements IBestSeatValueService {
  
  private Venue venue;
  
  @Override
  public int calculateBestValue(Row row, int seatNumber) {
    return row.getId().hashCode();
  }
  
  @Override
  public void setVenue(Venue venue) {
    this.venue = venue;
  }

}
