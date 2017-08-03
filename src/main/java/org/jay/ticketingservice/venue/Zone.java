package org.jay.ticketingservice.venue;

import java.math.BigDecimal;

/**
 * 
 * @author jayanthp
 *
 */
public class Zone {

  public enum DIRECTION {
    STAGE_FRONT, STAGE_SIDE, STAGE_REAR
  }

  private String id;
  
  private BigDecimal price;
  
  private DIRECTION direction;
  
  @Override
  public String toString() {
    return "Zone [id=" + id + ", price=" + price + ", direction=" + direction + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
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
    Zone other = (Zone) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public DIRECTION getDirection() {
    return direction;
  }

  public void setDirection(DIRECTION direction) {
    this.direction = direction;
  }
  
}
