package org.jay.ticketingservice.service.seatallocation;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.log4j.Logger;
import org.jay.ticketingservice.event.profile.SeatProfile;
import org.jay.ticketingservice.event.profile.SeatStatus;
import org.jay.ticketingservice.service.util.PriorityQueueEntry;
import org.jay.ticketingservice.venue.Row;
import org.jay.ticketingservice.venue.Venue;

public class FirstBestSeatAvailableStrategy extends AbstractSeatAllocationService {
  
  protected static final Logger LOGGER = Logger.getLogger(FirstBestSeatAvailableStrategy.class);

//  protected PriorityQueue<PriorityQueueEntry<SeatProfile>> availableSeats =
//      new PriorityQueue<PriorityQueueEntry<SeatProfile>>();
  
  protected PriorityBlockingQueue<PriorityQueueEntry<SeatProfile>> availableSeats =
      new PriorityBlockingQueue<PriorityQueueEntry<SeatProfile>>();

  @Override
  public void intializeForNewEvent(Integer eventId, Venue venue) {
    for(Row row: venue.getRows()) {
      for(int seatNumber =0; seatNumber<row.getNumOfSeats(); seatNumber++ ) {
        PriorityQueueEntry<SeatProfile> entry = createPriorityQueueEntry(row,seatNumber);
        if(LOGGER.isTraceEnabled()) {
          LOGGER.trace("entry: " + entry);
        }
        availableSeats.add(entry);
      }
    }
  }
  
  @Override
  public void unHoldSeats(Integer eventId, List<SeatProfile> seatProfiles) {
    for(SeatProfile seatProfile: seatProfiles) {
      PriorityQueueEntry<SeatProfile> entry = createPriorityQueueEntry(seatProfile);
      availableSeats.add( entry );
    }
  }  
  
  @Override
  public int numSeatsAvailable(Integer eventId) {
    return availableSeats.size();
  }

  @Override
  public List<SeatProfile> findAndHoldSeats(Integer eventId, int numSeats, String customerId) {
    List<SeatProfile> holdSeatProfiles = new ArrayList<>();
    List<PriorityQueueEntry<SeatProfile>> items = new ArrayList<>();
    for (int count = 0; count < numSeats; count++) {
      PriorityQueueEntry<SeatProfile> item = availableSeats.poll();
      if (null == item) {
        break;
      } else {
        SeatProfile seatProfile = item.getEntry();
        seatProfile.setStatus(SeatStatus.ON_HOLD);
        holdSeatProfiles.add(seatProfile);
        items.add(item);
      }
    }
    if(LOGGER.isTraceEnabled()) {
      LOGGER.trace("holdSeatProfiles: " + holdSeatProfiles);
    }
    if(numSeats < holdSeatProfiles.size()) {
      LOGGER.warn("Possible concurrency issue : got " + 
          holdSeatProfiles.size() + " seats but expected numSeatsAvaiable = " + numSeats );
      // add elements back to the list
      for(PriorityQueueEntry<SeatProfile> item: items) {
        availableSeats.add(item);
      }
      return null;
    }
    return holdSeatProfiles;
  }

  @Override
  public void reserveSeats(Integer eventId, List<SeatProfile> seatProfiles, String customerId) {
    for(SeatProfile seatProfile: seatProfiles) {
      seatProfile.setStatus(SeatStatus.RESERVED);
    }
  }
  
}
