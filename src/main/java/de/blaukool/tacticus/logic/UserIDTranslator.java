package de.blaukool.tacticus.logic;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class UserIDTranslator {
    private static final String USER_MAPPING_FILE = "/userMapping.properties";
    private final Map<String, String> userIdToNameMap;

    public UserIDTranslator() {
        this.userIdToNameMap = loadUserMappings();
    }

    private Map<String, String> loadUserMappings() {
        Map<String, String> mappings = new HashMap<>();
        
        try (InputStream inputStream = getClass().getResourceAsStream(USER_MAPPING_FILE)) {
            if (inputStream != null) {
                Properties properties = new Properties();
                properties.load(inputStream);
                
                for (String key : properties.stringPropertyNames()) {
                    mappings.put(key, properties.getProperty(key));
                }
            } else {
                System.err.println("Warning: User mapping file not found at " + USER_MAPPING_FILE);
            }
        } catch (IOException e) {
            System.err.println("Error loading user mappings: " + e.getMessage());
        }
        
        return mappings;
    }

    public String getUserName(String userId) {
        return userIdToNameMap.getOrDefault(userId, "unknown");
    }

    public boolean hasMapping(String userId) {
        return userIdToNameMap.containsKey(userId);
    }

    public int getMappingsCount() {
        return userIdToNameMap.size();
    }
}