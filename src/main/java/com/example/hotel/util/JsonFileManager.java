package com.example.hotel.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Utility class for JSON file operations
 */
public class JsonFileManager {
    private static final Logger logger = Logger.getLogger(JsonFileManager.class.getName());
    private static final ObjectMapper objectMapper = createObjectMapper();
    
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }
    
    /**
     * Save list of objects to JSON file
     */
    public static <T> void saveToFile(List<T> objects, String filePath) throws IOException {
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs(); // Create directories if they don't exist
            objectMapper.writeValue(file, objects);
            logger.info("Successfully saved " + objects.size() + " objects to " + filePath);
        } catch (IOException e) {
            logger.severe("Failed to save objects to " + filePath + ": " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Load list of objects from JSON file
     */
    public static <T> List<T> loadFromFile(String filePath, Class<T> clazz) throws IOException {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                logger.info("File " + filePath + " does not exist, returning empty list");
                return List.of();
            }
            
            List<T> objects = objectMapper.readValue(file, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
            logger.info("Successfully loaded " + objects.size() + " objects from " + filePath);
            return objects;
        } catch (IOException e) {
            logger.severe("Failed to load objects from " + filePath + ": " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Save single object to JSON file
     */
    public static <T> void saveObjectToFile(T object, String filePath) throws IOException {
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            objectMapper.writeValue(file, object);
            logger.info("Successfully saved object to " + filePath);
        } catch (IOException e) {
            logger.severe("Failed to save object to " + filePath + ": " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Load single object from JSON file
     */
    public static <T> T loadObjectFromFile(String filePath, Class<T> clazz) throws IOException {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return null;
            }
            
            T object = objectMapper.readValue(file, clazz);
            logger.info("Successfully loaded object from " + filePath);
            return object;
        } catch (IOException e) {
            logger.severe("Failed to load object from " + filePath + ": " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Check if file exists
     */
    public static boolean fileExists(String filePath) {
        return new File(filePath).exists();
    }
    
    /**
     * Create backup of a file
     */
    public static void createBackup(String filePath, String backupPath) throws IOException {
        File source = new File(filePath);
        File backup = new File(backupPath);
        
        if (source.exists()) {
            backup.getParentFile().mkdirs();
            java.nio.file.Files.copy(source.toPath(), backup.toPath(), 
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            logger.info("Created backup from " + filePath + " to " + backupPath);
        }
    }
}