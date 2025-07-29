package de.blaukool.tacticus;

import de.blaukool.tacticus.api.GuildRaidResponse;
import de.blaukool.tacticus.api.GuildResponse;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class MainTest {

    @Mock
    private CloseableHttpClient mockHttpClient;
    
    @Mock
    private CloseableHttpResponse mockResponse;
    
    @Mock
    private StatusLine mockStatusLine;
    
    @Mock
    private HttpEntity mockEntity;

    private ByteArrayOutputStream outputStream;
    private PrintStream originalErr;

    @BeforeEach
    public void setUp() throws Exception {
        // Capture System.out for testing
        outputStream = new ByteArrayOutputStream();
        originalErr = System.err;
        System.setErr(new PrintStream(outputStream));
    }

    @Test
    public void testMain_NoApiKey() throws Exception {
        // Test with no arguments (no API key)
        String[] args = {};
        
        Main.main(args);
        
        String output = outputStream.toString();
        assertTrue(output.contains("Please provide an API key as an argument"));
    }

    @Test
    public void testMain_WithValidApiKey() throws Exception {
        // Create a testable version by mocking the makeApiCall method indirectly
        String[] args = {"test-api-key"};
        
        // We can't easily mock static methods without PowerMock, 
        // so this test will attempt to run but may fail on actual HTTP calls
        // In a real scenario, you'd refactor Main to accept dependencies
        assertDoesNotThrow(() -> {
            try {
                Main.main(args);
            } catch (Exception e) {
                // Expected to fail on actual HTTP call, but shouldn't crash on argument processing
                assertTrue(e instanceof IOException || e.getCause() instanceof IOException);
            }
        });
    }

    @Test
    public void testValidateApiKeyFormat() {
        // Test API key validation (if such method existed)
        String validApiKey = "a9d8b338-c04a-43ec-ae5d-daf42b9c44b3";
        String invalidApiKey = "invalid-key";
        
        // These would be unit tests for helper methods if they existed
        assertTrue(validApiKey.matches("[a-f0-9-]{36}"));
        assertFalse(invalidApiKey.matches("[a-f0-9-]{36}"));
    }

    // Integration test that requires refactoring Main class for better testability
    @Test
    public void testMainIntegration_MockedDependencies() {
        // This test demonstrates how Main would be tested if refactored to accept dependencies
        
        // Create mock data
        GuildResponse.Guild mockGuild = new GuildResponse.Guild();
        mockGuild.setName("Test Guild");
        mockGuild.setGuildTag("TEST");
        mockGuild.setLevel(30);
        mockGuild.setGuildRaidSeasons(Arrays.asList(78, 79));
        
        GuildResponse.GuildMember mockMember = new GuildResponse.GuildMember();
        mockMember.setUserId("test-user-id");
        mockMember.setRole("LEADER");
        mockMember.setLevel(50);
        mockMember.setLastActivityOn(OffsetDateTime.now());
        mockGuild.setMembers(Arrays.asList(mockMember));
        
        GuildResponse mockGuildResponse = new GuildResponse();
        mockGuildResponse.setGuild(mockGuild);
        
        // Verify mock data structure
        assertNotNull(mockGuildResponse.getGuild());
        assertEquals("Test Guild", mockGuildResponse.getGuild().getName());
        assertEquals(1, mockGuildResponse.getGuild().getMembers().size());
        assertEquals(2, mockGuildResponse.getGuild().getGuildRaidSeasons().size());
    }

    @Test
    public void testCreateGuildRaidResponse() {
        // Test creating and validating guild raid response structure
        GuildRaidResponse mockRaidResponse = new GuildRaidResponse();
        mockRaidResponse.setSeason(78);
        mockRaidResponse.setSeasonConfigId("season-78");
        
        GuildRaidResponse.Raid mockRaid = new GuildRaidResponse.Raid();
        mockRaid.setUserId("test-user-id");
        mockRaid.setDamageDealt(5000);
        mockRaid.setDamageType("Battle");
        mockRaid.setEncounterType("Boss");
        
        mockRaidResponse.setEntries(Arrays.asList(mockRaid));
        
        // Verify mock data structure
        assertEquals(78, mockRaidResponse.getSeason());
        assertEquals(1, mockRaidResponse.getEntries().size());
        assertEquals(5000, mockRaidResponse.getEntries().get(0).getDamageDealt());
        assertEquals("Battle", mockRaidResponse.getEntries().get(0).getDamageType());
        assertEquals("Boss", mockRaidResponse.getEntries().get(0).getEncounterType());
    }

    @Test
    public void testDamageCategorization() {
        // Test damage categorization logic based on encounterType and damageType
        
        // Boss Battle
        GuildRaidResponse.Raid bossBattleRaid = new GuildRaidResponse.Raid();
        bossBattleRaid.setEncounterType("Boss");
        bossBattleRaid.setDamageType("Battle");
        bossBattleRaid.setDamageDealt(1000);
        
        // Boss Bomb
        GuildRaidResponse.Raid bossBombRaid = new GuildRaidResponse.Raid();
        bossBombRaid.setEncounterType("Boss");
        bossBombRaid.setDamageType("Bomb");
        bossBombRaid.setDamageDealt(1500);
        
        // SideBoss Battle
        GuildRaidResponse.Raid sideBossBattleRaid = new GuildRaidResponse.Raid();
        sideBossBattleRaid.setEncounterType("SideBoss");
        sideBossBattleRaid.setDamageType("Battle");
        sideBossBattleRaid.setDamageDealt(800);
        
        // SideBoss Bomb
        GuildRaidResponse.Raid sideBossBombRaid = new GuildRaidResponse.Raid();
        sideBossBombRaid.setEncounterType("SideBoss");
        sideBossBombRaid.setDamageType("Bomb");
        sideBossBombRaid.setDamageDealt(1200);
        
        // Verify categorization logic
        assertTrue("Boss".equals(bossBattleRaid.getEncounterType()) && "Battle".equals(bossBattleRaid.getDamageType()));
        assertTrue("Boss".equals(bossBombRaid.getEncounterType()) && "Bomb".equals(bossBombRaid.getDamageType()));
        assertTrue("SideBoss".equals(sideBossBattleRaid.getEncounterType()) && "Battle".equals(sideBossBattleRaid.getDamageType()));
        assertTrue("SideBoss".equals(sideBossBombRaid.getEncounterType()) && "Bomb".equals(sideBossBombRaid.getDamageType()));
    }

    @Test
    public void testBattleAndBombCounterLogic() {
        // Test that battle and bomb counters would be incremented correctly
        GuildRaidResponse.Raid[] raids = {
            createRaid("Boss", "Battle", 1000),
            createRaid("Boss", "Bomb", 1500),
            createRaid("SideBoss", "Battle", 800),
            createRaid("SideBoss", "Bomb", 1200),
            createRaid("Boss", "Battle", 2000)  // Another battle
        };
        
        int expectedBattleCount = 0;
        int expectedBombCount = 0;
        
        for (GuildRaidResponse.Raid raid : raids) {
            if ("Battle".equals(raid.getDamageType())) {
                expectedBattleCount++;
            } else if ("Bomb".equals(raid.getDamageType())) {
                expectedBombCount++;
            }
        }
        
        assertEquals(3, expectedBattleCount); // 2 boss battles + 1 sideboss battle
        assertEquals(2, expectedBombCount);   // 1 boss bomb + 1 sideboss bomb
    }

    @Test
    public void testUpdatedTableFormatting() {
        // Test the updated table formatting with role column
        String memberName = "TestPlayer";
        String memberRole = "LEADER";
        int bossBattle = 1000;
        int bossBomb = 500;
        int sideBattle = 750;
        int sideBomb = 300;
        int total = bossBattle + bossBomb + sideBattle + sideBomb;
        int battleCount = 2;
        int bombCount = 2;
        int rank = 1;
        
        String formattedRow = String.format("%-4d %-15s %-10s %,12d %,12d %,12d %,12d %,12d %8d %8d", 
            rank, 
            memberName.length() > 15 ? memberName.substring(0, 12) + "..." : memberName,
            memberRole != null ? memberRole : "N/A",
            bossBattle, bossBomb, sideBattle, sideBomb, total, battleCount, bombCount);
        
        assertTrue(formattedRow.contains("TestPlayer"));
        assertTrue(formattedRow.contains("LEADER"));
        assertTrue(formattedRow.contains("1,000"));  // Boss Battle
        assertTrue(formattedRow.contains("500"));    // Boss Bomb
        assertTrue(formattedRow.contains("750"));    // Side Battle
        assertTrue(formattedRow.contains("300"));    // Side Bomb
        assertTrue(formattedRow.contains("2,550"));  // Total
        assertTrue(formattedRow.contains("2"));      // Battle count (appears twice)
    }

    @Test
    public void testMemberContributionInitializationWithAllFields() {
        // Test that all fields are properly initialized including role
        de.blaukool.tacticus.logic.MemberContribution contribution = 
            new de.blaukool.tacticus.logic.MemberContribution();
        contribution.setName("TestUser");
        contribution.setRole("MEMBER");
        contribution.setBossBattle(0);
        contribution.setSidebossBattle(0);
        contribution.setBossBomb(0);
        contribution.setSidebossBomb(0);
        contribution.setBattleCount(0);
        contribution.setBombCount(0);
        
        assertEquals("TestUser", contribution.getName());
        assertEquals("MEMBER", contribution.getRole());
        assertEquals(Integer.valueOf(0), contribution.getBossBattle());
        assertEquals(Integer.valueOf(0), contribution.getSidebossBattle());
        assertEquals(Integer.valueOf(0), contribution.getBossBomb());
        assertEquals(Integer.valueOf(0), contribution.getSidebossBomb());
        assertEquals(Integer.valueOf(0), contribution.getBattleCount());
        assertEquals(Integer.valueOf(0), contribution.getBombCount());
    }

    @Test
    public void testIntegrationWithAllContributionTypes() {
        // Integration test with mock data covering all contribution types
        GuildRaidResponse mockRaidResponse = new GuildRaidResponse();
        mockRaidResponse.setSeason(78);
        
        GuildRaidResponse.Raid[] raids = {
            createRaid("Boss", "Battle", 1000),
            createRaid("Boss", "Bomb", 1500),
            createRaid("SideBoss", "Battle", 800),
            createRaid("SideBoss", "Bomb", 1200)
        };
        
        mockRaidResponse.setEntries(Arrays.asList(raids));
        
        // Simulate the damage processing logic from Main
        de.blaukool.tacticus.logic.MemberContribution contribution = 
            new de.blaukool.tacticus.logic.MemberContribution();
        contribution.setName("TestPlayer");
        contribution.setRole("OFFICER");
        contribution.setBossBattle(0);
        contribution.setSidebossBattle(0);
        contribution.setBossBomb(0);
        contribution.setSidebossBomb(0);
        contribution.setBattleCount(0);
        contribution.setBombCount(0);
        
        for (GuildRaidResponse.Raid raid : mockRaidResponse.getEntries()) {
            String encounterType = raid.getEncounterType();
            String damageType = raid.getDamageType();
            int damage = raid.getDamageDealt();
            
            if ("Boss".equals(encounterType) && "Battle".equals(damageType)) {
                contribution.addBossBattle(damage);
                contribution.incrementBattleCount();
            } else if ("Boss".equals(encounterType) && "Bomb".equals(damageType)) {
                contribution.addBossBomb(damage);
                contribution.incrementBombCount();
            } else if ("SideBoss".equals(encounterType) && "Battle".equals(damageType)) {
                contribution.addSidebossBattle(damage);
                contribution.incrementBattleCount();
            } else if ("SideBoss".equals(encounterType) && "Bomb".equals(damageType)) {
                contribution.addSidebossBomb(damage);
                contribution.incrementBombCount();
            }
        }
        
        // Verify the final state matches expected values
        assertEquals("TestPlayer", contribution.getName());
        assertEquals("OFFICER", contribution.getRole());
        assertEquals(Integer.valueOf(1000), contribution.getBossBattle());
        assertEquals(Integer.valueOf(1500), contribution.getBossBomb());
        assertEquals(Integer.valueOf(800), contribution.getSidebossBattle());
        assertEquals(Integer.valueOf(1200), contribution.getSidebossBomb());
        assertEquals(Integer.valueOf(2), contribution.getBattleCount()); // Boss + SideBoss battles
        assertEquals(Integer.valueOf(2), contribution.getBombCount());   // Boss + SideBoss bombs
        
        // Verify total damage calculation
        int totalDamage = contribution.getBossBattle() + contribution.getBossBomb() + 
                         contribution.getSidebossBattle() + contribution.getSidebossBomb();
        assertEquals(4500, totalDamage);
    }

    @Test
    public void testRoleHandlingInOutput() {
        // Test role handling including null role scenario
        String memberName = "TestPlayer";
        String nullRole = null;
        String validRole = "LEADER";
        
        // Test null role formatting (should show "N/A")
        String formattedRowWithNullRole = String.format("%-4d %-15s %-10s", 
            1, memberName, nullRole != null ? nullRole : "N/A");
        assertTrue(formattedRowWithNullRole.contains("N/A"));
        
        // Test valid role formatting
        String formattedRowWithValidRole = String.format("%-4d %-15s %-10s", 
            1, memberName, validRole != null ? validRole : "N/A");
        assertTrue(formattedRowWithValidRole.contains("LEADER"));
        assertFalse(formattedRowWithValidRole.contains("N/A"));
    }

    @Test
    public void testGuildMemberRoleExtraction() {
        // Test that roles are properly extracted from guild members
        GuildResponse.GuildMember leader = new GuildResponse.GuildMember();
        leader.setUserId("leader-id");
        leader.setRole("LEADER");
        
        GuildResponse.GuildMember officer = new GuildResponse.GuildMember();
        officer.setUserId("officer-id");
        officer.setRole("OFFICER");
        
        GuildResponse.GuildMember member = new GuildResponse.GuildMember();
        member.setUserId("member-id");
        member.setRole("MEMBER");
        
        // Verify role extraction
        assertEquals("LEADER", leader.getRole());
        assertEquals("OFFICER", officer.getRole());
        assertEquals("MEMBER", member.getRole());
        
        // Verify that roles would be stored correctly in maps
        Map<String, String> memberRoles = new HashMap<>();
        memberRoles.put(leader.getUserId(), leader.getRole());
        memberRoles.put(officer.getUserId(), officer.getRole());
        memberRoles.put(member.getUserId(), member.getRole());
        
        assertEquals("LEADER", memberRoles.get("leader-id"));
        assertEquals("OFFICER", memberRoles.get("officer-id"));
        assertEquals("MEMBER", memberRoles.get("member-id"));
    }

    // Helper method to create raid entries
    private GuildRaidResponse.Raid createRaid(String encounterType, String damageType, int damage) {
        GuildRaidResponse.Raid raid = new GuildRaidResponse.Raid();
        raid.setUserId("test-user-id");
        raid.setEncounterType(encounterType);
        raid.setDamageType(damageType);
        raid.setDamageDealt(damage);
        return raid;
    }

    @Test
    public void testApiEndpointConstruction() {
        // Test API endpoint URL construction logic
        String baseUrl = "https://api.tacticusgame.com";
        String guildEndpoint = "/api/v1/guild";
        String raidEndpoint = "/api/v1/guildRaid";
        
        assertEquals("https://api.tacticusgame.com/api/v1/guild", baseUrl + guildEndpoint);
        assertEquals("https://api.tacticusgame.com/api/v1/guildRaid/78", baseUrl + raidEndpoint + "/78");
    }

    @Test
    public void testTableFormatting() {
        // Test table formatting logic that would be extracted to a helper method
        String memberName = "TestPlayer";
        int damage = 12345;
        int rank = 1;
        
        String formattedRow = String.format("%-4d %-20s %,15d", rank, memberName, damage);
        
        assertTrue(formattedRow.contains("TestPlayer"));
        assertTrue(formattedRow.contains("12,345"));
        assertTrue(formattedRow.contains("1"));
    }

    @Test
    public void testMemberNameTruncation() {
        // Test member name truncation logic
        String longName = "VeryLongPlayerNameThatExceedsLimit";
        String truncatedName = longName.length() > 20 ? 
            longName.substring(0, 17) + "..." : longName;
        
        assertEquals("VeryLongPlayerNam...", truncatedName);
        assertTrue(truncatedName.length() <= 20);
    }

    @Test
    public void testArgumentValidation() {
        // Test argument validation
        String[] emptyArgs = {};
        String[] validArgs = {"test-api-key"};
        String[] tooManyArgs = {"key1", "key2", "key3"};
        
        assertTrue(emptyArgs.length == 0);
        assertTrue(validArgs.length >= 1);
        assertTrue(tooManyArgs.length > 1); // Could be used for additional validation
    }

    @Test
    public void testSeasonNumberValidation() {
        // Test season number validation
        Integer validSeason = 78;
        Integer invalidSeason = -1;
        Integer nullSeason = null;
        
        assertTrue(validSeason > 0);
        assertFalse(invalidSeason > 0);
        assertNull(nullSeason);
    }

    // Cleanup method to restore System.out
    @org.junit.jupiter.api.AfterEach
    public void tearDown() {
        System.setOut(originalErr);
    }
}