package org.jay.ticketingservice.service.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.jay.ticketingservice.event.profile.SeatProfile;
import org.jay.ticketingservice.event.profile.SeatReservedProfile;
import org.jay.ticketingservice.event.profile.SeatStatus;
import org.jay.ticketingservice.venue.Row;

import com.google.gson.Gson;

public class Util {

  public static String createSeatId(Row row, Integer seatNumber) {
    return row.getId() + "-" + seatNumber;
  }
  
  public static String getRowId(String seatId) {
    int indexSeparator = seatId.indexOf("-");
    if(-1 == indexSeparator) {
      return null;
    } else {
      return seatId.substring(0,indexSeparator);
    }
  }  
  
  public static List<SeatProfile> createSeatProfiles(List<String> seatIds, SeatStatus seatStatus) {
    List<SeatProfile> seatProfiles = new ArrayList<>();
    for (String seatId : seatIds) {
      seatProfiles.add(new SeatProfile(seatId, seatStatus));
    }
    return seatProfiles;
  }

  public static boolean areSeatIdsInSameRow(List<String> seatIds) {
    List<String> sortedIds = new ArrayList<>(seatIds);
    Collections.sort(sortedIds);
    String prevRowId = null;
    boolean areInSameRow = true;
    for(String seatId: sortedIds) {
      String rowId = getRowId(seatId);
      if(null != prevRowId && !prevRowId.equals(rowId) ) {
        areInSameRow = false;
        break;
      }
      prevRowId = rowId;
    }
    return areInSameRow;
  }
  
  public static int[] numReservedSeatsInSameRow(List<SeatReservedProfile> seatReservedProfiles) {
    int numSameRowCount = 0;
    int numMultipleSeatCounts = 0;
    for(SeatReservedProfile seatReservedProfile: seatReservedProfiles) {
      if(seatReservedProfile.getSeatIds().size() > 1) {
        numMultipleSeatCounts++;
        if(areSeatIdsInSameRow(seatReservedProfile.getSeatIds())) {
          numSameRowCount++;
        }
      }
    }
    return new int[] { numMultipleSeatCounts, numSameRowCount };
  }
  
  public static boolean areSeatIdsContinous(List<String> seatIds) {
    List<String> sortedIds = new ArrayList<>(seatIds);
    Collections.sort(sortedIds);
    int prevSeatCode = -1;
    boolean isContinous = true;
    for(String seatId: sortedIds) {
      if(prevSeatCode != -1 && prevSeatCode != seatId.hashCode()-1) {
        isContinous = false;
        break;
      }
      prevSeatCode = seatId.hashCode();
    }
    return isContinous;
  }
  
  public static int[] numReservedSeatsContinous(List<SeatReservedProfile> seatReservedProfiles) {
    int numContinousCount = 0;
    int numMultipleSeatCounts = 0;
    for(SeatReservedProfile seatReservedProfile: seatReservedProfiles) {
      if(seatReservedProfile.getSeatIds().size() > 1) {
        numMultipleSeatCounts++;
        if(areSeatIdsContinous(seatReservedProfile.getSeatIds())) {
          numContinousCount++;
        }
      }
    }
    return new int[] { numMultipleSeatCounts, numContinousCount };
  }
 
  public static String writeToJson(Object object) {
    Gson gson = new Gson();
    String json = gson.toJson(object);
    return json;
  }
  
  public static InputStream getInputStreamFromClasspath(String filename) throws IOException {
    return Util.class.getClassLoader().getResourceAsStream(filename);
  }
  
  public static String getFileContentsFromClasspath(String filename) throws IOException {
    InputStream is = null;
    try {
      is = getInputStreamFromClasspath(filename);
      return IOUtils.toString(is);
    } finally {
      IOUtils.closeQuietly(is);
    }
  }
  
  public static Properties loadProperties(String filename) throws IOException {
    Properties properties = new Properties();
    InputStream is = null;
    try {
      is = getInputStreamFromClasspath(filename);
      properties.load(is);
    } finally {
      IOUtils.closeQuietly(is);
    }
    return properties;
  }
}
