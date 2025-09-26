package com.example.hotel.service;

import com.example.hotel.model.Reservation;
import com.example.hotel.model.Room;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread-safe in-memory reservation service.
 */
public class ReservationService {
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Reservation> reservations = new ConcurrentHashMap<>();
    // Index reservations by room for faster lookups
    private final Map<String, Set<String>> reservationsByRoom = new ConcurrentHashMap<>();

    public Room addRoom(Room room) {
        Objects.requireNonNull(room, "room");
        if (rooms.putIfAbsent(room.getId(), room) != null) {
            throw new IllegalArgumentException("Room with id already exists: " + room.getId());
        }
        reservationsByRoom.putIfAbsent(room.getId(), Collections.synchronizedSet(new LinkedHashSet<>()));
        return room;
    }

    public List<Room> listRooms() {
        return rooms.values().stream()
                .sorted(Comparator.comparing(Room::getNumber))
                .collect(Collectors.toUnmodifiableList());
    }

    public boolean isRoomAvailable(String roomId, LocalDate startDate, LocalDate endDate) {
        validateDates(startDate, endDate);
        Room room = rooms.get(roomId);
        if (room == null) throw new IllegalArgumentException("Unknown room: " + roomId);
        Set<String> ids = reservationsByRoom.getOrDefault(roomId, Set.of());
        for (String resId : ids) {
            Reservation r = reservations.get(resId);
            if (datesOverlap(startDate, endDate, r.getStartDate(), r.getEndDate())) {
                return false;
            }
        }
        return true;
    }

    public Reservation createReservation(String reservationId,
                                          String roomId,
                                          String guestName,
                                          LocalDate startDate,
                                          LocalDate endDate,
                                          int partySize) {
        validateDates(startDate, endDate);
        Room room = rooms.get(roomId);
        if (room == null) throw new IllegalArgumentException("Unknown room: " + roomId);
        if (partySize > room.getCapacity()) {
            throw new IllegalArgumentException("Party size " + partySize + " exceeds capacity " + room.getCapacity());
        }
        if (!isRoomAvailable(roomId, startDate, endDate)) {
            throw new ReservationConflictException("Room not available for given date range");
        }
        Reservation res = new Reservation(reservationId, roomId, guestName, startDate, endDate, partySize);
        if (reservations.putIfAbsent(res.getId(), res) != null) {
            throw new IllegalArgumentException("Reservation id already exists: " + res.getId());
        }
        reservationsByRoom.computeIfAbsent(roomId, key -> Collections.synchronizedSet(new LinkedHashSet<>())).add(res.getId());
        return res;
    }

    public List<Reservation> listReservationsForRoom(String roomId) {
        Set<String> ids = reservationsByRoom.getOrDefault(roomId, Collections.emptySet());
        return ids.stream()
                .map(reservations::get)
                .sorted(Comparator.comparing(Reservation::getStartDate))
                .collect(Collectors.toUnmodifiableList());
    }

    public Optional<Reservation> cancelReservation(String reservationId) {
        Reservation removed = reservations.remove(reservationId);
        if (removed != null) {
            Set<String> set = reservationsByRoom.getOrDefault(removed.getRoomId(), Collections.emptySet());
            set.remove(reservationId);
        }
        return Optional.ofNullable(removed);
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) throw new IllegalArgumentException("dates required");
        if (!startDate.isBefore(endDate)) throw new IllegalArgumentException("startDate must be before endDate");
    }

    private boolean datesOverlap(LocalDate aStart, LocalDate aEnd, LocalDate bStart, LocalDate bEnd) {
        // [aStart, aEnd) overlaps [bStart, bEnd) if aStart < bEnd && bStart < aEnd
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }
}

