package com.example.hotel.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Configuration manager for reading config.json
 */
public class ConfigManager {
    private static final Logger logger = Logger.getLogger(ConfigManager.class.getName());
    private static final String CONFIG_FILE = "config.json";
    private static JsonNode config;
    
    static {
        loadConfiguration();
    }
    
    private static void loadConfiguration() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File configFile = new File(CONFIG_FILE);
            if (configFile.exists()) {
                config = mapper.readTree(configFile);
                logger.info("Configuration loaded successfully from " + CONFIG_FILE);
            } else {
                logger.warning("Configuration file " + CONFIG_FILE + " not found");
                config = mapper.createObjectNode();
            }
        } catch (IOException e) {
            logger.severe("Failed to load configuration: " + e.getMessage());
            config = new ObjectMapper().createObjectNode();
        }
    }
    
    public static String getString(String path, String defaultValue) {
        try {
            String[] parts = path.split("\\.");
            JsonNode node = config;
            for (String part : parts) {
                node = node.get(part);
                if (node == null) return defaultValue;
            }
            return node.asText(defaultValue);
        } catch (Exception e) {
            logger.warning("Error getting config value for " + path + ": " + e.getMessage());
            return defaultValue;
        }
    }
    
    public static int getInt(String path, int defaultValue) {
        try {
            String[] parts = path.split("\\.");
            JsonNode node = config;
            for (String part : parts) {
                node = node.get(part);
                if (node == null) return defaultValue;
            }
            return node.asInt(defaultValue);
        } catch (Exception e) {
            logger.warning("Error getting config value for " + path + ": " + e.getMessage());
            return defaultValue;
        }
    }
    
    public static boolean getBoolean(String path, boolean defaultValue) {
        try {
            String[] parts = path.split("\\.");
            JsonNode node = config;
            for (String part : parts) {
                node = node.get(part);
                if (node == null) return defaultValue;
            }
            return node.asBoolean(defaultValue);
        } catch (Exception e) {
            logger.warning("Error getting config value for " + path + ": " + e.getMessage());
            return defaultValue;
        }
    }
    
    // Convenience methods for common configuration paths
    public static String getDataDirectory() {
        return getString("database.dataDirectory", "data");
    }
    
    public static String getBackupDirectory() {
        return getString("database.backupDirectory", "backups");
    }
    
    public static String getReportDirectory() {
        return getString("database.reportDirectory", "reports");
    }
    
    public static boolean isAutoBackupEnabled() {
        return getBoolean("database.autoBackup", true);
    }
    
    public static int getBackupInterval() {
        return getInt("database.backupInterval", 60);
    }
    
    public static String getLogLevel() {
        return getString("logging.level", "INFO");
    }
    
    public static boolean isLogToFileEnabled() {
        return getBoolean("logging.logToFile", true);
    }
    
    public static String getLogFile() {
        return getString("logging.logFile", "hotel.log");
    }
}