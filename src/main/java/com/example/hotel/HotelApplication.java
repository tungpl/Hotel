package com.example.hotel;

import com.example.hotel.model.Guest;
import com.example.hotel.model.Payment;
import com.example.hotel.model.Room;
import com.example.hotel.service.HotelManagementService;
import com.example.hotel.util.ValidationUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.Logger;

public class HotelApplication {
    private static final HotelManagementService service = new HotelManagementService();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("🏨 Hotel Booking Management System 🏨");
        System.out.println("=======================================");
        
        // Initialize with sample data if no data exists
        initializeSampleData();
        
        boolean running = true;
        while (running) {
            displayMenu();
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    handleRoomManagement();
                    break;
                case "2":
                    handleGuestManagement();
                    break;
                case "3":
                    handleReservationManagement();
                    break;
                case "4":
                    handlePaymentManagement();
                    break;
                case "5":
                    handleReportsAndSearch();
                    break;
                case "0":
                    System.out.println("Thank you for using Hotel Management System!");
                    running = false;
                    break;
                default:
                    System.out.println("❌ Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }
    
    private static void displayMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("📋 MAIN MENU");
        System.out.println("=".repeat(50));
        System.out.println("1. 🏠 Room Management");
        System.out.println("2. 👥 Guest Management");
        System.out.println("3. 📅 Reservation Management");
        System.out.println("4. 💰 Payment Management");
        System.out.println("5. 📊 Reports & Search");
        System.out.println("0. 🚪 Exit");
        System.out.println("=".repeat(50));
        System.out.print("Enter your choice: ");
    }
    
    private static void initializeSampleData() {
        // Add sample rooms if none exist
        if (service.listRooms().isEmpty()) {
            service.addRoom(new Room("R1", "101", 2));
            service.addRoom(new Room("R2", "102", 4));
            service.addRoom(new Room("R3", "201", 1));
            service.addRoom(new Room("R4", "202", 3));
            System.out.println("✅ Initialized with sample rooms");
        }
        
        // Add sample guests if none exist
        if (service.listGuests().isEmpty()) {
            Guest guest1 = new Guest("G1", "John", "Doe", "john.doe@email.com", "5551234567");
            Guest guest2 = new Guest("G2", "Jane", "Smith", "jane.smith@email.com", "5555678901");
            guest2.setVipStatus(true);
            
            service.addGuest(guest1);
            service.addGuest(guest2);
            System.out.println("✅ Initialized with sample guests");
        }
    }
    
    // ==================== ROOM MANAGEMENT ====================
    
    private static void handleRoomManagement() {
        System.out.println("\n🏠 ROOM MANAGEMENT");
        System.out.println("1. Add Room");
        System.out.println("2. List All Rooms");
        System.out.println("3. Search Rooms by Capacity");
        System.out.println("4. Remove Room");
        System.out.println("0. Back to Main Menu");
        System.out.print("Choice: ");
        
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                addRoom();
                break;
            case "2":
                listRooms();
                break;
            case "3":
                searchRoomsByCapacity();
                break;
            case "4":
                removeRoom();
                break;
            case "0":
                System.out.println("Returning to main menu...");
                break;
            default:
                System.out.println("❌ Invalid choice");
        }
    }
    
    private static void addRoom() {
        try {
            System.out.print("Room ID: ");
            String id = ValidationUtils.sanitizeInput(scanner.nextLine());
            
            System.out.print("Room Number: ");
            String number = ValidationUtils.sanitizeInput(scanner.nextLine());
            
            System.out.print("Capacity: ");
            int capacity = Integer.parseInt(scanner.nextLine().trim());
            
            if (!ValidationUtils.isValidId(id)) {
                System.out.println("❌ Invalid room ID format");
                return;
            }
            
            if (!ValidationUtils.isValidRoomNumber(number)) {
                System.out.println("❌ Invalid room number format");
                return;
            }
            
            if (!ValidationUtils.isValidCapacity(capacity)) {
                System.out.println("❌ Invalid capacity (must be 1-10)");
                return;
            }
            
            Room room = service.addRoom(new Room(id, number, capacity));
            System.out.println("✅ Room added successfully: " + room);
            
        } catch (Exception e) {
            System.out.println("❌ Error adding room: " + e.getMessage());
        }
    }
    
    private static void listRooms() {
        List<Room> rooms = service.listRooms();
        if (rooms.isEmpty()) {
            System.out.println("No rooms found.");
            return;
        }
        
        System.out.println("\n📋 ALL ROOMS:");
        System.out.println("-".repeat(50));
        rooms.forEach(System.out::println);
    }
    
