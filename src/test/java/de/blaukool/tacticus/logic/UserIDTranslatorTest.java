package de.blaukool.tacticus.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserIDTranslatorTest {

    private UserIDTranslator userIDTranslator;

    @BeforeEach
    public void setUp() throws Exception {
        // Create a UserIDTranslator with mocked data
        userIDTranslator = new UserIDTranslator() {
            @Override
            protected Map<String, String> loadUserMappings() {
                Map<String, String> mappings = new HashMap<>();
                mappings.put("user1", "TestUser1");
                mappings.put("user2", "TestUser2");
                mappings.put("user3", "TestUser3");
                return mappings;
            }
        };
    }

    @Test
    public void testGetUserName_ExistingUser() {
        // Test getting an existing user name
        String result = userIDTranslator.getUserName("user1");
        assertEquals("TestUser1", result);
    }

    @Test
    public void testGetUserName_NonExistingUser() {
        // Test getting a non-existing user name should return "unknown"
        String result = userIDTranslator.getUserName("nonexistent");
        assertEquals("unknown", result);
    }

    @Test
    public void testGetUserName_NullUser() {
        // Test getting a null user should return "unknown"
        String result = userIDTranslator.getUserName(null);
        assertEquals("unknown", result);
    }

    @Test
    public void testHasMapping_ExistingUser() {
        // Test checking if mapping exists for existing user
        boolean result = userIDTranslator.hasMapping("user2");
        assertTrue(result);
    }

    @Test
    public void testHasMapping_NonExistingUser() {
        // Test checking if mapping exists for non-existing user
        boolean result = userIDTranslator.hasMapping("nonexistent");
        assertFalse(result);
    }

    @Test
    public void testGetMappingsCount() {
        // Test getting the count of mappings
        int count = userIDTranslator.getMappingsCount();
        assertEquals(3, count);
    }

    @Test
    public void testUserIDTranslator_EmptyMappings() {
        // Test behavior when no mappings are loaded
        UserIDTranslator emptyTranslator = new UserIDTranslator() {
            @Override
            protected Map<String, String> loadUserMappings() {
                return new HashMap<>();
            }
        };
        
        // Should return "unknown" for any user when no mappings
        String result = emptyTranslator.getUserName("anyuser");
        assertEquals("unknown", result);
        
        // Should have 0 mappings
        assertEquals(0, emptyTranslator.getMappingsCount());
        assertFalse(emptyTranslator.hasMapping("anyuser"));
    }

    @Test
    public void testUserIDTranslator_NullMappings() {
        // Test behavior when null mappings are returned
        UserIDTranslator nullTranslator = new UserIDTranslator() {
            @Override
            protected Map<String, String> loadUserMappings() {
                return null;
            }
        };
        
        // Should handle null mappings gracefully
        assertThrows(NullPointerException.class, () -> {
            nullTranslator.getUserName("anyuser");
        });
    }

    @Test
    public void testUserIDTranslator_RealFileLoading() {
        // Test with actual UserIDTranslator that tries to load from resources
        UserIDTranslator realTranslator = new UserIDTranslator();
        
        // This should work with the actual userMapping.properties file
        // or return "unknown" if file doesn't exist
        String result = realTranslator.getUserName("nonexistent-user");
        assertEquals("unknown", result);
        
        // Should not throw exception
        int count = realTranslator.getMappingsCount();
        assertTrue(count >= 0);
    }

    @Test
    public void testUserIDTranslator_CaseSensitivity() {
        // Test that user IDs are case sensitive
        String result1 = userIDTranslator.getUserName("user1");
        String result2 = userIDTranslator.getUserName("USER1");
        
        assertEquals("TestUser1", result1);
        assertEquals("unknown", result2);
    }

    @Test
    public void testUserIDTranslator_SpecialCharacters() {
        // Test with user IDs containing special characters
        UserIDTranslator specialTranslator = new UserIDTranslator() {
            @Override
            protected Map<String, String> loadUserMappings() {
                Map<String, String> mappings = new HashMap<>();
                mappings.put("user-with-dash", "DashUser");
                mappings.put("user_with_underscore", "UnderscoreUser");
                mappings.put("user.with.dots", "DotUser");
                return mappings;
            }
        };
        
        assertEquals("DashUser", specialTranslator.getUserName("user-with-dash"));
        assertEquals("UnderscoreUser", specialTranslator.getUserName("user_with_underscore"));
        assertEquals("DotUser", specialTranslator.getUserName("user.with.dots"));
    }

    @Test
    public void testUserIDTranslator_EmptyStrings() {
        // Test with empty string user ID
        UserIDTranslator emptyStringTranslator = new UserIDTranslator() {
            @Override
            protected Map<String, String> loadUserMappings() {
                Map<String, String> mappings = new HashMap<>();
                mappings.put("", "EmptyUser");
                mappings.put("normal", "NormalUser");
                return mappings;
            }
        };
        
        assertEquals("EmptyUser", emptyStringTranslator.getUserName(""));
        assertEquals("NormalUser", emptyStringTranslator.getUserName("normal"));
    }
}