package org.jay.ticketingservice.dto;

import java.util.List;

import org.jay.ticketingservice.api.SeatHold;

/**
 * 
 * @author jayanthp
 *
 */
public class SeatHoldApi implements SeatHold {
  
  private Integer seatHoldId;
  private List<String> seatIds;
  private String customerId;
  private Integer timeToExpire;
    
  public SeatHoldApi(Integer seatHoldId, List<String> seatIds, String customerId, Integer timeToExpire) {
    this.seatHoldId = seatHoldId;
    this.seatIds = seatIds;
    this.customerId = customerId;
    this.timeToExpire = timeToExpire;
  }  
  
  @Override
  public String toString() {
    return "SeatHoldApi [seatHoldId=" + seatHoldId + ", seatIds=" + seatIds + ", customerId="
        + customerId + ", timeToExpire=" + timeToExpire + "]";
  }



  @Override
  public List<String> getSeatIds() {
    return seatIds;
  }

  @Override
  public String getCustomerId() {
    return customerId;
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
}
