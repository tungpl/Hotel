package com.example.hotel.service;

import com.example.hotel.model.Guest;
import com.example.hotel.model.Payment;
import com.example.hotel.model.Reservation;
import com.example.hotel.model.Room;
import com.example.hotel.util.ConfigManager;
import com.example.hotel.util.JsonFileManager;
import com.example.hotel.util.ValidationUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Enhanced Hotel Management Service with full CRUD operations, JSON persistence,
 * validation, logging, and advanced features as per requirements.
 */
public class HotelManagementService {
    private static final Logger logger = Logger.getLogger(HotelManagementService.class.getName());
    
    // Thread-safe collections
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Guest> guests = new ConcurrentHashMap<>();
    private final Map<String, Reservation> reservations = new ConcurrentHashMap<>();
    private final Map<String, Payment> payments = new ConcurrentHashMap<>();
    
    // Index for faster lookups
    private final Map<String, Set<String>> reservationsByRoom = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> reservationsByGuest = new ConcurrentHashMap<>();
    
    // File paths
    private final String dataDir;
    private final String roomsFile;
    private final String guestsFile;
    private final String reservationsFile;
    private final String paymentsFile;
    
    public HotelManagementService() {
        this.dataDir = ConfigManager.getDataDirectory();
        this.roomsFile = dataDir + "/rooms.json";
        this.guestsFile = dataDir + "/guests.json";
        this.reservationsFile = dataDir + "/reservations.json";
        this.paymentsFile = dataDir + "/payments.json";
        
        loadAllData();
        logger.info("HotelManagementService initialized with data directory: " + dataDir);
    }
    
    // ==================== ROOM OPERATIONS ====================
    
    public Room addRoom(Room room) {
        Objects.requireNonNull(room, "Room cannot be null");
        
        if (rooms.containsKey(room.getId())) {
            throw new IllegalArgumentException("Room with ID " + room.getId() + " already exists");
        }
        
        rooms.put(room.getId(), room);
        reservationsByRoom.putIfAbsent(room.getId(), Collections.synchronizedSet(new LinkedHashSet<>()));
        
        saveRooms();
        logger.info("Added room: " + room);
        return room;
    }
    
    public Optional<Room> getRoomById(String roomId) {
        return Optional.ofNullable(rooms.get(roomId));
    }
    
    public List<Room> listRooms() {
        return rooms.values().stream()
                .sorted(Comparator.comparing(Room::getNumber))
                .collect(Collectors.toUnmodifiableList());
    }
    
    public List<Room> searchRoomsByCapacity(int minCapacity) {
        return rooms.values().stream()
                .filter(room -> room.getCapacity() >= minCapacity)
                .sorted(Comparator.comparing(Room::getCapacity))
                .collect(Collectors.toList());
    }
    
    public boolean removeRoom(String roomId) {
        // Check if room has active reservations
        Set<String> roomReservations = reservationsByRoom.getOrDefault(roomId, Collections.emptySet());
        boolean hasActiveReservations = roomReservations.stream()
                .map(reservations::get)
                .anyMatch(res -> res.getEndDate().isAfter(LocalDate.now()));
        
        if (hasActiveReservations) {
            throw new IllegalStateException("Cannot remove room with active reservations");
        }
        
        Room removed = rooms.remove(roomId);
        if (removed != null) {
            reservationsByRoom.remove(roomId);
            saveRooms();
            logger.info("Removed room: " + removed);
            return true;
        }
        return false;
    }
    
    // ==================== GUEST OPERATIONS ====================
    
    public Guest addGuest(Guest guest) {
        Objects.requireNonNull(guest, "Guest cannot be null");
        
        // Validate guest data
        ValidationUtils.ValidationResult validation = ValidationUtils.validateGuest(
            guest.getFirstName(), guest.getLastName(), guest.getEmail(), guest.getPhone()
        );
        
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid guest data: " + validation.getErrorMessage());
        }
        
        if (guests.containsKey(guest.getGuestId())) {
            throw new IllegalArgumentException("Guest with ID " + guest.getGuestId() + " already exists");
        }
        
        // Check for duplicate email
        boolean emailExists = guests.values().stream()
                .anyMatch(g -> g.getEmail().equalsIgnoreCase(guest.getEmail()));
        
        if (emailExists) {
            throw new IllegalArgumentException("Guest with email " + guest.getEmail() + " already exists");
        }
        
        guests.put(guest.getGuestId(), guest);
        reservationsByGuest.putIfAbsent(guest.getGuestId(), Collections.synchronizedSet(new LinkedHashSet<>()));
        
