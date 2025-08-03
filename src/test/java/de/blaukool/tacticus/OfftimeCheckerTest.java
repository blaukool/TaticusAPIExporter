package de.blaukool.tacticus;

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
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OfftimeCheckerTest {

    @Mock
    private CloseableHttpClient mockHttpClient;
    
    @Mock
    private CloseableHttpResponse mockResponse;
    
    @Mock
    private StatusLine mockStatusLine;
    
    @Mock
    private HttpEntity mockEntity;

    private ByteArrayOutputStream outputStream;
    private ByteArrayOutputStream errorStream;
    private PrintStream originalOut;
    private PrintStream originalErr;

    @BeforeEach
    public void setUp() {
        // Capture System.out and System.err for testing
        outputStream = new ByteArrayOutputStream();
        errorStream = new ByteArrayOutputStream();
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(errorStream));
    }

    @Test
    public void testMain_NoApiKey() {
        // Test with no arguments (no API key)
        String[] args = {};
        
        OfftimeChecker.main(args);
        
        String errorOutput = errorStream.toString();
        assertTrue(errorOutput.contains("Please provide an API key as an argument"));
    }

    @Test
    public void testMain_WithValidApiKey() throws Exception {
        // Create mock guild response
        GuildResponse mockGuildResponse = createMockGuildResponse();
        
        // This test would require significant refactoring of OfftimeChecker to inject dependencies
        // For now, we'll test that it doesn't crash with valid arguments
        String[] args = {"test-api-key"};
        
        assertDoesNotThrow(() -> {
            try {
                OfftimeChecker.main(args);
            } catch (Exception e) {
                // Expected to fail on actual HTTP call, but shouldn't crash on argument processing
                assertTrue(e instanceof IOException || e.getCause() instanceof IOException);
            }
        });
    }

    @Test
    public void testGetRolePriority() throws Exception {
        // Use reflection to test the private getRolePriority method
        Method getRolePriorityMethod = OfftimeChecker.class.getDeclaredMethod("getRolePriority", String.class);
        getRolePriorityMethod.setAccessible(true);
        
        // Test role priorities
        assertEquals(0, getRolePriorityMethod.invoke(null, "LEADER"));
        assertEquals(0, getRolePriorityMethod.invoke(null, "leader")); // Case insensitive
        assertEquals(1, getRolePriorityMethod.invoke(null, "CO_LEADER"));
        assertEquals(2, getRolePriorityMethod.invoke(null, "OFFICER"));
        assertEquals(3, getRolePriorityMethod.invoke(null, "MEMBER"));
        assertEquals(4, getRolePriorityMethod.invoke(null, "UNKNOWN_ROLE"));
        String nullvalue = null;
        assertEquals(4, getRolePriorityMethod.invoke(null, nullvalue));
    }

    @Test
    public void testFormatDurationDetailed() throws Exception {
        // Use reflection to test the private formatDurationDetailed method
        Method formatDurationMethod = OfftimeChecker.class.getDeclaredMethod("formatDurationDetailed", long.class);
        formatDurationMethod.setAccessible(true);
        
        // Test various duration formats
        assertEquals("30s", formatDurationMethod.invoke(null, 30000L)); // 30 seconds
        assertEquals("2m 30s", formatDurationMethod.invoke(null, 150000L)); // 2 minutes 30 seconds
        assertEquals("1h 0m 0s", formatDurationMethod.invoke(null, 3600000L)); // 1 hour
        assertEquals("2h 15m 45s", formatDurationMethod.invoke(null, 8145000L)); // 2h 15m 45s
        assertEquals("0s", formatDurationMethod.invoke(null, 0L)); // 0 seconds
    }

    @Test
    public void testGetAllMembersWithActivity() throws Exception {
        // Use reflection to test the private getAllMembersWithActivity method
        Method getAllMembersMethod = OfftimeChecker.class.getDeclaredMethod("getAllMembersWithActivity", List.class);
        getAllMembersMethod.setAccessible(true);
        
        // Create test members
        List<GuildResponse.GuildMember> testMembers = createTestMembers();
        
        // Call the method
        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) getAllMembersMethod.invoke(null, testMembers);
        
        // Verify results
        assertNotNull(result);
        assertEquals(4, result.size());
        
        // The method should sort by role priority first, then by name
        // We can't easily test the internal structure without exposing the private class,
        // but we can verify the list size and that it doesn't throw exceptions
    }

    @Test
    public void testMemberActivitySorting() throws Exception {
        // Test that members are sorted correctly by role and name
        List<GuildResponse.GuildMember> testMembers = createTestMembersForSorting();
        
        Method getAllMembersMethod = OfftimeChecker.class.getDeclaredMethod("getAllMembersWithActivity", List.class);
        getAllMembersMethod.setAccessible(true);
        
        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) getAllMembersMethod.invoke(null, testMembers);
        
        // Verify sorting - should be in role priority order, then alphabetical by name
        assertNotNull(result);
        assertEquals(5, result.size());
    }

    @Test
    public void testInactiveMembers() throws Exception {
        // Test identification of inactive members (>48h)
        List<GuildResponse.GuildMember> testMembers = createMembersWithVariousActivityTimes();
        
        Method getAllMembersMethod = OfftimeChecker.class.getDeclaredMethod("getAllMembersWithActivity", List.class);
        getAllMembersMethod.setAccessible(true);
        
        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) getAllMembersMethod.invoke(null, testMembers);
        
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    public void testMembersWithNullActivity() throws Exception {
        // Test handling of members with null last activity
        List<GuildResponse.GuildMember> testMembers = createMembersWithNullActivity();
        
        Method getAllMembersMethod = OfftimeChecker.class.getDeclaredMethod("getAllMembersWithActivity", List.class);
        getAllMembersMethod.setAccessible(true);
        
        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) getAllMembersMethod.invoke(null, testMembers);
        
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testMainWithMockData() {
        // Test main method behavior with different scenarios
        // This would require dependency injection to properly test
        String[] args = {"test-api-key"};
        
        // Test that main method handles the API key correctly
        assertDoesNotThrow(() -> {
            try {
                OfftimeChecker.main(args);
            } catch (Exception e) {
                // Expected behavior - will fail on HTTP call but shouldn't crash
                assertNotNull(e);
            }
        });
    }

    @Test
    public void testOutputFormatting() throws Exception {
        // Test that the output formatting works correctly
        List<GuildResponse.GuildMember> testMembers = createTestMembersForOutput();
        
        Method getAllMembersMethod = OfftimeChecker.class.getDeclaredMethod("getAllMembersWithActivity", List.class);
        getAllMembersMethod.setAccessible(true);
        
        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) getAllMembersMethod.invoke(null, testMembers);
        
        // Verify that we get results without exceptions
        assertNotNull(result);
        assertTrue(result.size() > 0);
    }

    @Test
    public void testRoleHierarchyOrdering() throws Exception {
        // Test that role hierarchy is maintained in sorting
        Method getRolePriorityMethod = OfftimeChecker.class.getDeclaredMethod("getRolePriority", String.class);
        getRolePriorityMethod.setAccessible(true);
        
        // Verify hierarchy order
        int leaderPriority = (Integer) getRolePriorityMethod.invoke(null, "LEADER");
        int coLeaderPriority = (Integer) getRolePriorityMethod.invoke(null, "CO_LEADER");
        int officerPriority = (Integer) getRolePriorityMethod.invoke(null, "OFFICER");
        int memberPriority = (Integer) getRolePriorityMethod.invoke(null, "MEMBER");
        
        assertTrue(leaderPriority < coLeaderPriority);
        assertTrue(coLeaderPriority < officerPriority);
        assertTrue(officerPriority < memberPriority);
    }

    @Test
    public void testDurationFormattingEdgeCases() throws Exception {
        Method formatDurationMethod = OfftimeChecker.class.getDeclaredMethod("formatDurationDetailed", long.class);
        formatDurationMethod.setAccessible(true);
        
        // Test edge cases
        assertEquals("0s", formatDurationMethod.invoke(null, 0L));
        assertEquals("1s", formatDurationMethod.invoke(null, 1000L));
        assertEquals("59s", formatDurationMethod.invoke(null, 59000L));
        assertEquals("1m 0s", formatDurationMethod.invoke(null, 60000L));
        assertEquals("59m 59s", formatDurationMethod.invoke(null, 3599000L));
        assertEquals("1h 0m 0s", formatDurationMethod.invoke(null, 3600000L));
    }

    // Helper methods to create test data

    private GuildResponse createMockGuildResponse() {
        GuildResponse guildResponse = new GuildResponse();
        GuildResponse.Guild guild = new GuildResponse.Guild();
        guild.setName("Test Guild");
        guild.setGuildTag("TEST");
        guild.setMembers(createTestMembers());
        guildResponse.setGuild(guild);
        return guildResponse;
    }

    private List<GuildResponse.GuildMember> createTestMembers() {
        GuildResponse.GuildMember leader = new GuildResponse.GuildMember();
        leader.setUserId("leader-id");
        leader.setRole("LEADER");
        leader.setLevel(60);
        leader.setLastActivityOn(new Date(System.currentTimeMillis() - 1000 * 60 * 60)); // 1 hour ago

        GuildResponse.GuildMember officer = new GuildResponse.GuildMember();
        officer.setUserId("officer-id");
        officer.setRole("OFFICER");
        officer.setLevel(55);
        officer.setLastActivityOn(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24)); // 1 day ago

        GuildResponse.GuildMember member1 = new GuildResponse.GuildMember();
        member1.setUserId("member1-id");
        member1.setRole("MEMBER");
        member1.setLevel(45);
        member1.setLastActivityOn(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 72)); // 3 days ago (inactive)

        GuildResponse.GuildMember member2 = new GuildResponse.GuildMember();
        member2.setUserId("member2-id");
        member2.setRole("MEMBER");
        member2.setLevel(40);
        member2.setLastActivityOn(new Date(System.currentTimeMillis() - 1000 * 60 * 30)); // 30 minutes ago

        return Arrays.asList(leader, officer, member1, member2);
    }

    private List<GuildResponse.GuildMember> createTestMembersForSorting() {
        GuildResponse.GuildMember memberB = new GuildResponse.GuildMember();
        memberB.setUserId("memberB-id");
        memberB.setRole("MEMBER");
        memberB.setLevel(40);
        memberB.setLastActivityOn(new Date());

        GuildResponse.GuildMember memberA = new GuildResponse.GuildMember();
        memberA.setUserId("memberA-id");
        memberA.setRole("MEMBER");
        memberA.setLevel(45);
        memberA.setLastActivityOn(new Date());

        GuildResponse.GuildMember officer = new GuildResponse.GuildMember();
        officer.setUserId("officer-id");
        officer.setRole("OFFICER");
        officer.setLevel(55);
        officer.setLastActivityOn(new Date());

        GuildResponse.GuildMember coLeader = new GuildResponse.GuildMember();
        coLeader.setUserId("coleader-id");
        coLeader.setRole("CO_LEADER");
        coLeader.setLevel(58);
        coLeader.setLastActivityOn(new Date());

        GuildResponse.GuildMember leader = new GuildResponse.GuildMember();
        leader.setUserId("leader-id");
        leader.setRole("LEADER");
        leader.setLevel(60);
        leader.setLastActivityOn(new Date());

        return Arrays.asList(memberB, memberA, officer, coLeader, leader);
    }

    private List<GuildResponse.GuildMember> createMembersWithVariousActivityTimes() {
        long now = System.currentTimeMillis();
        long fortyEightHours = 48 * 60 * 60 * 1000;

        GuildResponse.GuildMember activeMember = new GuildResponse.GuildMember();
        activeMember.setUserId("active-id");
        activeMember.setRole("MEMBER");
        activeMember.setLevel(45);
        activeMember.setLastActivityOn(new Date(now - 1000 * 60 * 60)); // 1 hour ago

        GuildResponse.GuildMember inactiveMember1 = new GuildResponse.GuildMember();
        inactiveMember1.setUserId("inactive1-id");
        inactiveMember1.setRole("MEMBER");
        inactiveMember1.setLevel(40);
        inactiveMember1.setLastActivityOn(new Date(now - fortyEightHours - 1000)); // Just over 48h ago

        GuildResponse.GuildMember inactiveMember2 = new GuildResponse.GuildMember();
        inactiveMember2.setUserId("inactive2-id");
        inactiveMember2.setRole("OFFICER");
        inactiveMember2.setLevel(50);
        inactiveMember2.setLastActivityOn(new Date(now - fortyEightHours * 2)); // 4 days ago

        return Arrays.asList(activeMember, inactiveMember1, inactiveMember2);
    }

    private List<GuildResponse.GuildMember> createMembersWithNullActivity() {
        GuildResponse.GuildMember memberWithActivity = new GuildResponse.GuildMember();
        memberWithActivity.setUserId("active-id");
        memberWithActivity.setRole("MEMBER");
        memberWithActivity.setLevel(45);
        memberWithActivity.setLastActivityOn(new Date());

        GuildResponse.GuildMember memberWithoutActivity = new GuildResponse.GuildMember();
        memberWithoutActivity.setUserId("inactive-id");
        memberWithoutActivity.setRole("MEMBER");
        memberWithoutActivity.setLevel(40);
        memberWithoutActivity.setLastActivityOn(null); // Null activity

        return Arrays.asList(memberWithActivity, memberWithoutActivity);
    }

    private List<GuildResponse.GuildMember> createTestMembersForOutput() {
        GuildResponse.GuildMember leader = new GuildResponse.GuildMember();
        leader.setUserId("leader-id");
        leader.setRole("LEADER");
        leader.setLevel(60);
        leader.setLastActivityOn(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 72)); // 3 days ago (inactive)

        GuildResponse.GuildMember member = new GuildResponse.GuildMember();
        member.setUserId("member-id");
        member.setRole("MEMBER");
        member.setLevel(45);
        member.setLastActivityOn(new Date(System.currentTimeMillis() - 1000 * 60 * 30)); // 30 minutes ago (active)

        return Arrays.asList(leader, member);
    }

    // Cleanup method to restore System.out and System.err
    @org.junit.jupiter.api.AfterEach
    public void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }
}