    private static void searchRoomsByCapacity() {
        try {
            System.out.print("Minimum capacity: ");
            int minCapacity = Integer.parseInt(scanner.nextLine().trim());
            
            List<Room> rooms = service.searchRoomsByCapacity(minCapacity);
            if (rooms.isEmpty()) {
                System.out.println("No rooms found with capacity >= " + minCapacity);
                return;
            }
            
            System.out.println("\n📋 ROOMS WITH CAPACITY >= " + minCapacity + ":");
            System.out.println("-".repeat(50));
            rooms.forEach(System.out::println);
            
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid capacity number");
        }
    }
    
    private static void removeRoom() {
        try {
            System.out.print("Room ID to remove: ");
            String roomId = ValidationUtils.sanitizeInput(scanner.nextLine());
            
            if (service.removeRoom(roomId)) {
                System.out.println("✅ Room removed successfully");
            } else {
                System.out.println("❌ Room not found");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error removing room: " + e.getMessage());
        }
    }
    
    // ==================== GUEST MANAGEMENT ====================
    
    private static void handleGuestManagement() {
        System.out.println("\n👥 GUEST MANAGEMENT");
        System.out.println("1. Add Guest");
        System.out.println("2. List All Guests");
        System.out.println("3. Search Guests by Name");
        System.out.println("4. List VIP Guests");
        System.out.println("5. Update Guest VIP Status");
        System.out.println("0. Back to Main Menu");
        System.out.print("Choice: ");
        
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                addGuest();
                break;
            case "2":
                listGuests();
                break;
            case "3":
                searchGuestsByName();
                break;
            case "4":
                listVipGuests();
                break;
            case "5":
                updateGuestVipStatus();
                break;
            case "0":
                System.out.println("Returning to main menu...");
                break;
            default:
                System.out.println("❌ Invalid choice");
        }
    }
    
    private static void addGuest() {
        try {
            System.out.print("Guest ID: ");
            String id = ValidationUtils.sanitizeInput(scanner.nextLine());
            
            System.out.print("First Name: ");
            String firstName = ValidationUtils.sanitizeInput(scanner.nextLine());
            
            System.out.print("Last Name: ");
            String lastName = ValidationUtils.sanitizeInput(scanner.nextLine());
            
            System.out.print("Email: ");
            String email = ValidationUtils.sanitizeInput(scanner.nextLine());
            
            System.out.print("Phone: ");
            String phone = ValidationUtils.sanitizeInput(scanner.nextLine());
            
            Guest guest = service.addGuest(new Guest(id, firstName, lastName, email, phone));
            System.out.println("✅ Guest added successfully: " + guest);
            
        } catch (Exception e) {
            System.out.println("❌ Error adding guest: " + e.getMessage());
        }
    }
    
    private static void listGuests() {
        List<Guest> guests = service.listGuests();
        if (guests.isEmpty()) {
            System.out.println("No guests found.");
            return;
        }
        
        System.out.println("\n📋 ALL GUESTS:");
        System.out.println("-".repeat(50));
        guests.forEach(System.out::println);
    }
    
    private static void searchGuestsByName() {
        System.out.print("Search name: ");
        String name = scanner.nextLine().trim();
        
        List<Guest> guests = service.searchGuestsByName(name);
        if (guests.isEmpty()) {
            System.out.println("No guests found matching: " + name);
            return;
        }
        
        System.out.println("\n📋 SEARCH RESULTS:");
        System.out.println("-".repeat(50));
        guests.forEach(System.out::println);
    }
    
    private static void listVipGuests() {
        List<Guest> vipGuests = service.getVipGuests();
        if (vipGuests.isEmpty()) {
            System.out.println("No VIP guests found.");
            return;
        }
        
        System.out.println("\n⭐ VIP GUESTS:");
        System.out.println("-".repeat(50));
        vipGuests.forEach(System.out::println);
    }
    
    private static void updateGuestVipStatus() {
        try {
            System.out.print("Guest ID: ");
            String guestId = ValidationUtils.sanitizeInput(scanner.nextLine());
            
            Optional<Guest> guestOpt = service.getGuestById(guestId);
            if (guestOpt.isEmpty()) {
                System.out.println("❌ Guest not found");
                return;
            }
            
            Guest guest = guestOpt.get();
            System.out.print("Set VIP status (true/false): ");
            boolean vipStatus = Boolean.parseBoolean(scanner.nextLine().trim());
            
            guest.setVipStatus(vipStatus);
            service.updateGuest(guest);
            
            System.out.println("✅ VIP status updated: " + guest);
            
        } catch (Exception e) {
            System.out.println("❌ Error updating VIP status: " + e.getMessage());
        }
    }
    
