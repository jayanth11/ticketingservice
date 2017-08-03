
Instructions For compiling & testing:
1) To compile and run junit test cases: 
   mvn clean install

2) To run the Test Framework run any of the following commands:
   mvn exec:exec -Dexec.classpathScope="test"
   mvn clean install exec:exec -Dexec.classpathScope="test"
   The test framework generates reports in testreports/ folder. They are also printed to console.
   
3) To change application properties before testing
   edit file in classpath: src/main/resources/application.properties
   you have to edit this file to change the following:
   a) seat allocation strategy
   b) best seat value strategy
   c) seat hold time interval in seconds

4) To change venue related properties before testing 
    edit venue json config file: src/main/resources/venue-config.json
   
5) To change test framework properties before testing
   edit file in classpath: src/test/resources/testframework.properties     
   you have to edit this file to change the following:
   a) health Check Interval in Millis
   b) sleep between requets in millis
   c) no of concurrent threads making requests etc  
   
   
TicketingService Architecture & Design:

Assumptions:
1) Venue contains rows of seats. each rows can be grouped to different but mandatory zones
2) Seatid = tuple<Rowid,seatnumber>. Smaller row ids are closer to stage and are best seats

Testing:
1) Junit tests are provided for each service component
2) A Test framework is provided for more elaborate testing

Limitations of service submitted:
1) A single instance of service can handle only reservations for only one event hardcoded in application.properties
2) Service does not persist reservations for event anywhere. It load previous reservations for an event id

Advantages of service:
1) Service allows new strategies for seat allocation to be easily plugged in 
2) Service allows new strategies to assign 'best value' to seats to be easily plugged in
3) Service allows new strategies for seat holding and expiration to be easily plugged in

Advantages of Test framework:
1) provides a turn key solution to test concurrent requests to Ticketing service
2) provides throughput related performance metrics for comparison between seat allocation services
3) generates seat allocation related performance metrics for comparison between seat allocation services
4) provides a collated summary report of all parameters and metrics

TicketingService can be thought as a wrapper over many smaller and necessary services such as:

1) BestSeatValueService: A service which calculates a best value for each seat based on any plugable strategy 
   configured in application.properties
2) SeatHoldingService: A service which handles the requirement to hold seats, track them and expire them if they are not reserved
   after a configurable time interval in application.properties.
3) SeatConfirmationService: A service which handles the requirement to hold reserved seats and generate confirmation ids
4) SeatAllocationService: This is the brain of the application. This allocates the seats requested. The strategy used
   for this can be plugged in using the application.properties.
5) Database Service: This is a service which mocks a database by using caches

IN addition the service contains following helper POJOs:

1) Json Entities: This contains the java entities (Venue, Zone, Row) deserialized from the venue-config.json
2) service profiles. These describe a profile of Seats and its status, Seats Reserved, or Seats Held for a particular event
3) Api: The contain the POJOs that have to interfaced with external client
4) Util: These contain utility classes


How to describe a Venue?
A json schema is used to describe a venue as mentioned in src/main/resources/venue-config-schema.json
The actual venue data is specified in json file in src/main/resources/venue-config.json

Why Json ? Because it was the quickest way to get development running

Venue schema contains two sections:
1) One Section that contains metadata about 'Zones'
2) The other one contains metadata about rows. Each row contains a mandatory zone id


IBestSeatValueService -> How to assign best Seat value ?
I used a simplest strategy: to assign best value to seats which are closest to the stage.

However we can assign best value based on many properties of the row or zone.
i.e. some other strategies are: 
  1) Assign best values based on Zones instead of rows,
  2) Assign best values for seats based on a weightage of distance, direction facing stage, height from ground etc
Any strategy implemented can be easily plugged in if it adheres to the contract mentioned in IBestSeatValueService


ISeatAllocationService -> How to allocate seats?
I used a simple strategy of a thread safe priortiy queue (based on the best value of rows closest to stage) to group the seats.
I then allot the seats based on first best seats available from the queue.
Advantages:
   1) simple and all the 3 operations: findAndHold, get numSeatsAvailable and reserveSeats are very fast.
Disadvantages:
   1) Preference is not given to finding first continuous Seats, even if they are available.

i.e. some other strategies are: 
   1) FirstBestSeatsInRowPreferabblycontinuous -> complex 
       look for best seats in a row, preferably continuous. If notthe allocate discontinuous seats in the same row as they are best seats
   2) FirstcontinuousSeatsAvailableInVenuePreferabblyBest -> too complex and can be slow
       look for continuous seats in entire venue, preferably best value seats. Here emphasis is for continuous. 
Any strategy implemented can be easily plugged in if it adheres to the contract mentioned in ISeatAllocationService

