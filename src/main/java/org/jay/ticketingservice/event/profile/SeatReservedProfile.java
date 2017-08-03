package org.jay.ticketingservice.event.profile;

import java.util.List;

public class SeatReservedProfile {

  private String confirmationId;
  private List<String> seatIds;
  private String customerId;
  
  public SeatReservedProfile(String confirmationId, List<String> seatIds, String customerId ) {
    this.confirmationId = confirmationId;
    this.seatIds = seatIds;
    this.customerId = customerId;
  }
  
  @Override
  public String toString() {
    return "SeatReservedProfile [confirmationId=" + confirmationId + ", seatIds=" + seatIds
        + ", customerId=" + customerId + "]";
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((confirmationId == null) ? 0 : confirmationId.hashCode());
    result = prime * result + ((customerId == null) ? 0 : customerId.hashCode());
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
    SeatReservedProfile other = (SeatReservedProfile) obj;
    if (confirmationId == null) {
      if (other.confirmationId != null)
        return false;
    } else if (!confirmationId.equals(other.confirmationId))
      return false;
    if (customerId == null) {
      if (other.customerId != null)
        return false;
    } else if (!customerId.equals(other.customerId))
      return false;
    if (seatIds == null) {
      if (other.seatIds != null)
        return false;
    } else if (!seatIds.equals(other.seatIds))
      return false;
    return true;
  }

  public String getConfirmationId() {
    return confirmationId;
  }
  public void setConfirmationId(String confirmationId) {
    this.confirmationId = confirmationId;
  }
  public List<String> getSeatIds() {
    return seatIds;
  }
  public void setSeatIds(List<String> seatIds) {
    this.seatIds = seatIds;
  }
  public String getCustomerId() {
    return customerId;
  }
  public void setCustomerId(String customerId) {
    this.customerId = customerId;
  }
  
  
  
  
}
