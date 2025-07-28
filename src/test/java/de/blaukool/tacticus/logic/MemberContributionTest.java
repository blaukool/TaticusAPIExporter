package de.blaukool.tacticus.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MemberContributionTest {

    private MemberContribution memberContribution;

    @BeforeEach
    public void setUp() {
        memberContribution = new MemberContribution();
    }

    @Test
    public void testDefaultValues() {
        // Test that default values are set correctly
        assertNull(memberContribution.getName());
        assertEquals(Integer.valueOf(0), memberContribution.getDamage());
    }

    @Test
    public void testSetAndGetName() {
        // Test setting and getting name
        String testName = "TestPlayer";
        memberContribution.setName(testName);
        assertEquals(testName, memberContribution.getName());
    }

    @Test
    public void testSetAndGetName_Null() {
        // Test setting null name
        memberContribution.setName(null);
        assertNull(memberContribution.getName());
    }

    @Test
    public void testSetAndGetName_EmptyString() {
        // Test setting empty string name
        String emptyName = "";
        memberContribution.setName(emptyName);
        assertEquals(emptyName, memberContribution.getName());
    }

    @Test
    public void testSetAndGetDamage() {
        // Test setting and getting damage
        Integer testDamage = 1000;
        memberContribution.setDamage(testDamage);
        assertEquals(testDamage, memberContribution.getDamage());
    }

    @Test
    public void testSetAndGetDamage_Zero() {
        // Test setting zero damage
        Integer zeroDamage = 0;
        memberContribution.setDamage(zeroDamage);
        assertEquals(zeroDamage, memberContribution.getDamage());
    }

    @Test
    public void testSetAndGetDamage_Negative() {
        // Test setting negative damage
        Integer negativeDamage = -500;
        memberContribution.setDamage(negativeDamage);
        assertEquals(negativeDamage, memberContribution.getDamage());
    }

    @Test
    public void testSetAndGetDamage_Null() {
        // Test setting null damage
        memberContribution.setDamage(null);
        assertNull(memberContribution.getDamage());
    }

    @Test
    public void testAddDamage_FromZero() {
        // Test adding damage from initial zero value
        Integer additionalDamage = 500;
        memberContribution.addDamage(additionalDamage);
        assertEquals(Integer.valueOf(500), memberContribution.getDamage());
    }

    @Test
    public void testAddDamage_FromExistingValue() {
        // Test adding damage to existing value
        memberContribution.setDamage(1000);
        memberContribution.addDamage(500);
        assertEquals(Integer.valueOf(1500), memberContribution.getDamage());
    }

    @Test
    public void testAddDamage_Multiple() {
        // Test adding damage multiple times
        memberContribution.addDamage(100);
        memberContribution.addDamage(200);
        memberContribution.addDamage(300);
        assertEquals(Integer.valueOf(600), memberContribution.getDamage());
    }

    @Test
    public void testAddDamage_Zero() {
        // Test adding zero damage
        memberContribution.setDamage(500);
        memberContribution.addDamage(0);
        assertEquals(Integer.valueOf(500), memberContribution.getDamage());
    }

    @Test
    public void testAddDamage_Negative() {
        // Test adding negative damage (should decrease total)
        memberContribution.setDamage(1000);
        memberContribution.addDamage(-300);
        assertEquals(Integer.valueOf(700), memberContribution.getDamage());
    }

    @Test
    public void testAddDamage_Null() {
        // Test adding null damage (should throw NullPointerException)
        memberContribution.setDamage(500);
        assertThrows(NullPointerException.class, () -> {
            memberContribution.addDamage(null);
        });
        // Damage should remain unchanged after exception
        assertEquals(Integer.valueOf(500), memberContribution.getDamage());
    }

    @Test
    public void testAddDamage_WhenDamageIsNull() {
        // Test adding damage when current damage is null (should throw NullPointerException)
        memberContribution.setDamage(null);
        assertThrows(NullPointerException.class, () -> {
            memberContribution.addDamage(500);
        });
        // Should remain null after exception
        assertNull(memberContribution.getDamage());
    }

    @Test
    public void testCompleteWorkflow() {
        // Test a complete workflow
        String playerName = "TestPlayer";
        memberContribution.setName(playerName);
        
        // Add multiple damage values
        memberContribution.addDamage(1000);
        memberContribution.addDamage(2500);
        memberContribution.addDamage(750);
        
        // Verify final state
        assertEquals(playerName, memberContribution.getName());
        assertEquals(Integer.valueOf(4250), memberContribution.getDamage());
    }

    @Test
    public void testLargeDamageValues() {
        // Test with large damage values
        memberContribution.setDamage(Integer.MAX_VALUE - 1000);
        memberContribution.addDamage(500);
        assertEquals(Integer.valueOf(Integer.MAX_VALUE - 500), memberContribution.getDamage());
    }

    @Test
    public void testOverflowHandling() {
        // Test integer overflow behavior
        memberContribution.setDamage(Integer.MAX_VALUE);
        memberContribution.addDamage(1);
        // This will overflow to Integer.MIN_VALUE
        assertEquals(Integer.valueOf(Integer.MIN_VALUE), memberContribution.getDamage());
    }

    @Test
    public void testUnderflowHandling() {
        // Test integer underflow behavior
        memberContribution.setDamage(Integer.MIN_VALUE);
        memberContribution.addDamage(-1);
        // This will underflow to Integer.MAX_VALUE
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), memberContribution.getDamage());
    }

    @Test
    public void testNameWithSpecialCharacters() {
        // Test names with special characters
        String specialName = "Player@#$%^&*()_+-=[]{}|;':\",./<>?";
        memberContribution.setName(specialName);
        assertEquals(specialName, memberContribution.getName());
    }

    @Test
    public void testNameWithUnicodeCharacters() {
        // Test names with Unicode characters
        String unicodeName = "玩家测试用户名";
        memberContribution.setName(unicodeName);
        assertEquals(unicodeName, memberContribution.getName());
    }

    @Test
    public void testVeryLongName() {
        // Test with very long name
        String longName = "A".repeat(1000);
        memberContribution.setName(longName);
        assertEquals(longName, memberContribution.getName());
        assertEquals(1000, memberContribution.getName().length());
    }

    @Test
    public void testSequentialOperations() {
        // Test multiple sequential operations
        memberContribution.setName("InitialName");
        memberContribution.setDamage(100);
        
        memberContribution.addDamage(50);
        assertEquals(Integer.valueOf(150), memberContribution.getDamage());
        
        memberContribution.setName("UpdatedName");
        assertEquals("UpdatedName", memberContribution.getName());
        
        memberContribution.addDamage(25);
        assertEquals(Integer.valueOf(175), memberContribution.getDamage());
        
        memberContribution.setDamage(0);
        assertEquals(Integer.valueOf(0), memberContribution.getDamage());
        assertEquals("UpdatedName", memberContribution.getName());
    }
}