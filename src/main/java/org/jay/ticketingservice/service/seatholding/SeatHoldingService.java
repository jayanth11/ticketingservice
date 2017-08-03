package org.jay.ticketingservice.service.seatholding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.jay.ticketingservice.event.profile.SeatHoldProfile;
import org.jay.ticketingservice.event.profile.SeatProfile;

/**
 * 
 * @author jayanthp
 *
 */
public class SeatHoldingService implements ISeatHoldingService {

  protected static final Logger LOGGER = Logger.getLogger(SeatHoldingService.class);
  
  protected Map<Integer, SeatHoldProfile> seatHoldMap;
  protected Map<Long, List<Integer>> seatHoldExpiryMap;
  
  protected Integer timerIntervalInSecs, timerInitDelayInSecs;
  protected ScheduledThreadPoolExecutor timer;
  
  protected AtomicLong runningClockTick;
  protected Integer seatHoldTimeIntervalSecs, seatHoldTimeIntervalInClockTicks;
  
  protected AtomicInteger seatHoldIdCounter;
    
  protected List<ISeatHoldExpirationListener> expirationListeners;
  
  public SeatHoldingService() {
    seatHoldMap = new ConcurrentHashMap<>();
    seatHoldExpiryMap = new ConcurrentHashMap<>();
    runningClockTick = new AtomicLong(0);
    seatHoldIdCounter = new AtomicInteger(0);
    expirationListeners = new ArrayList<>();
  }
  
  public void init() throws Exception {
    LOGGER.info("initialize  SeatHoldingService ");
    validate();
    if(timer != null) {
      timer.scheduleWithFixedDelay(new TimerTickHandler(), timerIntervalInSecs, timerInitDelayInSecs, TimeUnit.SECONDS);
    }    
  }

  public void shutdown() {
    timer.shutdownNow();
  }
  
  @Override
  public void start() throws Exception {
  }  
  
  private void validate() throws Exception {
    if(null == timerIntervalInSecs || null == timerInitDelayInSecs) {
      throw new Exception();
    } 
    if(null == timer) {
      timer = new ScheduledThreadPoolExecutor(1);
    }
    if(null == seatHoldTimeIntervalSecs) {
      throw new Exception();
    }
    calculateSeatHoldTimeIntervalInClockTicks();
  }
  
  @Override
  public SeatHoldProfile holdSeats(List<SeatProfile> seatIds) {
    SeatHoldProfile seatHold = new SeatHoldProfile( generateNextSeatHoldId(), seatIds );
    seatHold.setTimeToExpire(seatHoldTimeIntervalSecs);
    seatHoldMap.put(seatHold.getSeatHoldId(), seatHold);
    Long expiryClockTick = getExpiryCLockTick();
    if(!seatHoldExpiryMap.containsKey(expiryClockTick)) {
      seatHoldExpiryMap.put(expiryClockTick, new ArrayList<>());
    }
    seatHoldExpiryMap.get(expiryClockTick).add(seatHold.getSeatHoldId());
    return seatHold;
  }    
  
  @Override
  public SeatHoldProfile getSeatHold(Integer seatHoldId) {
    if(seatHoldMap.containsKey(seatHoldId)) {
      return seatHoldMap.get(seatHoldId);
    }
    return null;
  }
  
  @Override
  public SeatHoldProfile removeSeatHold(Integer seatHoldId) {
    if(seatHoldMap.containsKey(seatHoldId)) {
      return seatHoldMap.remove(seatHoldId);
    }
    return null;
  }
  
  @Override
  public void addExpirationListener(ISeatHoldExpirationListener expirationListner) {
    expirationListeners.add(expirationListner);
  }
  
  @Override
  public void setTimerIntervalInSecs(Integer timerIntervalInSecs) {
    this.timerIntervalInSecs = timerIntervalInSecs;
  }

  @Override
  public void setTimerInitDelayInSecs(Integer timerInitDelayInSecs) {
    this.timerInitDelayInSecs = timerInitDelayInSecs;
  }

  @Override
  public void setSeatHoldTimeIntervalSecs(Integer seatHoldTimeIntervalSecs) {
    this.seatHoldTimeIntervalSecs = seatHoldTimeIntervalSecs;
  }
  
  public class TimerTickHandler implements Runnable {
    public void run() {
      handleTimerEvent();
    }
  }
  
  public void handleTimerEvent() {
    runningClockTick.getAndIncrement();
    Long expiredClockTick = getExpiryCLockTickForEviction() ;
    evictExpiredClockTickEntries(expiredClockTick);
  }
  
  private void evictExpiredClockTickEntries(Long expiredClockTick) {
    List<Integer> expiredSeatHoldIds = seatHoldExpiryMap.remove(expiredClockTick);
    if(null == expiredSeatHoldIds) {
      return;
    }
    List<SeatHoldProfile> seatHolds = new ArrayList<>();   
    for(Integer expiredSeatHoldId : expiredSeatHoldIds) {
      if(seatHoldMap.containsKey(expiredSeatHoldId)) {
        SeatHoldProfile seatHold = seatHoldMap.remove(expiredSeatHoldId);
        seatHolds.add(seatHold);
      }
    }
    if(LOGGER.isTraceEnabled()) {
      LOGGER.trace("expiredClockTick = " + expiredClockTick + " evict seatHoldIds: " + seatHolds);  
    }
    for(ISeatHoldExpirationListener expirationListener : expirationListeners) {
      expirationListener.onSeatHoldExpiration(seatHolds);
    }
  }

  private void calculateSeatHoldTimeIntervalInClockTicks() {
    seatHoldTimeIntervalInClockTicks = seatHoldTimeIntervalSecs / timerIntervalInSecs;
  }
  
  private Integer generateNextSeatHoldId() {
    return seatHoldIdCounter.incrementAndGet();
  }
  
  private Long getExpiryCLockTick() {
    return runningClockTick.get() + 1 + seatHoldTimeIntervalInClockTicks;
  }
  
  private Long getExpiryCLockTickForEviction() {
    return runningClockTick.get();
  }
  
}
