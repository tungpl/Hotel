package com.example.hotel.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Reservation uses [startDate, endDate) semantics: start inclusive, end exclusive.
 */
public class Reservation {
    @JsonProperty("id")
    private final String id;
    
    @JsonProperty("roomId")
    private final String roomId;
    
    @JsonProperty("guestName")
    private final String guestName;
    
    @JsonProperty("startDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate startDate; // inclusive
    
    @JsonProperty("endDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate endDate;   // exclusive
    
    @JsonProperty("partySize")
    private final int partySize;

    // Default constructor for JSON deserialization
    public Reservation() {
        this.id = "";
        this.roomId = "";
        this.guestName = "";
        this.startDate = LocalDate.now();
        this.endDate = LocalDate.now().plusDays(1);
        this.partySize = 1;
    }

    public Reservation(String id, String roomId, String guestName, LocalDate startDate, LocalDate endDate, int partySize) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id required");
        if (roomId == null || roomId.isBlank()) throw new IllegalArgumentException("roomId required");
        if (guestName == null || guestName.isBlank()) throw new IllegalArgumentException("guestName required");
        if (startDate == null || endDate == null) throw new IllegalArgumentException("dates required");
        if (!startDate.isBefore(endDate)) throw new IllegalArgumentException("startDate must be before endDate");
        if (partySize <= 0) throw new IllegalArgumentException("partySize must be > 0");
        this.id = id;
        this.roomId = roomId;
        this.guestName = guestName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.partySize = partySize;
    }

    public String getId() { return id; }
    public String getRoomId() { return roomId; }
    public String getGuestName() { return guestName; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public int getPartySize() { return partySize; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation)) return false;
        Reservation that = (Reservation) o;
        return id.equals(that.id);
    }

    @Override public int hashCode() { return Objects.hash(id); }

    @Override public String toString() {
        return "Reservation{" +
                "id='" + id + '\'' +
                ", roomId='" + roomId + '\'' +
                ", guestName='" + guestName + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", partySize=" + partySize +
                '}';
    }
}