ISeatHoldingService -> How to keep track of seats held and expire them ?
I use two maps 
1) one map is to easily access a profile of seats held using its id which sent to the client
2) second map is to group all seat hold ids according to the the timestamp (in terms of timer ticks) when they are supposed to expire
   So when a timer tick event occurs, a running counter is incremented and the service easily accesses all the 
   seat hold ids using the counter and expire them. 
   When a new profile of seats held is added to the service, it calculates in advance the expected expiry timestamp
   using the running counter and groups the profile according to that expirty timetampmp.
   

How to use Test Framework?
The test framework contains 3 ExecutorService thread pools:

1) Thread pool to make the actual requests to the ticketing service
2) Single Threaded service to create request objects at a particular rate
3) Single Scheduled Threaded service to act as a watchdog and periodically write health metrics to console.
   it also determines when the test is to stop.
   
   
Instructions to run the tests are mentioned at the beginning.

The test framework writes a test summary report to file and console.
The report generates important info to compare performance for different seat allocation strategy such as:
1) how may of the seat reservations are mulitple seat reservations and how many are in same row and continuous
2) metrics like mean , 95% and 75% percentile time for the findAndHold api call.  

Here is a sample rest result report:
   
-------------------------Test Summary--------------------------------------
testname: Test-TicketingService-20170803-1319700
testResult: Test Completed And Succesfull

-------------------------Test Parameters--------------------------------------
SeatAllocationStrategy: org.jay.ticketingservice.service.seatallocation.FirstBestSeatAvailableStrategy
seatHoldIntervalSecs: 4
sleepBetweenRequestMillis: 50 Millis
sendRequestExecutorThreadPool: 10
maxSeatsPerRequest: 5

-------------------------Test results--------------------------------------
total test time: 25 Secs
numSeatsVenue = 12
numSeatsAvailable = 0
numReservedSeats = 12
expected numReservedSeatsProfiles = 4
actual numReservedSeatsProfiles = 4
numMultipleSeatReservations = 4
numSameRowReservations = 2
numContinousSeatReservations = 2
expected numExpiredSeatHolds = 6
actual numExpiredSeatHolds = 6
totalRequestCounter = 384
totalReservedRequestCounter = 191
totalExpiredRequestCounter = 193
totalDroppedRequestCounter = 222
findSeatsTimer mean = 102.64485714285715 millis
findSeatsTimer max = 1419 millis
findSeatsTimer 75 percentile = 76.6365 millis
findSeatsTimer 95 percentile = 1288.118399999998 millis
----------------
actualSeatReservedProfiles: [SeatReservedProfile [confirmationId=1, seatIds=[AA-3, AA-4, AA-5, AB-0], customerId=cust-3], SeatReservedProfile [confirmationId=2, seatIds=[AB-1, AB-2, AB-3, AB-4], customerId=cust-4], SeatReservedProfile [confirmationId=3, seatIds=[AA-2, AB-5], customerId=cust-193], SeatReservedProfile [confirmationId=4, seatIds=[AA-0, AA-1], customerId=cust-191]]
----------------
expectedSeatReservedProfiles: [SeatReservedProfile [confirmationId=1, seatIds=[AA-3, AA-4, AA-5, AB-0], customerId=cust-3], SeatReservedProfile [confirmationId=2, seatIds=[AB-1, AB-2, AB-3, AB-4], customerId=cust-4], SeatReservedProfile [confirmationId=3, seatIds=[AA-2, AB-5], customerId=cust-193], SeatReservedProfile [confirmationId=4, seatIds=[AA-0, AA-1], customerId=cust-191]]
----------------
actualExpiredSeatHoldProfiles: [SeatHoldProfile [seatHoldId=1, customerId=cust-2, seatIds=[AA-0, AA-1, AA-2]], SeatHoldProfile [seatHoldId=4, customerId=cust-7, seatIds=[AB-5]], SeatHoldProfile [seatHoldId=5, customerId=cust-61, seatIds=[AA-0, AA-1, AA-2]], SeatHoldProfile [seatHoldId=6, customerId=cust-69, seatIds=[AB-5]], SeatHoldProfile [seatHoldId=7, customerId=cust-130, seatIds=[AA-0]], SeatHoldProfile [seatHoldId=8, customerId=cust-131, seatIds=[AA-1, AA-2, AB-5]]]
----------------
expectedExpiredSeatHoldProfiles: [SeatHoldProfile [seatHoldId=1, customerId=cust-2, seatIds=[AA-0, AA-1, AA-2]], SeatHoldProfile [seatHoldId=4, customerId=cust-7, seatIds=[AB-5]], SeatHoldProfile [seatHoldId=5, customerId=cust-61, seatIds=[AA-0, AA-1, AA-2]], SeatHoldProfile [seatHoldId=6, customerId=cust-69, seatIds=[AB-5]], SeatHoldProfile [seatHoldId=7, customerId=cust-130, seatIds=[AA-0]], SeatHoldProfile [seatHoldId=8, customerId=cust-131, seatIds=[AA-1, AA-2, AB-5]]]
----------------

