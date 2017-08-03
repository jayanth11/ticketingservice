package org.jay.ticketingservice.service;

import java.io.IOException;

import org.jay.ticketingservice.api.TicketService;


/**
 * 
 * @author jayanthp
 *
 */
public class Startup {
  
  
  
  private TicketingService ticketingService;
  private ApplicationContext applicationContext;

  public Startup(String propertyFile) throws Exception {
    applicationContext = new ApplicationContext(propertyFile);
    ticketingService = applicationContext.loadApplication();
  }
  
  public static void main(String[] args) {   
    try {
      
      Startup startUp = new Startup("application.properties");   
      startUp.getTicketingService().init();
      startUp.getTicketingService().start();
      
    } catch (Exception e) {
      System.err.println("Failure to start:" + e.getMessage());
      e.printStackTrace();
      System.exit(-1);
    }
  }

  public TicketingService getTicketingService() {
    return ticketingService;
  }

  public ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  
}
