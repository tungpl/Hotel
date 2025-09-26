package com.example.hotel.service;

import com.example.hotel.model.Reservation;
import com.example.hotel.model.Room;

import java.time.LocalDate;

/**
 * Lightweight test harness (no external libs). Runs a few assertions.
 */
public class ReservationServiceTest {

    public static void main(String[] args) {
        ReservationServiceTest test = new ReservationServiceTest();
        try {
            test.testCreateAndList();
            test.testConflict();
            test.testCapacityExceeded();
            System.out.println("[PASS] All ReservationService tests passed.");
        } catch (AssertionError e) {
            System.err.println("[FAIL] " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("[ERROR] Unexpected: " + e);
            e.printStackTrace();
            System.exit(2);
        }
    }

    void testCreateAndList() {
        ReservationService svc = new ReservationService();
        svc.addRoom(new Room("R1","101",2));
        Reservation r = svc.createReservation("RES1","R1","Alice", LocalDate.of(2025,1,10), LocalDate.of(2025,1,12),2);
        assertTrue(svc.listReservationsForRoom("R1").size() == 1, "expected 1 reservation");
        assertTrue(svc.isRoomAvailable("R1", LocalDate.of(2025,1,12), LocalDate.of(2025,1,15)), "room should be free after end date");
        assertEquals("RES1", r.getId(), "reservation id mismatch");
    }

    void testConflict() {
        ReservationService svc = new ReservationService();
        svc.addRoom(new Room("R1","101",2));
        svc.createReservation("RES1","R1","Bob", LocalDate.of(2025,2,1), LocalDate.of(2025,2,5),2);
        boolean threw = false;
        try {
            svc.createReservation("RES2","R1","Carol", LocalDate.of(2025,2,4), LocalDate.of(2025,2,6),1);
        } catch (ReservationConflictException ex) {
            threw = true;
        }
        assertTrue(threw, "expected conflict exception");
    }

    void testCapacityExceeded() {
        ReservationService svc = new ReservationService();
        svc.addRoom(new Room("R1","101",2));
        boolean threw = false;
        try {
            svc.createReservation("RES1","R1","Dan", LocalDate.of(2025,3,1), LocalDate.of(2025,3,2),3);
        } catch (IllegalArgumentException ex) {
            threw = true;
        }
        assertTrue(threw, "expected capacity exception");
    }

    private void assertTrue(boolean condition, String msg) {
        if (!condition) throw new AssertionError(msg);
    }

    private void assertEquals(Object expected, Object actual, String msg) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(msg + " (expected=" + expected + ", actual=" + actual + ")");
        }
    }
}