    // ==================== RESERVATION MANAGEMENT ====================
    
    private static void handleReservationManagement() {
        System.out.println("\n📅 RESERVATION MANAGEMENT");
        System.out.println("1. Create Reservation");
        System.out.println("2. List All Reservations");
        System.out.println("3. List Reservations by Room");
        System.out.println("4. List Reservations by Guest");
        System.out.println("5. Check Room Availability");
        System.out.println("6. Cancel Reservation");
        System.out.println("0. Back to Main Menu");
        System.out.print("Choice: ");
        
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                createReservation();
                break;
            case "2":
                listAllReservations();
                break;
            case "3":
                listReservationsByRoom();
                break;
            case "4":
                listReservationsByGuest();
                break;
            case "5":
                checkRoomAvailability();
                break;
            case "6":
                cancelReservation();
                break;
            case "0":
                System.out.println("Returning to main menu...");
                break;
            default:
                System.out.println("❌ Invalid choice");
        }
    }
    
    private static void createReservation() {
        try {
            System.out.print("Reservation ID: ");
            String reservationId = ValidationUtils.sanitizeInput(scanner.nextLine());
            
            System.out.print("Room ID: ");
            String roomId = ValidationUtils.sanitizeInput(scanner.nextLine());
            
            System.out.print("Guest ID: ");
            String guestId = ValidationUtils.sanitizeInput(scanner.nextLine());
            
            System.out.print("Start Date (YYYY-MM-DD): ");
            String startDateStr = scanner.nextLine().trim();
            LocalDate startDate = ValidationUtils.parseDate(startDateStr);
            
            System.out.print("End Date (YYYY-MM-DD): ");
            String endDateStr = scanner.nextLine().trim();
            LocalDate endDate = ValidationUtils.parseDate(endDateStr);
            
            System.out.print("Party Size: ");
            int partySize = Integer.parseInt(scanner.nextLine().trim());
            
            var reservation = service.createReservation(reservationId, roomId, guestId, startDate, endDate, partySize);
            System.out.println("✅ Reservation created successfully: " + reservation);
            
        } catch (DateTimeParseException e) {
            System.out.println("❌ Invalid date format. Use YYYY-MM-DD");
        } catch (Exception e) {
            System.out.println("❌ Error creating reservation: " + e.getMessage());
        }
    }
    
    private static void listAllReservations() {
        var reservations = service.listAllReservations();
        if (reservations.isEmpty()) {
            System.out.println("No reservations found.");
            return;
        }
        
        System.out.println("\n📋 ALL RESERVATIONS:");
        System.out.println("-".repeat(50));
        reservations.forEach(System.out::println);
    }
    
    private static void listReservationsByRoom() {
        System.out.print("Room ID: ");
        String roomId = ValidationUtils.sanitizeInput(scanner.nextLine());
        
        var reservations = service.listReservationsForRoom(roomId);
        if (reservations.isEmpty()) {
            System.out.println("No reservations found for room: " + roomId);
            return;
        }
        
        System.out.println("\n📋 RESERVATIONS FOR ROOM " + roomId + ":");
        System.out.println("-".repeat(50));
        reservations.forEach(System.out::println);
    }
    
    private static void listReservationsByGuest() {
        System.out.print("Guest ID: ");
        String guestId = ValidationUtils.sanitizeInput(scanner.nextLine());
        
        var reservations = service.listReservationsForGuest(guestId);
        if (reservations.isEmpty()) {
            System.out.println("No reservations found for guest: " + guestId);
            return;
        }
        
        System.out.println("\n📋 RESERVATIONS FOR GUEST " + guestId + ":");
        System.out.println("-".repeat(50));
        reservations.forEach(System.out::println);
    }
    
    private static void checkRoomAvailability() {
        try {
            System.out.print("Room ID (or 'all' for all rooms): ");
            String roomId = ValidationUtils.sanitizeInput(scanner.nextLine());
            
            System.out.print("Start Date (YYYY-MM-DD): ");
            String startDateStr = scanner.nextLine().trim();
            LocalDate startDate = ValidationUtils.parseDate(startDateStr);
            
            System.out.print("End Date (YYYY-MM-DD): ");
            String endDateStr = scanner.nextLine().trim();
            LocalDate endDate = ValidationUtils.parseDate(endDateStr);
            
            if ("all".equalsIgnoreCase(roomId)) {
                var availableRooms = service.getAvailableRooms(startDate, endDate);
                if (availableRooms.isEmpty()) {
                    System.out.println("❌ No rooms available for the specified period");
                } else {
                    System.out.println("\n✅ AVAILABLE ROOMS:");
                    System.out.println("-".repeat(50));
                    availableRooms.forEach(System.out::println);
                }
            } else {
                boolean available = service.isRoomAvailable(roomId, startDate, endDate);
                System.out.println(available ? "✅ Room is available" : "❌ Room is not available");
            }
            
        } catch (DateTimeParseException e) {
            System.out.println("❌ Invalid date format. Use YYYY-MM-DD");
        } catch (Exception e) {
            System.out.println("❌ Error checking availability: " + e.getMessage());
        }
    }
    
    private static void cancelReservation() {
        try {
            System.out.print("Reservation ID to cancel: ");
            String reservationId = ValidationUtils.sanitizeInput(scanner.nextLine());
            
            var cancelled = service.cancelReservation(reservationId);
            if (cancelled.isPresent()) {
                System.out.println("✅ Reservation cancelled: " + cancelled.get());
            } else {
                System.out.println("❌ Reservation not found");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error cancelling reservation: " + e.getMessage());
        }
    }
    
    // ==================== PAYMENT MANAGEMENT ====================
    
    private static void handlePaymentManagement() {
        System.out.println("\n💰 PAYMENT MANAGEMENT");
        System.out.println("1. Add Payment");
        System.out.println("2. List Payments by Reservation");
        System.out.println("3. List Payments by Guest");
        System.out.println("4. Get Total Payments for Reservation");
        System.out.println("0. Back to Main Menu");
        System.out.print("Choice: ");
        
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                addPayment();
                break;
            case "2":
                listPaymentsByReservation();
                break;
            case "3":
                listPaymentsByGuest();
                break;
            case "4":
                getTotalPayments();
                break;
            case "0":
                System.out.println("Returning to main menu...");
                break;
            default:
                System.out.println("❌ Invalid choice");
        }
    }
    
    private static void addPayment() {
        try {
            System.out.print("Payment ID: ");
            String paymentId = ValidationUtils.sanitizeInput(scanner.nextLine());
            
            System.out.print("Reservation ID: ");
            String reservationId = ValidationUtils.sanitizeInput(scanner.nextLine());
            
            System.out.print("Guest ID: ");
            String guestId = ValidationUtils.sanitizeInput(scanner.nextLine());
            
            System.out.print("Amount: ");
            BigDecimal amount = new BigDecimal(scanner.nextLine().trim());
            
            System.out.println("Payment Methods:");
            System.out.println("1. CASH  2. CREDIT_CARD  3. DEBIT_CARD  4. BANK_TRANSFER  5. ONLINE");
            System.out.print("Choose payment method (1-5): ");
            int methodChoice = Integer.parseInt(scanner.nextLine().trim());
            
            Payment.PaymentMethod method = switch (methodChoice) {
                case 1 -> Payment.PaymentMethod.CASH;
                case 2 -> Payment.PaymentMethod.CREDIT_CARD;
                case 3 -> Payment.PaymentMethod.DEBIT_CARD;
                case 4 -> Payment.PaymentMethod.BANK_TRANSFER;
                case 5 -> Payment.PaymentMethod.ONLINE;
                default -> throw new IllegalArgumentException("Invalid payment method");
            };
            
            Payment payment = new Payment(paymentId, reservationId, guestId, amount, method);
            payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
            
            service.addPayment(payment);
            System.out.println("✅ Payment added successfully: " + payment);
            
        } catch (Exception e) {
            System.out.println("❌ Error adding payment: " + e.getMessage());
        }
    }
    
    private static void listPaymentsByReservation() {
        System.out.print("Reservation ID: ");
        String reservationId = ValidationUtils.sanitizeInput(scanner.nextLine());
        
        var payments = service.listPaymentsForReservation(reservationId);
        if (payments.isEmpty()) {
            System.out.println("No payments found for reservation: " + reservationId);
            return;
        }
        
        System.out.println("\n📋 PAYMENTS FOR RESERVATION " + reservationId + ":");
        System.out.println("-".repeat(50));
        payments.forEach(System.out::println);
    }
    
    private static void listPaymentsByGuest() {
        System.out.print("Guest ID: ");
        String guestId = ValidationUtils.sanitizeInput(scanner.nextLine());
        
        var payments = service.listPaymentsForGuest(guestId);
        if (payments.isEmpty()) {
            System.out.println("No payments found for guest: " + guestId);
            return;
        }
        
        System.out.println("\n📋 PAYMENTS FOR GUEST " + guestId + ":");
        System.out.println("-".repeat(50));
        payments.forEach(System.out::println);
    }
    
    private static void getTotalPayments() {
        System.out.print("Reservation ID: ");
        String reservationId = ValidationUtils.sanitizeInput(scanner.nextLine());
        
        BigDecimal total = service.getTotalPaymentsForReservation(reservationId);
        System.out.println("💰 Total payments for reservation " + reservationId + ": $" + total);
    }
    
    // ==================== REPORTS & SEARCH ====================
    
    private static void handleReportsAndSearch() {
        System.out.println("\n📊 REPORTS & SEARCH");
        System.out.println("1. Generate Occupancy Report");
        System.out.println("2. Search Available Rooms");
        System.out.println("3. System Statistics");
        System.out.println("0. Back to Main Menu");
        System.out.print("Choice: ");
        
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                generateOccupancyReport();
                break;
            case "2":
                searchAvailableRooms();
                break;
            case "3":
                showSystemStatistics();
                break;
            case "0":
                System.out.println("Returning to main menu...");
                break;
            default:
                System.out.println("❌ Invalid choice");
        }
    }
    
    private static void generateOccupancyReport() {
        try {
            System.out.print("Start Date (YYYY-MM-DD): ");
            String startDateStr = scanner.nextLine().trim();
            LocalDate startDate = ValidationUtils.parseDate(startDateStr);
            
            System.out.print("End Date (YYYY-MM-DD): ");
            String endDateStr = scanner.nextLine().trim();
            LocalDate endDate = ValidationUtils.parseDate(endDateStr);
            
            Map<String, Object> report = service.generateOccupancyReport(startDate, endDate);
            
            System.out.println("\n📊 OCCUPANCY REPORT");
            System.out.println("=".repeat(50));
            report.forEach((key, value) -> 
                System.out.println(formatReportKey(key) + ": " + value));
            
        } catch (DateTimeParseException e) {
            System.out.println("❌ Invalid date format. Use YYYY-MM-DD");
        } catch (Exception e) {
            System.out.println("❌ Error generating report: " + e.getMessage());
        }
    }
    
    private static void searchAvailableRooms() {
        try {
            System.out.print("Start Date (YYYY-MM-DD): ");
            String startDateStr = scanner.nextLine().trim();
            LocalDate startDate = ValidationUtils.parseDate(startDateStr);
            
            System.out.print("End Date (YYYY-MM-DD): ");
            String endDateStr = scanner.nextLine().trim();
            LocalDate endDate = ValidationUtils.parseDate(endDateStr);
            
            var availableRooms = service.getAvailableRooms(startDate, endDate);
            if (availableRooms.isEmpty()) {
                System.out.println("❌ No rooms available for the specified period");
            } else {
                System.out.println("\n✅ AVAILABLE ROOMS (" + startDate + " to " + endDate + "):");
                System.out.println("-".repeat(50));
                availableRooms.forEach(System.out::println);
            }
            
        } catch (DateTimeParseException e) {
            System.out.println("❌ Invalid date format. Use YYYY-MM-DD");
        } catch (Exception e) {
            System.out.println("❌ Error searching rooms: " + e.getMessage());
        }
    }
    
    private static void showSystemStatistics() {
        System.out.println("\n📈 SYSTEM STATISTICS");
        System.out.println("=".repeat(50));
        System.out.println("Total Rooms: " + service.listRooms().size());
        System.out.println("Total Guests: " + service.listGuests().size());
        System.out.println("VIP Guests: " + service.getVipGuests().size());
        System.out.println("Total Reservations: " + service.listAllReservations().size());
        
        // Active reservations (not ended yet)
        long activeReservations = service.listAllReservations().stream()
                .filter(res -> res.getEndDate().isAfter(LocalDate.now()))
                .count();
        System.out.println("Active Reservations: " + activeReservations);
    }
    
    private static String formatReportKey(String key) {
        return switch (key) {
            case "totalRooms" -> "Total Rooms";
            case "totalReservations" -> "Total Reservations";
            case "totalGuests" -> "Total Guests";
            case "totalPayments" -> "Total Payments";
            case "reportPeriod" -> "Report Period";
            case "generatedAt" -> "Generated At";
            case "occupancyRate" -> "Occupancy Rate";
            default -> key;
        };
    }
}
