package org.jay.ticketingservice.service;

public interface IProcessState {

  void init() throws Exception;
  
  void start() throws Exception;
  
  void shutdown() throws Exception;
  
}
