package org.jay.ticketingservice.service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jay.ticketingservice.api.SeatHold;
import org.jay.ticketingservice.event.profile.SeatHoldProfile;
import org.jay.ticketingservice.event.profile.SeatReservedProfile;
import org.jay.ticketingservice.event.profile.SeatStatus;
import org.jay.ticketingservice.service.ApplicationContext;
import org.jay.ticketingservice.service.TicketingService;
import org.jay.ticketingservice.service.util.MockDataBase;
import org.jay.ticketingservice.service.util.Util;
import org.jay.ticketingservice.venue.Venue;

import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;

/**
 * 
 * @author jayanthp
 *
 */
public class TestFramework {
  
  protected static final Logger LOGGER = Logger.getLogger(TestFramework.class);
  
  protected String testName;
  protected String currentRunTime;
  
  protected ApplicationContext applicationContext;
  protected TicketingService ticketService;
  protected Venue venue;
  protected Integer eventId;
  protected String customerIdPrefix = "cust-";

  protected Properties properties;
  protected int healthCheckIntervalMillis = 5000; // (rate: every 5 secs)
  protected int healthCheckDelayMillis = 300;
  protected int sleepBetweenRequestMillis = 200; // (req generated rate: 1 req/sec)
  protected int sendRequestExecutorThreadPool = 5; // (num of threads making requests)
  protected int maxSeatsPerRequest = 3;
  protected int numMillisAddedToSeatHoldTimeForWaitBeforeReserve = 2000; //(bound should be more than seat hold time for seat holding service)
  protected int waitTimeBeforeReserveBoundMillis;
  protected int requestRetryCountBeforeDrop = 1;
  protected int expectedTestCompletionTimeSecs = 180;
  
  protected Random random = new Random();
  protected ExecutorService createRequestsExecutor;
  protected ExecutorService sendRequestsExecutor;
  protected ScheduledExecutorService healthCheckExecutor;
  
  protected MockDataBase testResultDatabase, applicationDataBase;
  
  protected AtomicInteger totalRequestCounter = new AtomicInteger(0);
  protected AtomicInteger totalReservedRequestCounter = new AtomicInteger(0);
  protected AtomicInteger totalExpiredRequestCounter = new AtomicInteger(0);
  protected AtomicInteger totalDroppedRequestCounter = new AtomicInteger(0);
  
  protected Timer findSeatsTimer;
  
  protected List<SeatReservedProfile> expectedSeatReservedProfiles, actualSeatReservedProfiles;
  protected List<SeatHoldProfile> expectedExpiredSeatHoldProfiles, actualExpiredSeatHoldProfiles;
  
  protected CountDownLatch testStopLatch = new CountDownLatch(1);
  protected AtomicLong timeElapsedCounter = new AtomicLong();

  public TestFramework(String testFramewrokConfigFile) throws Exception {
    createTestName();
    testResultDatabase = new MockDataBase();
    properties = Util.loadProperties(testFramewrokConfigFile);
    setTestFrameworkProperties(properties);
  }

  private void setTestFrameworkProperties(Properties testFrameworkProperties) throws Exception {
    if(null == testFrameworkProperties) {
      return;
    }
    
    this.healthCheckIntervalMillis = Integer.parseInt( 
        testFrameworkProperties.getProperty("healthCheckIntervalMillis") );
    this.sleepBetweenRequestMillis = Integer.parseInt( 
        testFrameworkProperties.getProperty("sleepBetweenRequestMillis") );
    this.sendRequestExecutorThreadPool = Integer.parseInt( 
        testFrameworkProperties.getProperty("sendRequestExecutorThreadPool") );
    this.maxSeatsPerRequest = Integer.parseInt( 
        testFrameworkProperties.getProperty("maxSeatsPerRequest") );
    this.numMillisAddedToSeatHoldTimeForWaitBeforeReserve = Integer.parseInt( 
        testFrameworkProperties.getProperty("numMillisAddedToSeatHoldTimeForWaitBeforeReserve") );
    this.requestRetryCountBeforeDrop = Integer.parseInt( 
        testFrameworkProperties.getProperty("requestRetryCountBeforeDrop") );    
    this.expectedTestCompletionTimeSecs = Integer.parseInt( 
        testFrameworkProperties.getProperty("expectedTestCompletionTimeSecs") );        
  }
  
  private void createTestName() {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmSS");
    currentRunTime = dateFormat.format(System.currentTimeMillis());
    testName = "Test-TicketingService-" + currentRunTime;
  }
  
