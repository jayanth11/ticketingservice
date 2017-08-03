package org.jay.ticketingservice.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.jay.ticketingservice.api.SeatHold;
import org.jay.ticketingservice.api.TicketService;
import org.jay.ticketingservice.dto.SeatHoldApi;
import org.jay.ticketingservice.event.profile.SeatHoldProfile;
import org.jay.ticketingservice.event.profile.SeatProfile;
import org.jay.ticketingservice.event.profile.SeatReservedProfile;
import org.jay.ticketingservice.service.seatallocation.ISeatAllocationService;
import org.jay.ticketingservice.service.seatconfirmation.ISeatConfirmationService;
import org.jay.ticketingservice.service.seatholding.ISeatHoldExpirationListener;
import org.jay.ticketingservice.service.seatholding.ISeatHoldingService;
import org.jay.ticketingservice.service.util.MockDataBase;
import org.jay.ticketingservice.venue.Venue;

/**
 * 
 * @author jayanthp
 *
 */
public class TicketingService implements TicketService, ISeatHoldExpirationListener, IProcessState {
  
  protected static final Logger LOGGER = Logger.getLogger(TicketingService.class);
  
  private Venue venue;
  private Integer currentEventIdForBooking;
  
  private ISeatAllocationService seatAllocationService;
  private ISeatHoldingService seatHoldingService;
  private ISeatConfirmationService seatConfirmationService;
  private MockDataBase database;
  
  public TicketingService() {
  }
  
  public void init() throws Exception {
    LOGGER.info("initialize  TicketingService ");
    seatAllocationService.intializeForNewEvent(currentEventIdForBooking, venue);
    seatHoldingService.init();
  }

  public void start() throws Exception {
    seatHoldingService.start();
  }
  
  public void shutdown() throws Exception {
    seatHoldingService.shutdown();
  }  
    
  @Override
  public int numSeatsAvailable() {
    return seatAllocationService.numSeatsAvailable(currentEventIdForBooking);
  }

  @Override
  public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
    if(0 >= numSeats) {
      return null;
    }
    int numSeatsAvailable = seatAllocationService.numSeatsAvailable(currentEventIdForBooking); 
    if (numSeats > numSeatsAvailable ) {
      return null;
    }
    List<SeatProfile> seatProfiles = seatAllocationService.findAndHoldSeats(currentEventIdForBooking, numSeats, customerEmail);
    if(null == seatProfiles) {
      LOGGER.warn("Possible concurrency issue : got 0 seats but "
          + "expected confirmed numSeatsAvaiable = " + numSeats );
      return null;
    }
    SeatHoldProfile seatHoldProfile = seatHoldingService.holdSeats(seatProfiles);
    seatHoldProfile.setCustomerId(customerEmail);
    return new SeatHoldApi(seatHoldProfile.getSeatHoldId(), seatHoldProfile.getSeatIds(),
        customerEmail, seatHoldProfile.getTimeToExpire() );
  }

  @Override
  public String reserveSeats(int seatHoldId, String customerEmail) {
    SeatHoldProfile seatHoldProfile = seatHoldingService.removeSeatHold(seatHoldId);
    if(null == seatHoldProfile || !seatHoldProfile.getCustomerId().equals(customerEmail)) {
      return null;
    }
    seatAllocationService.reserveSeats(currentEventIdForBooking, seatHoldProfile.getSeatProfiles(), customerEmail );    
    SeatReservedProfile seatReservedProfile = seatConfirmationService.reserveSeats(currentEventIdForBooking, 
        seatHoldProfile, customerEmail);
    database.putReservedSeatProfile(seatReservedProfile);
    return seatReservedProfile.getConfirmationId();
  }
  
  @Override
  public void onSeatHoldExpiration(List<SeatHoldProfile> seatHolds) {
    for(SeatHoldProfile seatHold: seatHolds) { 
      seatAllocationService.unHoldSeats(currentEventIdForBooking, seatHold.getSeatProfiles());
      database.putExpiredSeatHold(seatHold);
    }
  }
  
  public int numSeatsInVenue() {
    return venue.numSeatsInVenue();
  }
  
  public int numSeatsReserved() {
    return seatConfirmationService.numSeatsReserved(currentEventIdForBooking);
  }

  public void setVenue(Venue venue) {
    this.venue = venue;
  }

  public void setCurrentEventIdForBooking(Integer currentEventIdForBooking) {
    this.currentEventIdForBooking = currentEventIdForBooking;
  }

  public void setSeatAllocationService(ISeatAllocationService seatAllocationService) {
    this.seatAllocationService = seatAllocationService;
  }

  public void setSeatHoldingService(ISeatHoldingService seatHoldingService) {
    this.seatHoldingService = seatHoldingService;
  }

  public void setSeatConfirmationService(ISeatConfirmationService seatConfirmationService) {
    this.seatConfirmationService = seatConfirmationService;
  }

  public void setDatabase(MockDataBase database) {
    this.database = database;
  }

}
