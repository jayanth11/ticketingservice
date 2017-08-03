package org.jay.ticketingservice.venue;

import java.util.List;

/**
 * 
 * @author jayanthp
 *
 */
public class Venue {

  private String name;
    
  private List<Zone> zones;
  
  private List<Row> rows;  
  
  @Override
  public String toString() {
    return "Venue [name=" + name + ", zones=" + zones + ", rows=" + rows + "]";
  }

  public int numSeatsInVenue() {
    int seatCount = 0;
    for(Row row: rows) {
      seatCount += row.getNumOfSeats();
    }
    return seatCount;
  }  
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Zone> getZones() {
    return zones;
  }

  public void setZones(List<Zone> zones) {
    this.zones = zones;
  }

  public List<Row> getRows() {
    return rows;
  }

  public void setRows(List<Row> rows) {
    this.rows = rows;
  }

}