  public void init() throws Exception {
    LOGGER.info("Initializating Test: " + this.testName);
    ticketService.init();
    venue = applicationContext.getVenue();
    eventId = applicationContext.getCurrentEventIdForBooking();
    applicationDataBase = applicationContext.getDatabase();
    if(null == findSeatsTimer) {
      findSeatsTimer = new Timer();
    }
    waitTimeBeforeReserveBoundMillis = applicationContext.getSeatHoldTimeIntervalSecs() + 
        numMillisAddedToSeatHoldTimeForWaitBeforeReserve;
    createRequestsExecutor = Executors.newSingleThreadExecutor();
    sendRequestsExecutor = Executors.newFixedThreadPool(sendRequestExecutorThreadPool);
    healthCheckExecutor = Executors.newSingleThreadScheduledExecutor();
  }

  public void runTest() throws Exception {
    LOGGER.info("Call Run for Test: " + this.testName);
    
    HealthCheckTask healthCheckTask = new HealthCheckTask();
    healthCheckExecutor.scheduleAtFixedRate(healthCheckTask, healthCheckDelayMillis, 
        healthCheckIntervalMillis, TimeUnit.MILLISECONDS);
    
    createRequestsExecutor.submit(new CreateNewRequestTasks());
    testStopLatch.await();
    
    healthCheckExecutor.shutdownNow();
    createRequestsExecutor.shutdownNow();
    sendRequestsExecutor.shutdownNow();
    LOGGER.info("End Test: " + this.testName);
    LOGGER.info("Print Final Health metrics for Test " + this.testName);
    String testResultSummary = verifyTest();
    printReport(testResultSummary);
  }

  private String verifyTest() {
    String testResult = "Test Completed And Succesfull";
    
    expectedSeatReservedProfiles = testResultDatabase.getAllReservedSeatsProfiles(eventId);
    actualSeatReservedProfiles = applicationDataBase.getAllReservedSeatsProfiles(eventId);
    
    expectedExpiredSeatHoldProfiles = testResultDatabase.getAllExpiredSeatHolds(eventId);
    actualExpiredSeatHoldProfiles = applicationDataBase.getAllExpiredSeatHolds(eventId);
    
    if(timeElapsedCounter.get()/1000 > expectedTestCompletionTimeSecs) {
      testResult = "Test Not Completed";
      return testResult;
    }
    
    if(!expectedSeatReservedProfiles.equals(actualSeatReservedProfiles)) {
      testResult = "Test Completed But Failed. Mismatch between expectedSeatReservedProfiles & actualSeatReservedProfiles";
      return testResult;
    }
    if(!expectedExpiredSeatHoldProfiles.equals(actualExpiredSeatHoldProfiles)) {
      testResult = "Test Completed But Failed. Mismatch between expectedSeatHoldProfiles & actualSeatHoldProfiles";
      return testResult;
    }
    return testResult;
  }
  