        saveGuests();
        logger.info("Added guest: " + guest);
        return guest;
    }
    
    public Optional<Guest> getGuestById(String guestId) {
        return Optional.ofNullable(guests.get(guestId));
    }
    
    public List<Guest> listGuests() {
        return guests.values().stream()
                .sorted(Comparator.comparing(Guest::getLastName)
                        .thenComparing(Guest::getFirstName))
                .collect(Collectors.toUnmodifiableList());
    }
    
    public List<Guest> searchGuestsByName(String name) {
        String searchTerm = name.toLowerCase().trim();
        return guests.values().stream()
                .filter(guest -> guest.getFullName().toLowerCase().contains(searchTerm))
                .sorted(Comparator.comparing(Guest::getLastName))
                .collect(Collectors.toList());
    }
    
    public List<Guest> getVipGuests() {
        return guests.values().stream()
                .filter(Guest::isVipStatus)
                .sorted(Comparator.comparing(Guest::getLastName))
                .collect(Collectors.toList());
    }
    
    public Guest updateGuest(Guest updatedGuest) {
        Objects.requireNonNull(updatedGuest, "Guest cannot be null");
        
        if (!guests.containsKey(updatedGuest.getGuestId())) {
            throw new IllegalArgumentException("Guest with ID " + updatedGuest.getGuestId() + " not found");
        }
        
        guests.put(updatedGuest.getGuestId(), updatedGuest);
        saveGuests();
        logger.info("Updated guest: " + updatedGuest);
        return updatedGuest;
    }
    
    // ==================== RESERVATION OPERATIONS ====================
    
    public Reservation createReservation(String reservationId, String roomId, String guestId,
                                         LocalDate startDate, LocalDate endDate, int partySize) {
        // Validate input
        if (!ValidationUtils.isValidId(reservationId)) {
            throw new IllegalArgumentException("Invalid reservation ID format");
        }
        if (!ValidationUtils.isValidDateRange(startDate, endDate)) {
            throw new IllegalArgumentException("Invalid date range");
        }
        if (!ValidationUtils.isValidPartySize(partySize)) {
            throw new IllegalArgumentException("Invalid party size");
        }
        
        // Check if room exists
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Room with ID " + roomId + " not found");
        }
        
        // Check if guest exists
        Guest guest = guests.get(guestId);
        if (guest == null) {
            throw new IllegalArgumentException("Guest with ID " + guestId + " not found");
        }
        
        // Check capacity
        if (partySize > room.getCapacity()) {
            throw new IllegalArgumentException("Party size " + partySize + " exceeds room capacity " + room.getCapacity());
        }
        
        // Check availability
        if (!isRoomAvailable(roomId, startDate, endDate)) {
            throw new ReservationConflictException("Room not available for given date range");
        }
        
        // Check for existing reservation ID
        if (reservations.containsKey(reservationId)) {
            throw new IllegalArgumentException("Reservation with ID " + reservationId + " already exists");
        }
        
        Reservation reservation = new Reservation(reservationId, roomId, guest.getFullName(), startDate, endDate, partySize);
        reservations.put(reservationId, reservation);
        
        // Update indexes
        reservationsByRoom.computeIfAbsent(roomId, k -> Collections.synchronizedSet(new LinkedHashSet<>()))
                .add(reservationId);
        reservationsByGuest.computeIfAbsent(guestId, k -> Collections.synchronizedSet(new LinkedHashSet<>()))
                .add(reservationId);
        
        saveReservations();
        logger.info("Created reservation: " + reservation);
        return reservation;
    }
    
    public boolean isRoomAvailable(String roomId, LocalDate startDate, LocalDate endDate) {
        if (!ValidationUtils.isValidDateRange(startDate, endDate)) {
            return false;
        }
        
        Set<String> roomReservationIds = reservationsByRoom.getOrDefault(roomId, Collections.emptySet());
        
        return roomReservationIds.stream()
                .map(reservations::get)
                .filter(Objects::nonNull)
                .noneMatch(reservation -> datesOverlap(startDate, endDate, 
                        reservation.getStartDate(), reservation.getEndDate()));
    }
    
    public List<Room> getAvailableRooms(LocalDate startDate, LocalDate endDate) {
        return rooms.values().stream()
                .filter(room -> isRoomAvailable(room.getId(), startDate, endDate))
                .sorted(Comparator.comparing(Room::getNumber))
                .collect(Collectors.toList());
    }
    
    public Optional<Reservation> getReservationById(String reservationId) {
        return Optional.ofNullable(reservations.get(reservationId));
    }
    
    public List<Reservation> listReservationsForRoom(String roomId) {
        Set<String> ids = reservationsByRoom.getOrDefault(roomId, Collections.emptySet());
        return ids.stream()
                .map(reservations::get)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Reservation::getStartDate))
                .collect(Collectors.toUnmodifiableList());
    }
    
    public List<Reservation> listReservationsForGuest(String guestId) {
        Set<String> ids = reservationsByGuest.getOrDefault(guestId, Collections.emptySet());
        return ids.stream()
                .map(reservations::get)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Reservation::getStartDate))
                .collect(Collectors.toUnmodifiableList());
    }
    
    public List<Reservation> listAllReservations() {
        return reservations.values().stream()
                .sorted(Comparator.comparing(Reservation::getStartDate))
                .collect(Collectors.toUnmodifiableList());
    }
    
    public Optional<Reservation> cancelReservation(String reservationId) {
        Reservation reservation = reservations.remove(reservationId);
        if (reservation != null) {
            // Update indexes
            Set<String> roomReservations = reservationsByRoom.get(reservation.getRoomId());
            if (roomReservations != null) {
                roomReservations.remove(reservationId);
            }
            
            // Find guest by name and update index
            guests.values().stream()
                    .filter(guest -> guest.getFullName().equals(reservation.getGuestName()))
                    .findFirst()
                    .ifPresent(guest -> {
                        Set<String> guestReservations = reservationsByGuest.get(guest.getGuestId());
                        if (guestReservations != null) {
                            guestReservations.remove(reservationId);
                        }
                    });
            
            saveReservations();
            logger.info("Cancelled reservation: " + reservation);
        }
        return Optional.ofNullable(reservation);
    }
    
    // ==================== PAYMENT OPERATIONS ====================
    
    public Payment addPayment(Payment payment) {
        Objects.requireNonNull(payment, "Payment cannot be null");
        
        if (payments.containsKey(payment.getPaymentId())) {
            throw new IllegalArgumentException("Payment with ID " + payment.getPaymentId() + " already exists");
        }
        
        // Validate reservation exists
        if (!reservations.containsKey(payment.getReservationId())) {
            throw new IllegalArgumentException("Reservation with ID " + payment.getReservationId() + " not found");
        }
        
        // Validate guest exists
        if (!guests.containsKey(payment.getGuestId())) {
            throw new IllegalArgumentException("Guest with ID " + payment.getGuestId() + " not found");
        }
        
        payments.put(payment.getPaymentId(), payment);
        savePayments();
        logger.info("Added payment: " + payment);
        return payment;
    }
    
    public List<Payment> listPaymentsForReservation(String reservationId) {
        return payments.values().stream()
                .filter(payment -> payment.getReservationId().equals(reservationId))
                .sorted(Comparator.comparing(Payment::getPaymentDate))
                .collect(Collectors.toList());
    }
    
    public List<Payment> listPaymentsForGuest(String guestId) {
        return payments.values().stream()
                .filter(payment -> payment.getGuestId().equals(guestId))
                .sorted(Comparator.comparing(Payment::getPaymentDate))
                .collect(Collectors.toList());
    }
    
    public BigDecimal getTotalPaymentsForReservation(String reservationId) {
        return payments.values().stream()
                .filter(payment -> payment.getReservationId().equals(reservationId))
                .filter(payment -> payment.getPaymentStatus() == Payment.PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    // ==================== UTILITY METHODS ====================
    
    private boolean datesOverlap(LocalDate aStart, LocalDate aEnd, LocalDate bStart, LocalDate bEnd) {
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }
    
    // ==================== DATA PERSISTENCE ====================
    
    private void loadAllData() {
        try {
            loadRooms();
            loadGuests();
            loadReservations();
            loadPayments();
            rebuildIndexes();
            logger.info("Successfully loaded all data from JSON files");
        } catch (Exception e) {
            logger.severe("Error loading data: " + e.getMessage());
        }
    }
    
    private void loadRooms() {
        try {
            List<Room> roomList = JsonFileManager.loadFromFile(roomsFile, Room.class);
            rooms.clear();
            roomList.forEach(room -> rooms.put(room.getId(), room));
            logger.info("Loaded " + roomList.size() + " rooms");
        } catch (IOException e) {
            logger.warning("Could not load rooms: " + e.getMessage());
        }
    }
    
    private void loadGuests() {
        try {
            List<Guest> guestList = JsonFileManager.loadFromFile(guestsFile, Guest.class);
            guests.clear();
            guestList.forEach(guest -> guests.put(guest.getGuestId(), guest));
            logger.info("Loaded " + guestList.size() + " guests");
        } catch (IOException e) {
            logger.warning("Could not load guests: " + e.getMessage());
        }
    }
    
    private void loadReservations() {
        try {
            List<Reservation> reservationList = JsonFileManager.loadFromFile(reservationsFile, Reservation.class);
            reservations.clear();
            reservationList.forEach(reservation -> reservations.put(reservation.getId(), reservation));
            logger.info("Loaded " + reservationList.size() + " reservations");
        } catch (IOException e) {
            logger.warning("Could not load reservations: " + e.getMessage());
        }
    }
    
    private void loadPayments() {
        try {
            List<Payment> paymentList = JsonFileManager.loadFromFile(paymentsFile, Payment.class);
            payments.clear();
            paymentList.forEach(payment -> payments.put(payment.getPaymentId(), payment));
            logger.info("Loaded " + paymentList.size() + " payments");
        } catch (IOException e) {
            logger.warning("Could not load payments: " + e.getMessage());
        }
    }
    
    private void rebuildIndexes() {
        reservationsByRoom.clear();
        reservationsByGuest.clear();
        
        // Rebuild room index
        for (Room room : rooms.values()) {
            reservationsByRoom.putIfAbsent(room.getId(), Collections.synchronizedSet(new LinkedHashSet<>()));
        }
        
        // Rebuild guest index
        for (Guest guest : guests.values()) {
            reservationsByGuest.putIfAbsent(guest.getGuestId(), Collections.synchronizedSet(new LinkedHashSet<>()));
        }
        
        // Add reservations to indexes
        for (Reservation reservation : reservations.values()) {
            reservationsByRoom.computeIfAbsent(reservation.getRoomId(), 
                k -> Collections.synchronizedSet(new LinkedHashSet<>())).add(reservation.getId());
            
            // Find guest by name and add to index
            guests.values().stream()
                    .filter(guest -> guest.getFullName().equals(reservation.getGuestName()))
                    .findFirst()
                    .ifPresent(guest -> reservationsByGuest.computeIfAbsent(guest.getGuestId(),
                            k -> Collections.synchronizedSet(new LinkedHashSet<>())).add(reservation.getId()));
        }
    }
    
    private void saveRooms() {
        try {
            JsonFileManager.saveToFile(new ArrayList<>(rooms.values()), roomsFile);
        } catch (IOException e) {
            logger.severe("Failed to save rooms: " + e.getMessage());
        }
    }
    
    private void saveGuests() {
        try {
            JsonFileManager.saveToFile(new ArrayList<>(guests.values()), guestsFile);
        } catch (IOException e) {
            logger.severe("Failed to save guests: " + e.getMessage());
        }
    }
    
    private void saveReservations() {
        try {
            JsonFileManager.saveToFile(new ArrayList<>(reservations.values()), reservationsFile);
        } catch (IOException e) {
            logger.severe("Failed to save reservations: " + e.getMessage());
        }
    }
    
    private void savePayments() {
        try {
            JsonFileManager.saveToFile(new ArrayList<>(payments.values()), paymentsFile);
        } catch (IOException e) {
            logger.severe("Failed to save payments: " + e.getMessage());
        }
    }
    
    // ==================== BACKUP AND RESTORE ====================
    
    public void createBackup() {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupDir = ConfigManager.getBackupDirectory() + "/" + timestamp;
            
            JsonFileManager.createBackup(roomsFile, backupDir + "/rooms.json");
            JsonFileManager.createBackup(guestsFile, backupDir + "/guests.json");
            JsonFileManager.createBackup(reservationsFile, backupDir + "/reservations.json");
            JsonFileManager.createBackup(paymentsFile, backupDir + "/payments.json");
            
            logger.info("Backup created successfully in " + backupDir);
        } catch (IOException e) {
            logger.severe("Failed to create backup: " + e.getMessage());
        }
    }
    
    // ==================== REPORTING AND STATISTICS ====================
    
    public Map<String, Object> generateOccupancyReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        
        List<Reservation> periodReservations = reservations.values().stream()
                .filter(res -> !res.getEndDate().isBefore(startDate) && !res.getStartDate().isAfter(endDate))
                .collect(Collectors.toList());
        
        report.put("totalRooms", rooms.size());
        report.put("totalReservations", periodReservations.size());
        report.put("totalGuests", guests.size());
        report.put("totalPayments", payments.size());
        report.put("reportPeriod", startDate + " to " + endDate);
        report.put("generatedAt", LocalDateTime.now());
        
        // Calculate occupan2cy rate
        long daysInPeriod = startDate.datesUntil(endDate.plusDays(1)).count();
        long totalRoomDays = rooms.size() * daysInPeriod;
        long occupiedRoomDays = periodReservations.stream()
                .mapToLong(res -> res.getStartDate().datesUntil(res.getEndDate()).count())
                .sum();
        
        double occupancyRate = totalRoomDays > 0 ? (double) occupiedRoomDays / totalRoomDays * 100 : 0;
        report.put("occupancyRate", String.format("%.2f%%", occupancyRate));
        
        return report;
    }
}