package org.jay.ticketingservice.service.seatallocation;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jay.ticketingservice.event.profile.SeatProfile;
import org.jay.ticketingservice.event.profile.SeatStatus;
import org.jay.ticketingservice.service.bestseatvalue.IBestSeatValueService;
import org.jay.ticketingservice.service.util.PriorityQueueEntry;
import org.jay.ticketingservice.service.util.Util;
import org.jay.ticketingservice.venue.Row;

public abstract class AbstractSeatAllocationService implements ISeatAllocationService {

  protected static final Logger LOGGER = Logger.getLogger(AbstractSeatAllocationService.class);
  
  IBestSeatValueService bestSeatValueService;
  
  protected Map<String,Integer> seatPriorityMap = new HashMap<>();
  
  @Override 
  public void setBestSeatValueService(IBestSeatValueService bestSeatValueService) {
    this.bestSeatValueService = bestSeatValueService;
  }
  
  protected PriorityQueueEntry<SeatProfile> createPriorityQueueEntry(Row row, Integer seatNumber) {
    String seatId = Util.createSeatId(row, seatNumber);
    SeatProfile seatProfile = new SeatProfile(seatId,SeatStatus.AVAILABLE);
    int priority = bestSeatValueService.calculateBestValue(row,seatNumber);
    seatPriorityMap.put(seatId, priority);
    PriorityQueueEntry<SeatProfile> entry = new PriorityQueueEntry<SeatProfile>(seatProfile,priority);
    if(LOGGER.isTraceEnabled()) {
      LOGGER.trace("entry: " + entry);
    }
    return entry;
  }
  
  protected PriorityQueueEntry<SeatProfile> createPriorityQueueEntry(SeatProfile seatProfile) {
    Integer priority = seatPriorityMap.get(seatProfile.getSeatId());
    seatProfile.setStatus(SeatStatus.AVAILABLE);
    PriorityQueueEntry<SeatProfile> entry = new PriorityQueueEntry<SeatProfile>(seatProfile,priority);
    return entry;
  }
}