  private void printReport(String testResult) throws Exception {
    StringBuilder stringBuilder = new StringBuilder(); 
    Snapshot snapShot = findSeatsTimer.getSnapshot();
    stringBuilder.append("\n-------------------------Test Summary--------------------------------------\n" );
    stringBuilder.append("testname: ").append(testName).append("\n");
    stringBuilder.append("testResult: ").append(testResult).append("\n");
    
    stringBuilder.append("\n-------------------------Test Parameters--------------------------------------\n" );
    stringBuilder.append("SeatAllocationStrategy: ").append(applicationContext.getSeatAllocationServiceStrategyClass()).append("\n");
    stringBuilder.append("seatHoldIntervalSecs: ").append(applicationContext.getSeatHoldTimeIntervalSecs()).append("\n");
    stringBuilder.append("sleepBetweenRequestMillis: ").append(sleepBetweenRequestMillis).append(" Millis").append("\n");
    stringBuilder.append("sendRequestExecutorThreadPool: ").append(sendRequestExecutorThreadPool).append("\n");
    stringBuilder.append("maxSeatsPerRequest: ").append(maxSeatsPerRequest).append("\n");
    
    stringBuilder.append("\n-------------------------Test results--------------------------------------\n" );
    stringBuilder.append("total test time: ").append(timeElapsedCounter.get() / 1000).append(" Secs").append("\n");
    stringBuilder.append("numSeatsVenue = ").append(venue.numSeatsInVenue()).append("\n");
    stringBuilder.append("numSeatsAvailable = ").append(ticketService.numSeatsAvailable()).append("\n");
    stringBuilder.append("numReservedSeats = ").append(ticketService.numSeatsReserved()).append("\n");

    stringBuilder.append("expected numReservedSeatsProfiles = ").append(testResultDatabase.numReservedSeatsProfiles(eventId)).append("\n");
    stringBuilder.append("actual numReservedSeatsProfiles = ").append(applicationDataBase.numReservedSeatsProfiles(eventId)).append("\n");
    
    int[] result = null;
    
    result = Util.numReservedSeatsInSameRow(actualSeatReservedProfiles);   
    stringBuilder.append("numMultipleSeatReservations = ").append(result[0]).append("\n");
    stringBuilder.append("numSameRowReservations = ").append(result[1]).append("\n");
    
    result = Util.numReservedSeatsContinous(actualSeatReservedProfiles);
    stringBuilder.append("numContinousSeatReservations = ").append(result[1]).append("\n");
    
    stringBuilder.append("expected numExpiredSeatHolds = ").append(testResultDatabase.numSeatHoldsExpired(eventId)).append("\n");
    stringBuilder.append("actual numExpiredSeatHolds = ").append(applicationDataBase.numSeatHoldsExpired(eventId)).append("\n");   
    stringBuilder.append("totalRequestCounter = ").append(totalRequestCounter.get()).append("\n");
    stringBuilder.append("totalReservedRequestCounter = ").append(totalReservedRequestCounter.get()).append("\n");    
    stringBuilder.append("totalExpiredRequestCounter = ").append(totalExpiredRequestCounter.get()).append("\n");
    stringBuilder.append("totalDroppedRequestCounter = ").append(totalDroppedRequestCounter.get()).append("\n");
    stringBuilder.append("findSeatsTimer mean = ").append(snapShot.getMean() / 1000).append(" millis").append("\n");
    stringBuilder.append("findSeatsTimer max = ").append(snapShot.getMax()  / 1000).append(" millis").append("\n");
    stringBuilder.append("findSeatsTimer 75 percentile = ").append(snapShot.get75thPercentile() / 1000).append(" millis").append("\n");
    stringBuilder.append("findSeatsTimer 95 percentile = ").append(snapShot.get95thPercentile() / 1000).append(" millis").append("\n");
    stringBuilder.append("----------------").append("\n");
    stringBuilder.append("actualSeatReservedProfiles: ").append(actualSeatReservedProfiles).append("\n");
    stringBuilder.append("----------------").append("\n");
    stringBuilder.append("expectedSeatReservedProfiles: ").append(expectedSeatReservedProfiles).append("\n");
    stringBuilder.append("----------------").append("\n");
    stringBuilder.append("actualExpiredSeatHoldProfiles: ").append(actualExpiredSeatHoldProfiles).append("\n");
    stringBuilder.append("----------------").append("\n");
    stringBuilder.append("expectedExpiredSeatHoldProfiles: ").append(expectedExpiredSeatHoldProfiles).append("\n");
    stringBuilder.append("----------------").append("\n");
    String data = stringBuilder.toString();
    LOGGER.info(data);
    FileUtils.writeStringToFile(new File("testreports/" + testName + ".txt"), data);
  }
  
  public class CreateNewRequestTasks implements Runnable {
    @Override
    public void run() {
      try {
        while (true) {
          String customerid = customerIdPrefix + totalRequestCounter.incrementAndGet();
          boolean reserveSeats = random.nextBoolean();
          SeatsRequestTask requestTask = new SeatsRequestTask(customerid, random.nextInt(maxSeatsPerRequest),
                  reserveSeats , random.nextInt(waitTimeBeforeReserveBoundMillis));
          sendRequestsExecutor.execute(requestTask);
          if(reserveSeats) {
            totalReservedRequestCounter.incrementAndGet();
          } else {
            totalExpiredRequestCounter.incrementAndGet();
          }
          Thread.sleep(sleepBetweenRequestMillis);
        }
      } catch (InterruptedException e) {
        LOGGER.info(" Thread" + Thread.currentThread().getName()
            + " - processing Interuppted");
      }
    }
  }

  public class SeatsRequestTask implements Runnable {
    private final String customerId;
    private final int numSeats;
    private final boolean reserveSeats;
    private final int waitTimeBeforeReserveBoundMillis;

    public SeatsRequestTask(String customerId, int numSeats, boolean reserveSeats, int waitTimeBeforeReserveBoundMillis) {
      this.customerId = customerId;
      this.numSeats = numSeats;
      this.reserveSeats = reserveSeats;
      this.waitTimeBeforeReserveBoundMillis = waitTimeBeforeReserveBoundMillis;
    }
    
