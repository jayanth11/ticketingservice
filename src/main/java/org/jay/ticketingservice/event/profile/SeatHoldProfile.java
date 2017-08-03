package org.jay.ticketingservice.event.profile;

import java.util.ArrayList;
import java.util.List;

import org.jay.ticketingservice.api.SeatHold;

/**
 * 
 * @author jayanthp
 *
 */
public class SeatHoldProfile implements SeatHold {

  private Integer seatHoldId;
  private String customerId;
  private Integer timeToExpire;
  
  private List<String> seatIds;
  private List<SeatProfile> seatProfiles;  
  
  public SeatHoldProfile(Integer seatHoldId, List<SeatProfile> seatProfiles) {
    this.seatHoldId = seatHoldId;
    this.seatProfiles = seatProfiles;
    seatIds = new ArrayList<>();
    for(SeatProfile seatProfile : seatProfiles) {
      seatIds.add( seatProfile.getSeatId() );
    }
  }  
  
  @Override
  public String toString() {
    return "SeatHoldProfile [seatHoldId=" + seatHoldId + ", customerId=" + customerId
        + ", seatIds=" + seatIds + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((customerId == null) ? 0 : customerId.hashCode());
    result = prime * result + ((seatHoldId == null) ? 0 : seatHoldId.hashCode());
    result = prime * result + ((seatIds == null) ? 0 : seatIds.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SeatHoldProfile other = (SeatHoldProfile) obj;
    if (customerId == null) {
      if (other.customerId != null)
        return false;
    } else if (!customerId.equals(other.customerId))
      return false;
    if (seatHoldId == null) {
      if (other.seatHoldId != null)
        return false;
    } else if (!seatHoldId.equals(other.seatHoldId))
      return false;
    if (seatIds == null) {
      if (other.seatIds != null)
        return false;
    } else if (!seatIds.equals(other.seatIds))
      return false;
    return true;
  }

  @Override
  public List<String> getSeatIds() {
    return seatIds;
  }

  @Override
  public String getCustomerId() {
    return customerId;
  }

  public void setCustomerId(String customerId) {
    this.customerId = customerId;
  }

  @Override
  public Integer getSeatHoldId() {
    return seatHoldId;
  }

  public Integer getTimeToExpire() {
    return timeToExpire;
  }

  public void setTimeToExpire(Integer timeToExpire) {
    this.timeToExpire = timeToExpire;
  }

  public List<SeatProfile> getSeatProfiles() {
    return seatProfiles;
  }  
  
}
