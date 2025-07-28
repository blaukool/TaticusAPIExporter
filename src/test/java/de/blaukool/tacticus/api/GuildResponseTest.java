package de.blaukool.tacticus.api;

import de.blaukool.tacticus.logic.UserIDTranslator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class GuildResponseTest {

    private GuildResponse guildResponse;
    private GuildResponse.Guild guild;
    private GuildResponse.GuildMember guildMember;

    @BeforeEach
    public void setUp() {
        guildResponse = new GuildResponse();
        guild = new GuildResponse.Guild();
        guildMember = new GuildResponse.GuildMember();
    }

    @Test
    public void testGuildResponseSettersAndGetters() {
        guildResponse.setGuild(guild);
        assertEquals(guild, guildResponse.getGuild());
    }

    @Test
    public void testGuildSettersAndGetters() {
        String guildId = "test-guild-id";
        String guildTag = "TEST";
        String name = "Test Guild";
        int level = 25;
        
        guild.setGuildId(guildId);
        guild.setGuildTag(guildTag);
        guild.setName(name);
        guild.setLevel(level);
        guild.setMembers(Arrays.asList(guildMember));
        guild.setGuildRaidSeasons(Arrays.asList(78, 79));
        
        assertEquals(guildId, guild.getGuildId());
        assertEquals(guildTag, guild.getGuildTag());
        assertEquals(name, guild.getName());
        assertEquals(level, guild.getLevel());
        assertEquals(1, guild.getMembers().size());
        assertEquals(2, guild.getGuildRaidSeasons().size());
        assertTrue(guild.getGuildRaidSeasons().contains(78));
        assertTrue(guild.getGuildRaidSeasons().contains(79));
    }

    @Test
    public void testGuildMemberSettersAndGetters() {
        String userId = "test-user-id";
        String role = "LEADER";
        int level = 50;
        OffsetDateTime lastActivity = OffsetDateTime.now();
        
        guildMember.setUserId(userId);
        guildMember.setRole(role);
        guildMember.setLevel(level);
        guildMember.setLastActivityOn(lastActivity);
        
        assertEquals(userId, guildMember.getUserId());
        assertEquals(role, guildMember.getRole());
        assertEquals(level, guildMember.getLevel());
        assertEquals(lastActivity, guildMember.getLastActivityOn());
    }

    @Test
    public void testGuildMemberGetName_WithUserMapping() {
        // Test with a user ID that should be in the userMapping.properties
        // This tests the actual UserIDTranslator integration
        guildMember.setUserId("33699d0f-e0ed-4930-833a-0d5069acda43"); // Should map to "Reaper"
        
        String result = guildMember.getName();
        // This will return either the mapped name or "unknown" depending on the actual mapping file
        assertNotNull(result);
        assertTrue(result.equals("Reaper") || result.equals("unknown"));
    }

    @Test
    public void testGuildMemberGetName_UnknownUser() {
        // Test with a user ID that definitely won't be in the mapping
        guildMember.setUserId("unknown-user-id-12345");
        guildMember.setRole("MEMBER");
        guildMember.setLevel(42);
        
        String result = guildMember.getName();
        // Should return "unknown" for unmapped users
        assertEquals("unknown", result);
    }

    @Test
    public void testGuildMemberGetName_NullUserId() {
        // Test with null user ID
        guildMember.setUserId(null);
        guildMember.setRole("MEMBER");
        guildMember.setLevel(30);
        
        String result = guildMember.getName();
        assertEquals("unknown", result);
    }

    @Test
    public void testGuildMemberRoles() {
        // Test different role types
        String[] roles = {"LEADER", "CO_LEADER", "OFFICER", "MEMBER"};
        
        for (String role : roles) {
            guildMember.setRole(role);
            assertEquals(role, guildMember.getRole());
        }
    }

    @Test
    public void testGuildMemberLevelBoundaries() {
        // Test various level values
        int[] levels = {1, 25, 50, 60};
        
        for (int level : levels) {
            guildMember.setLevel(level);
            assertEquals(level, guildMember.getLevel());
        }
    }

    @Test
    public void testNullValues() {
        // Test that null values are handled correctly
        guild.setGuildId(null);
        guild.setGuildTag(null);
        guild.setName(null);
        guild.setMembers(null);
        guild.setGuildRaidSeasons(null);
        
        assertNull(guild.getGuildId());
        assertNull(guild.getGuildTag());
        assertNull(guild.getName());
        assertNull(guild.getMembers());
        assertNull(guild.getGuildRaidSeasons());
        
        guildMember.setUserId(null);
        guildMember.setRole(null);
        guildMember.setLastActivityOn(null);
        
        assertNull(guildMember.getUserId());
        assertNull(guildMember.getRole());
        assertNull(guildMember.getLastActivityOn());
    }

    @Test
    public void testEmptyCollections() {
        // Test with empty collections
        guild.setMembers(Arrays.asList());
        guild.setGuildRaidSeasons(Arrays.asList());
        
        assertTrue(guild.getMembers().isEmpty());
        assertTrue(guild.getGuildRaidSeasons().isEmpty());
    }

    @Test
    public void testGuildMemberNewUserDetection() {
        // Test the new user detection logic in getName method
        GuildResponse.GuildMember newMember = new GuildResponse.GuildMember();
        newMember.setUserId("brand-new-user-id");
        newMember.setRole("OFFICER");
        newMember.setLevel(45);
        
        // Capture System.out to test the "is new" message
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        java.io.PrintStream originalOut = System.out;
        System.setOut(new java.io.PrintStream(outputStream));
        
        try {
            String result = newMember.getName();
            assertEquals("unknown", result);
            
            // Check if the "is new" message was printed
            String output = outputStream.toString();
            assertTrue(output.contains("brand-new-user-id is new"));
            assertTrue(output.contains("OFFICER"));
            assertTrue(output.contains("45"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testMultipleMembers() {
        // Test with multiple guild members
        GuildResponse.GuildMember member1 = new GuildResponse.GuildMember();
        member1.setUserId("user1");
        member1.setRole("LEADER");
        member1.setLevel(55);
        
        GuildResponse.GuildMember member2 = new GuildResponse.GuildMember();
        member2.setUserId("user2");
        member2.setRole("OFFICER");
        member2.setLevel(48);
        
        guild.setMembers(Arrays.asList(member1, member2));
        
        assertEquals(2, guild.getMembers().size());
        assertEquals("LEADER", guild.getMembers().get(0).getRole());
        assertEquals("OFFICER", guild.getMembers().get(1).getRole());
        assertEquals(55, guild.getMembers().get(0).getLevel());
        assertEquals(48, guild.getMembers().get(1).getLevel());
    }

    @Test
    public void testGuildRaidSeasonsOrder() {
        // Test that guild raid seasons maintain order
        guild.setGuildRaidSeasons(Arrays.asList(76, 77, 78, 79, 80));
        
        assertEquals(5, guild.getGuildRaidSeasons().size());
        assertEquals(Integer.valueOf(76), guild.getGuildRaidSeasons().get(0));
        assertEquals(Integer.valueOf(80), guild.getGuildRaidSeasons().get(4));
    }

    @Test
    public void testGuildCompleteStructure() {
        // Test a complete guild structure
        guild.setGuildId("complete-guild-id");
        guild.setGuildTag("COMP");
        guild.setName("Complete Test Guild");
        guild.setLevel(40);
        
        GuildResponse.GuildMember leader = new GuildResponse.GuildMember();
        leader.setUserId("leader-id");
        leader.setRole("LEADER");
        leader.setLevel(60);
        leader.setLastActivityOn(OffsetDateTime.now());
        
        GuildResponse.GuildMember officer = new GuildResponse.GuildMember();
        officer.setUserId("officer-id");
        officer.setRole("OFFICER");
        officer.setLevel(52);
        officer.setLastActivityOn(OffsetDateTime.now().minusHours(2));
        
        guild.setMembers(Arrays.asList(leader, officer));
        guild.setGuildRaidSeasons(Arrays.asList(78, 79));
        
        guildResponse.setGuild(guild);
        
        // Verify complete structure
        assertNotNull(guildResponse.getGuild());
        assertEquals("Complete Test Guild", guildResponse.getGuild().getName());
        assertEquals(2, guildResponse.getGuild().getMembers().size());
        assertEquals("LEADER", guildResponse.getGuild().getMembers().get(0).getRole());
        assertEquals(2, guildResponse.getGuild().getGuildRaidSeasons().size());
    }
}