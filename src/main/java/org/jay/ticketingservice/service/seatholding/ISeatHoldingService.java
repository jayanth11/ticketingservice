package org.jay.ticketingservice.service.seatholding;

import java.util.List;

import org.jay.ticketingservice.event.profile.SeatHoldProfile;
import org.jay.ticketingservice.event.profile.SeatProfile;
import org.jay.ticketingservice.service.IProcessState;

public interface ISeatHoldingService extends IProcessState {

  SeatHoldProfile holdSeats(List<SeatProfile> seatIds);
  
  SeatHoldProfile getSeatHold(Integer seatHoldId);
 
  SeatHoldProfile removeSeatHold(Integer seatHoldId);
  
  void setSeatHoldTimeIntervalSecs(Integer seatHoldTimeIntervalSecs);  
  
  void addExpirationListener(ISeatHoldExpirationListener expirationListner);

  void setTimerIntervalInSecs(Integer geTimerIntervalInSecs);

  void setTimerInitDelayInSecs(Integer timerInitDelayInSecs);
  
}
