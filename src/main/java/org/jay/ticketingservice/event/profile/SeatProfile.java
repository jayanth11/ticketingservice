package org.jay.ticketingservice.event.profile;

import org.jay.ticketingservice.service.util.Util;

/**
 * 
 * @author jayanthp
 *
 */
public class SeatProfile implements Comparable<SeatProfile> {
  
  private String seatId;
  private SeatStatus status;
    
  public SeatProfile(String seatId, SeatStatus status) {
    this.seatId = seatId;
    this.status = status;
  }
  
  @Override
  public String toString() {
    return "[seatId=" + seatId + ", status=" + status + "]";
  }  

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((seatId == null) ? 0 : seatId.hashCode());
    result = prime * result + ((status == null) ? 0 : status.hashCode());
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
    SeatProfile other = (SeatProfile) obj;
    if (seatId == null) {
      if (other.seatId != null)
        return false;
    } else if (!seatId.equals(other.seatId))
      return false;
    if (status != other.status)
      return false;
    return true;
  }

  @Override
  public int compareTo(SeatProfile o) {
    return seatId.compareTo(o.getSeatId());
  }  
  
  public String getRowId() {
    return Util.getRowId(seatId);
  }
  
  public void holdSeat() {
    this.status = SeatStatus.ON_HOLD;
  }
  
  public void reserveSeat() {
    this.status = SeatStatus.RESERVED;
  }
  
  public void unHoldSeat() {
    this.status = SeatStatus.AVAILABLE;
  }
    
  public String getSeatId() {
    return seatId;
  }
  
  public void setSeatId(String seatId) {
    this.seatId = seatId;
  }

  public SeatStatus getStatus() {
    return status;
  }
  
  public void setStatus(SeatStatus status) {
    this.status = status;
  }
  
}
