# Hotel Project

A minimal pure-Java (no external build tool required) starter project named `Hotel`. It demonstrates a simple in-memory reservation system with rooms and reservations, including basic conflict detection and a lightweight custom test harness (no JUnit needed).

## Structure
```
Hotel/
  build.sh              # Compile sources & tests, then run test harness
  run.sh                # Build (if needed) and run the main app
  src/
    main/java/com/example/hotel/
      HotelApplication.java
      model/Room.java
      model/Reservation.java
      service/ReservationService.java
      service/ReservationConflictException.java
    test/java/com/example/hotel/service/
      ReservationServiceTest.java
  out/                  # (generated) compiled classes
```

## Requirements
* Java 18 (detected on your system) or later JDK.
* No Maven/Gradle required. (A sample `pom.xml` is provided if you later add Maven.)

## Quick Start
```bash
cd Hotel
./build.sh          # compile and run tests
./run.sh            # run the interactive demo
```

## Test Harness
`ReservationServiceTest` uses simple assertions. If a test fails it prints an error and exits with non‑zero status. Add more tests by extending that class or creating new ones with a `public static void main` method.

## Adding Maven (Optional)
If you install Maven later, you can run:
```bash
mvn test
mvn exec:java -Dexec.mainClass="com.example.hotel.HotelApplication"
```
(You may need to add the Exec Maven Plugin.)

## Next Ideas
* Persist data (JDBC, JPA, or simple JSON storage)
* REST API (Spring Boot, Micronaut, or SparkJava)
* Authentication & pricing rules
* Enhanced availability search

## License
TungPL


