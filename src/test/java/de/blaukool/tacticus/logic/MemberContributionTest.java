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
        assertNull(memberContribution.getRole());
        assertEquals(Integer.valueOf(0), memberContribution.getBossBattle());
        assertEquals(Integer.valueOf(0), memberContribution.getSidebossBattle());
        assertEquals(Integer.valueOf(0), memberContribution.getBossBomb());
        assertEquals(Integer.valueOf(0), memberContribution.getSidebossBomb());
        assertEquals(Integer.valueOf(0), memberContribution.getBattleCount());
        assertEquals(Integer.valueOf(0), memberContribution.getBombCount());
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
    public void testSetAndGetRole() {
        // Test setting and getting role
        String testRole = "LEADER";
        memberContribution.setRole(testRole);
        assertEquals(testRole, memberContribution.getRole());
    }

    @Test
    public void testSetAndGetRole_Null() {
        // Test setting null role
        memberContribution.setRole(null);
        assertNull(memberContribution.getRole());
    }

    @Test
    public void testSetAndGetRole_EmptyString() {
        // Test setting empty string role
        String emptyRole = "";
        memberContribution.setRole(emptyRole);
        assertEquals(emptyRole, memberContribution.getRole());
    }

    @Test
    public void testAllRoleTypes() {
        // Test all possible role types
        String[] roles = {"LEADER", "OFFICER", "MEMBER"};
        
        for (String role : roles) {
            memberContribution.setRole(role);
            assertEquals(role, memberContribution.getRole());
        }
    }

    @Test
    public void testSetAndGetDamage() {
        // Test setting and getting damage
        Integer testDamage = 1000;
        memberContribution.setBossBattle(testDamage);
        assertEquals(testDamage, memberContribution.getBossBattle());
    }

    @Test
    public void testSetAndGetDamage_Zero() {
        // Test setting zero damage
        Integer zeroDamage = 0;
        memberContribution.setBossBattle(zeroDamage);
        assertEquals(zeroDamage, memberContribution.getBossBattle());
    }

    @Test
    public void testSetAndGetDamage_Negative() {
        // Test setting negative damage
        Integer negativeDamage = -500;
        memberContribution.setBossBattle(negativeDamage);
        assertEquals(negativeDamage, memberContribution.getBossBattle());
    }

    @Test
    public void testSetAndGetDamage_Null() {
        // Test setting null damage
        memberContribution.setBossBattle(null);
        assertNull(memberContribution.getBossBattle());
    }

    @Test
    public void testAddBossBattle_FromZero() {
        // Test adding damage from initial zero value
        Integer additionalDamage = 500;
        memberContribution.addBossBattle(additionalDamage);
        assertEquals(Integer.valueOf(500), memberContribution.getBossBattle());
    }

    @Test
    public void testAddBossBattle_FromExistingValue() {
        // Test adding damage to existing value
        memberContribution.setBossBattle(1000);
        memberContribution.addBossBattle(500);
        assertEquals(Integer.valueOf(1500), memberContribution.getBossBattle());
    }

    @Test
    public void testAddBossBattle_Multiple() {
        // Test adding damage multiple times
        memberContribution.addBossBattle(100);
        memberContribution.addBossBattle(200);
        memberContribution.addBossBattle(300);
        assertEquals(Integer.valueOf(600), memberContribution.getBossBattle());
    }

    @Test
    public void testAddBossBattle_Zero() {
        // Test adding zero damage
        memberContribution.setBossBattle(500);
        memberContribution.addBossBattle(0);
        assertEquals(Integer.valueOf(500), memberContribution.getBossBattle());
    }

    @Test
    public void testAddBossBattle_Negative() {
        // Test adding negative damage (should decrease total)
        memberContribution.setBossBattle(1000);
        memberContribution.addBossBattle(-300);
        assertEquals(Integer.valueOf(700), memberContribution.getBossBattle());
    }

    @Test
    public void testAddBossBattle_Null() {
        // Test adding null damage (should throw NullPointerException)
        memberContribution.setBossBattle(500);
        assertThrows(NullPointerException.class, () -> {
            memberContribution.addBossBattle(null);
        });
        // Damage should remain unchanged after exception
        assertEquals(Integer.valueOf(500), memberContribution.getBossBattle());
    }

    @Test
    public void testAddDamage_WhenBossBattleIsNull() {
        // Test adding damage when current damage is null (should throw NullPointerException)
        memberContribution.setBossBattle(null);
        assertThrows(NullPointerException.class, () -> {
            memberContribution.addBossBattle(500);
        });
        // Should remain null after exception
        assertNull(memberContribution.getBossBattle());
    }

    @Test
    public void testCompleteWorkflow() {
        // Test a complete workflow
        String playerName = "TestPlayer";
        memberContribution.setName(playerName);
        
        // Add multiple damage values
        memberContribution.addBossBattle(1000);
        memberContribution.addBossBattle(2500);
        memberContribution.addBossBattle(750);
        
        // Verify final state
        assertEquals(playerName, memberContribution.getName());
        assertEquals(Integer.valueOf(4250), memberContribution.getBossBattle());
    }

    @Test
    public void testLargeDamageValues() {
        // Test with large damage values
        memberContribution.setBossBattle(Integer.MAX_VALUE - 1000);
        memberContribution.addBossBattle(500);
        assertEquals(Integer.valueOf(Integer.MAX_VALUE - 500), memberContribution.getBossBattle());
    }

    @Test
    public void testOverflowHandling() {
        // Test integer overflow behavior
        memberContribution.setBossBattle(Integer.MAX_VALUE);
        memberContribution.addBossBattle(1);
        // This will overflow to Integer.MIN_VALUE
        assertEquals(Integer.valueOf(Integer.MIN_VALUE), memberContribution.getBossBattle());
    }

    @Test
    public void testUnderflowHandling() {
        // Test integer underflow behavior
        memberContribution.setBossBattle(Integer.MIN_VALUE);
        memberContribution.addBossBattle(-1);
        // This will underflow to Integer.MAX_VALUE
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), memberContribution.getBossBattle());
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
        memberContribution.setBossBattle(100);
        
        memberContribution.addBossBattle(50);
        assertEquals(Integer.valueOf(150), memberContribution.getBossBattle());
        
        memberContribution.setName("UpdatedName");
        assertEquals("UpdatedName", memberContribution.getName());
        
        memberContribution.addBossBattle(25);
        assertEquals(Integer.valueOf(175), memberContribution.getBossBattle());
        
        memberContribution.setBossBattle(0);
        assertEquals(Integer.valueOf(0), memberContribution.getBossBattle());
        assertEquals("UpdatedName", memberContribution.getName());
    }

    @Test
    public void testBattleCountGetterSetter() {
        // Test battle count getter and setter
        Integer testCount = 5;
        memberContribution.setBattleCount(testCount);
        assertEquals(testCount, memberContribution.getBattleCount());
        
        // Test with zero
        memberContribution.setBattleCount(0);
        assertEquals(Integer.valueOf(0), memberContribution.getBattleCount());
        
        // Test with null
        memberContribution.setBattleCount(null);
        assertNull(memberContribution.getBattleCount());
    }

    @Test
    public void testBombCountGetterSetter() {
        // Test bomb count getter and setter
        Integer testCount = 3;
        memberContribution.setBombCount(testCount);
        assertEquals(testCount, memberContribution.getBombCount());
        
        // Test with zero
        memberContribution.setBombCount(0);
        assertEquals(Integer.valueOf(0), memberContribution.getBombCount());
        
        // Test with null
        memberContribution.setBombCount(null);
        assertNull(memberContribution.getBombCount());
    }

    @Test
    public void testIncrementBattleCount() {
        // Test incrementing battle count from default
        memberContribution.incrementBattleCount();
        assertEquals(Integer.valueOf(1), memberContribution.getBattleCount());
        
        // Test multiple increments
        memberContribution.incrementBattleCount();
        memberContribution.incrementBattleCount();
        assertEquals(Integer.valueOf(3), memberContribution.getBattleCount());
        
        // Test increment from set value
        memberContribution.setBattleCount(10);
        memberContribution.incrementBattleCount();
        assertEquals(Integer.valueOf(11), memberContribution.getBattleCount());
    }

    @Test
    public void testIncrementBombCount() {
        // Test incrementing bomb count from default
        memberContribution.incrementBombCount();
        assertEquals(Integer.valueOf(1), memberContribution.getBombCount());
        
        // Test multiple increments
        memberContribution.incrementBombCount();
        memberContribution.incrementBombCount();
        assertEquals(Integer.valueOf(3), memberContribution.getBombCount());
        
        // Test increment from set value
        memberContribution.setBombCount(7);
        memberContribution.incrementBombCount();
        assertEquals(Integer.valueOf(8), memberContribution.getBombCount());
    }

    @Test
    public void testIncrementCountsWhenNull() {
        // Test incrementing when counts are null (should throw NullPointerException)
        memberContribution.setBattleCount(null);
        assertThrows(NullPointerException.class, () -> {
            memberContribution.incrementBattleCount();
        });
        
        memberContribution.setBombCount(null);
        assertThrows(NullPointerException.class, () -> {
            memberContribution.incrementBombCount();
        });
    }

    @Test
    public void testSidebossFields() {
        // Test sideboss battle damage
        Integer sidebossBattleDamage = 1500;
        memberContribution.setSidebossBattle(sidebossBattleDamage);
        assertEquals(sidebossBattleDamage, memberContribution.getSidebossBattle());
        
        // Test sideboss bomb damage
        Integer sidebossBombDamage = 2000;
        memberContribution.setSidebossBomb(sidebossBombDamage);
        assertEquals(sidebossBombDamage, memberContribution.getSidebossBomb());
        
        // Test boss bomb damage
        Integer bossBombDamage = 1800;
        memberContribution.setBossBomb(bossBombDamage);
        assertEquals(bossBombDamage, memberContribution.getBossBomb());
    }

    @Test
    public void testAddSidebossBattle() {
        // Test adding sideboss battle damage
        memberContribution.addSidebossBattle(500);
        assertEquals(Integer.valueOf(500), memberContribution.getSidebossBattle());
        
        memberContribution.addSidebossBattle(300);
        assertEquals(Integer.valueOf(800), memberContribution.getSidebossBattle());
    }

    @Test
    public void testAddBossBomb() {
        // Test adding boss bomb damage
        memberContribution.addBossBomb(750);
        assertEquals(Integer.valueOf(750), memberContribution.getBossBomb());
        
        memberContribution.addBossBomb(250);
        assertEquals(Integer.valueOf(1000), memberContribution.getBossBomb());
    }

    @Test
    public void testAddSidebossBomb() {
        // Test adding sideboss bomb damage
        memberContribution.addSidebossBomb(400);
        assertEquals(Integer.valueOf(400), memberContribution.getSidebossBomb());
        
        memberContribution.addSidebossBomb(600);
        assertEquals(Integer.valueOf(1000), memberContribution.getSidebossBomb());
    }

    @Test
    public void testCompleteWorkflowWithCounters() {
        // Test complete workflow including counters
        String playerName = "CounterTestPlayer";
        String playerRole = "OFFICER";
        memberContribution.setName(playerName);
        memberContribution.setRole(playerRole);
        
        // Add different types of damage and track counters
        memberContribution.addBossBattle(1000);
        memberContribution.incrementBattleCount();
        
        memberContribution.addBossBomb(500);
        memberContribution.incrementBombCount();
        
        memberContribution.addSidebossBattle(750);
        memberContribution.incrementBattleCount();
        
        memberContribution.addSidebossBomb(300);
        memberContribution.incrementBombCount();
        
        // Verify final state
        assertEquals(playerName, memberContribution.getName());
        assertEquals(playerRole, memberContribution.getRole());
        assertEquals(Integer.valueOf(1000), memberContribution.getBossBattle());
        assertEquals(Integer.valueOf(500), memberContribution.getBossBomb());
        assertEquals(Integer.valueOf(750), memberContribution.getSidebossBattle());
        assertEquals(Integer.valueOf(300), memberContribution.getSidebossBomb());
        assertEquals(Integer.valueOf(2), memberContribution.getBattleCount());
        assertEquals(Integer.valueOf(2), memberContribution.getBombCount());
    }

    @Test
    public void testCounterOverflow() {
        // Test counter overflow behavior
        memberContribution.setBattleCount(Integer.MAX_VALUE);
        memberContribution.incrementBattleCount();
        assertEquals(Integer.valueOf(Integer.MIN_VALUE), memberContribution.getBattleCount());
        
        memberContribution.setBombCount(Integer.MAX_VALUE);
        memberContribution.incrementBombCount();
        assertEquals(Integer.valueOf(Integer.MIN_VALUE), memberContribution.getBombCount());
    }
}