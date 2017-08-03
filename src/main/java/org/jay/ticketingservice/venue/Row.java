package org.jay.ticketingservice.venue;

/**
 * 
 * @author jayanthp
 *
 */
public class Row {

  private String id;
 
  private int numOfSeats;
  
  private int distanceFromStage;
 
  private String zoneId;
  
  @Override
  public String toString() {
    return "Row [id=" + id + ", numOfSeats=" + numOfSeats + ", distanceFromStage="
        + distanceFromStage + ", zoneId=" + zoneId + "]";
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
    Row other = (Row) obj;
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
  
  public int getNumOfSeats() {
    return numOfSeats;
  }

  public void setNumOfSeats(int numOfSeats) {
    this.numOfSeats = numOfSeats;
  }

  public int getDistanceFromStage() {
    return distanceFromStage;
  }

  public void setDistanceFromStage(int distanceFromStage) {
    this.distanceFromStage = distanceFromStage;
  }

  public String getZoneId() {
    return zoneId;
  }
  
  public void setZoneId(String zoneId) {
    this.zoneId = zoneId;
  }
  
  
  
}
