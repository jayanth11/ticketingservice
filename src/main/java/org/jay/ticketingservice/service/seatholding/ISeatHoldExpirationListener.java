package org.jay.ticketingservice.service.seatholding;

import java.util.List;

import org.jay.ticketingservice.event.profile.SeatHoldProfile;

public interface ISeatHoldExpirationListener {

  void onSeatHoldExpiration(List<SeatHoldProfile> seatHolds);
}
