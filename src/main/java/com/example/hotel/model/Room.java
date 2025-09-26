package com.example.hotel.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class Room {
    @JsonProperty("id")
    private final String id;
    
    @JsonProperty("number")
    private final String number;
    
    @JsonProperty("capacity")
    private final int capacity;

    // Default constructor for JSON deserialization
    public Room() {
        this.id = "";
        this.number = "";
        this.capacity = 0;
    }

    public Room(String id, String number, int capacity) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id required");
        if (number == null || number.isBlank()) throw new IllegalArgumentException("number required");
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        this.id = id;
        this.number = number;
        this.capacity = capacity;
    }

    public String getId() { return id; }
    public String getNumber() { return number; }
    public int getCapacity() { return capacity; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Room)) return false;
        Room room = (Room) o;
        return id.equals(room.id);
    }

    @Override public int hashCode() { return Objects.hash(id); }

    @Override public String toString() {
        return "Room{" +
                "id='" + id + '\'' +
                ", number='" + number + '\'' +
                ", capacity=" + capacity +
                '}';
    }
}

