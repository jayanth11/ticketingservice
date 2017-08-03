package org.jay.ticketingservice.service;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jay.ticketingservice.service.bestseatvalue.IBestSeatValueService;
import org.jay.ticketingservice.service.seatallocation.ISeatAllocationService;
import org.jay.ticketingservice.service.seatconfirmation.ISeatConfirmationService;
import org.jay.ticketingservice.service.seatconfirmation.SeatConfirmationService;
import org.jay.ticketingservice.service.seatholding.ISeatHoldingService;
import org.jay.ticketingservice.service.seatholding.SeatHoldingService;
import org.jay.ticketingservice.service.util.MockDataBase;
import org.jay.ticketingservice.service.util.Util;
import org.jay.ticketingservice.venue.Venue;

import com.google.gson.Gson;

/**
 * 
 * @author jayanthp
 *
 */
public class ApplicationContext {
  
  protected static final Logger LOGGER = Logger.getLogger(TicketingService.class);

  private Properties properties;
  
  private Venue venue;
  private Integer currentEventIdForBooking;
  private ISeatAllocationService seatAllocationService;
  private ISeatHoldingService seatHoldingService;
  private ISeatConfirmationService seatConfirmationService;
  private IBestSeatValueService bestSeatValueService;
  private TicketingService ticketingService;
  private MockDataBase database;
  
  public ApplicationContext(String propertyFileFromClassPath) throws IOException {
    properties = Util.loadProperties(propertyFileFromClassPath);
    LOGGER.info("properties : " + properties);
  }
  
  public ApplicationContext(Properties properties) throws IOException {
    this.properties = properties;
    LOGGER.info("properties : " + properties);
  }  
  
  public TicketingService loadApplication() throws Exception {
    LOGGER.info("loadApplication : " + properties);
    database = new MockDataBase();
    venue = loadVenue();
    LOGGER.info("venue : " + venue);
    currentEventIdForBooking = getCurrentEventIdForBooking();
    bestSeatValueService = loadBestSeatValueService();
    seatHoldingService = loadSeatHoldingService();
    seatConfirmationService = loadSeatConfirmationService();
    seatAllocationService = loadSeatAllocationService();
    ticketingService = loadTicketingService();
    return ticketingService;
  }
  
  public Venue loadVenue() throws Exception {
    String venueConfigFile = getVenueJSONConfigFile();
    String venueJsonStr = Util.getFileContentsFromClasspath(venueConfigFile);
    LOGGER.info("venueJsonStr : " + venueJsonStr);
    Gson gson = new Gson();
    return gson.fromJson(venueJsonStr, Venue.class);
  }
  
  public ISeatHoldingService loadSeatHoldingService() throws Exception {
    ISeatHoldingService seatHoldingService = new SeatHoldingService();
    seatHoldingService.setSeatHoldTimeIntervalSecs(getSeatHoldTimeIntervalSecs());
    seatHoldingService.setTimerIntervalInSecs(geTimerIntervalInSecs());
    seatHoldingService.setTimerInitDelayInSecs(getTimerInitDelayInSecs());
    return seatHoldingService;
  }
  
  public IBestSeatValueService loadBestSeatValueService() throws Exception {
    String bestSeatValueServiceStrategyClass = getBestSeatValueServiceStrategyClass();
    Class<?> bestSeatValueServiceStrategyClazz = ClassLoader.getSystemClassLoader().loadClass(bestSeatValueServiceStrategyClass);
    return (IBestSeatValueService) bestSeatValueServiceStrategyClazz.newInstance();
  }    
  
  public ISeatAllocationService loadSeatAllocationService() throws Exception {
    String seatAllocationServiceStrategyClass = getSeatAllocationServiceStrategyClass();
    Class<?> seatAllocationServiceStrategyClazz = ClassLoader.getSystemClassLoader().loadClass(seatAllocationServiceStrategyClass);
    ISeatAllocationService seatAllocationService = (ISeatAllocationService) seatAllocationServiceStrategyClazz.newInstance();
    seatAllocationService.setBestSeatValueService(bestSeatValueService);
    return seatAllocationService;
  }  
  
  public ISeatConfirmationService loadSeatConfirmationService() throws Exception {
    return new SeatConfirmationService();
  }
  
  public TicketingService loadTicketingService() throws Exception {
    TicketingService ticketingService = new TicketingService();
    ticketingService.setSeatAllocationService(seatAllocationService);
    ticketingService.setSeatConfirmationService(seatConfirmationService);
    ticketingService.setSeatHoldingService(seatHoldingService);
    ticketingService.setCurrentEventIdForBooking(currentEventIdForBooking);
    ticketingService.setVenue(venue);
    ticketingService.setDatabase(database);
    seatHoldingService.addExpirationListener(ticketingService);
    return ticketingService;
  }
  
  public Properties getProperties() {
    return properties;
  }

  public String getVenueJSONConfigFile() {
    return properties.getProperty("venueConfigFile");
  }
  
  public String getSeatAllocationServiceStrategyClass() {
    return properties.getProperty("seatAllocationServiceStrategyClass");
  }
  
  public String getSeatHoldingServiceStrategyClass() {
    return properties.getProperty("seatHoldingServiceStrategyClass");
  }  

  public String getBestSeatValueServiceStrategyClass() {
    return properties.getProperty("bestSeatValueServiceStrategyClass");
  }    
  
  public Integer getCurrentEventIdForBooking() throws Exception {
    return Integer.parseInt( properties.getProperty("currentEventIdForBooking") );
  }
  
  public Integer getSeatHoldTimeIntervalSecs() throws Exception {
    return Integer.parseInt( properties.getProperty("seatHoldTimeIntervalSecs") );
  }
  
  public Integer geTimerIntervalInSecs() throws Exception {
    return Integer.parseInt( properties.getProperty("timerIntervalInSecs") );
  }
 
  public Integer getTimerInitDelayInSecs() throws Exception {
    return Integer.parseInt( properties.getProperty("timerInitDelayInSecs") );
  }

  public Venue getVenue() {
    return venue;
  }

  public MockDataBase getDatabase() {
    return database;
  }
  
}
