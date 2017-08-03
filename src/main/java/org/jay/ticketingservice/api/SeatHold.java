package org.jay.ticketingservice.api;

import java.util.*;

public interface SeatHold {
  
  public List<String> getSeatIds();
  
  public Integer getSeatHoldId();
  
  public String getCustomerId();
}