    @Override
    public String toString() {
      return "SeatsRequestTask [customerId=" + customerId + ", numSeats=" + numSeats
          + ", reserveSeats=" + reserveSeats + ", waitTimeBeforeReserveBoundMillis=" + waitTimeBeforeReserveBoundMillis + "]";
    }

    public void run() {
      Context timerContext = null;
      String threadName = Thread.currentThread().getName();
      try {
        if(LOGGER.isTraceEnabled()) {
          LOGGER.info(" Thread: " +  threadName + " Processing request: " +  this);
        }
        int numSeatsAvailable = 0;
        for(int count=0; count<requestRetryCountBeforeDrop; count++) {
          numSeatsAvailable = ticketService.numSeatsAvailable();
          if(numSeats > numSeatsAvailable) {
            Thread.sleep(waitTimeBeforeReserveBoundMillis);
          } else {
            break;
          }
        }
        
        if(0 == numSeatsAvailable) {
          totalDroppedRequestCounter.incrementAndGet();
          return;
        }
        
        timerContext = findSeatsTimer.time();
        SeatHold seatHold = ticketService.findAndHoldSeats(numSeats, customerId);
        timerContext.stop();
        if (null == seatHold) {
          return;
        }
        if (reserveSeats) {
          Thread.sleep(waitTimeBeforeReserveBoundMillis);
          String confirmationId = ticketService.reserveSeats(seatHold.getSeatHoldId(), customerId);
          if(confirmationId != null) {
            testResultDatabase.putReservedSeatProfile(new SeatReservedProfile(confirmationId, seatHold
                .getSeatIds(), customerId));
          } else {
            SeatHoldProfile seatHoldProfile = new SeatHoldProfile(seatHold.getSeatHoldId(),
                Util.createSeatProfiles(seatHold.getSeatIds(), SeatStatus.ON_HOLD));
            seatHoldProfile.setCustomerId(customerId);
            testResultDatabase.putExpiredSeatHold(seatHoldProfile);
          }
        } else {
          SeatHoldProfile seatHoldProfile = new SeatHoldProfile(seatHold.getSeatHoldId(),
              Util.createSeatProfiles(seatHold.getSeatIds(), SeatStatus.ON_HOLD));
          seatHoldProfile.setCustomerId(customerId);
          testResultDatabase.putExpiredSeatHold(seatHoldProfile);
        }
      } catch (InterruptedException e) {
        LOGGER.info(" Thread" + Thread.currentThread().getName() + " - processing "
            + customerId + " Interuppted");
      }
    }
  }

  public class HealthCheckTask implements Runnable {

    @Override
    public void run() {
      
      LOGGER.info("--------------------------Health Check-------------------------------------" );
      
      LOGGER.info("numSeatsVenue = " + venue.numSeatsInVenue() );
      LOGGER.info("numSeatsAvailable = " + ticketService.numSeatsAvailable() );
      LOGGER.info("numReservedSeats = " + ticketService.numSeatsReserved() );
            
      LOGGER.info("totalRequestCounter = " + totalRequestCounter.get() );
      LOGGER.info("totalReservedRequestCounter = " + totalReservedRequestCounter.get() );
      LOGGER.info("totalExpiredRequestCounter = " + totalExpiredRequestCounter.get() );
      LOGGER.info("totalDroppedRequestCounter = " + totalDroppedRequestCounter.get() );
      
      LOGGER.info("--------------------------------------------------------------------------" );
      long timeElapsed = timeElapsedCounter.addAndGet(healthCheckIntervalMillis);
      boolean stopTest = venue.numSeatsInVenue() == ticketService.numSeatsReserved() ||
      (timeElapsed/1000 > expectedTestCompletionTimeSecs) ;
       
      if(stopTest) {
        testStopLatch.countDown();
      }
    }
  }
  
  public static void main(String[] args) {  
    try {
      LOGGER.info("command line args : " + Arrays.toString(args));
      
      TestFramework testFramework = new TestFramework("testframework.properties");

      Startup startUp = new Startup("application.properties");
      testFramework.applicationContext = startUp.getApplicationContext();
      testFramework.ticketService = startUp.getTicketingService();
            
      testFramework.init();
      testFramework.runTest();
      
    } catch(Exception e) {
      LOGGER.info("Error while running TestFramework: " + e);
    } finally {
      System.exit(0);
    }
  }
  
}
