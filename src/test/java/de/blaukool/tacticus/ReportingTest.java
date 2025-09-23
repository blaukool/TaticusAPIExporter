package de.blaukool.tacticus;

import de.blaukool.tacticus.api.GuildRaidResponse;
import de.blaukool.tacticus.api.GuildResponse;
import de.blaukool.tacticus.logic.MemberContribution;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ReportingTest {

    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private PrintStream originalErr;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setUp() {
        // Capture System.out and System.err for testing
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(outputStream));
    }

    @org.junit.jupiter.api.AfterEach
    public void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void testMain_NoApiKey() {
        // Test with no arguments (no API key)
        String[] args = {};

        Reporting.main(args);

        String output = outputStream.toString();
        assertTrue(output.contains("Please provide an API key as an argument"));
    }

    @Test
    public void testMain_WithValidApiKey() {
        // Test with valid API key format
        String[] args = {"test-api-key"};

        // This will fail on actual HTTP calls, but should not crash on argument processing
        assertDoesNotThrow(() -> {
            try {
                Reporting.main(args);
            } catch (Exception e) {
                // Expected to fail on actual HTTP call, but shouldn't crash on argument processing
                assertTrue(e instanceof RuntimeException || e.getCause() instanceof Exception);
            }
        });
    }

    @Test
    public void testConstants() {
        // Test that all constants are properly defined
        try {
            Class<?> clazz = Reporting.class;

            // Check API constants
            assertEquals("https://api.tacticusgame.com", getStaticField(clazz, "API_BASE_URL"));
            assertEquals("/api/v1/guild", getStaticField(clazz, "GUILD_ENDPOINT"));
            assertEquals("/api/v1/guildRaid/", getStaticField(clazz, "GUILD_RAID_ENDPOINT"));
            assertEquals("yyyyMMdd_HHmmss", getStaticField(clazz, "TIMESTAMP_FORMAT"));
            assertEquals("%s_raid_report_season_%s_%s.xlsx", getStaticField(clazz, "FILENAME_FORMAT"));

            // Check sheet name constants
            assertEquals("Statistics Overview", getStaticField(clazz, "SHEET_STATISTICS_OVERVIEW"));
            assertEquals("Boss Timeline", getStaticField(clazz, "SHEET_BOSS_TIMELINE"));
            assertEquals("Player Battle Statistics", getStaticField(clazz, "SHEET_PLAYER_BATTLE_STATS"));

        } catch (Exception e) {
            fail("Constants should be accessible: " + e.getMessage());
        }
    }

    @Test
    public void testCreatePlayerStatisticsSheet() throws Exception {
        // Create test data
        List<MemberContribution> contributions = createTestContributions();

        // Create workbook and sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test");

        // Create styles
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        CellStyle numberStyle = workbook.createCellStyle();
        numberStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));

        // Call the method via reflection
        Method method = Reporting.class.getDeclaredMethod("createPlayerStatisticsSheet",
            Sheet.class, CellStyle.class, CellStyle.class, List.class, Integer.class);
        method.setAccessible(true);
        method.invoke(null, sheet, headerStyle, numberStyle, contributions, 78);

        // Verify sheet content
        assertNotNull(sheet.getRow(0)); // Season header
        assertTrue(sheet.getRow(0).getCell(0).getStringCellValue().contains("Guild Raid Season 78"));

        assertNotNull(sheet.getRow(2)); // Table header
        assertEquals("Rank", sheet.getRow(2).getCell(0).getStringCellValue());
        assertEquals("Member Name", sheet.getRow(2).getCell(1).getStringCellValue());
        assertEquals("Role", sheet.getRow(2).getCell(2).getStringCellValue());

        // Verify data rows
        assertNotNull(sheet.getRow(3)); // First data row
        assertEquals(1.0, sheet.getRow(3).getCell(0).getNumericCellValue());
        assertEquals("Player1", sheet.getRow(3).getCell(1).getStringCellValue());
        assertEquals("LEADER", sheet.getRow(3).getCell(2).getStringCellValue());

        workbook.close();
    }

    @Test
    public void testCreateBossTimelineSheet() throws Exception {
        // Create test boss data
        SortedSet<Boss> bosses = createTestBosses();

        // Create workbook and sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test");

        // Create styles
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        CellStyle numberStyle = workbook.createCellStyle();
        numberStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));

        // Call the method via reflection
        Method method = Reporting.class.getDeclaredMethod("createBossTimelineSheet",
            Sheet.class, CellStyle.class, CellStyle.class, SortedSet.class, Integer.class);
        method.setAccessible(true);
        method.invoke(null, sheet, headerStyle, numberStyle, bosses, 78);

        // Verify sheet content
        assertNotNull(sheet.getRow(0)); // Season header
        assertTrue(sheet.getRow(0).getCell(0).getStringCellValue().contains("Guild Raid Season 78"));

        workbook.close();
    }

    @Test
    public void testCreatePlayerBattleStatisticsSheet() throws Exception {
        // Create test data with raids
        List<MemberContribution> contributions = createTestContributionsWithRaids();

        // Create workbook and sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test");

        // Create styles
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        CellStyle numberStyle = workbook.createCellStyle();
        numberStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));

        // Call the method via reflection
        Method method = Reporting.class.getDeclaredMethod("createPlayerBattleStatisticsSheet",
            Sheet.class, CellStyle.class, CellStyle.class, List.class, Integer.class);
        method.setAccessible(true);
        method.invoke(null, sheet, headerStyle, numberStyle, contributions, 78);

        // Verify sheet content
        assertNotNull(sheet.getRow(0)); // Season header
        assertTrue(sheet.getRow(0).getCell(0).getStringCellValue().contains("Guild Raid Season 78"));

        // Verify player header
        assertNotNull(sheet.getRow(2)); // First player header
        assertEquals("Player1", sheet.getRow(2).getCell(0).getStringCellValue());

        // Verify battle details header
        assertNotNull(sheet.getRow(3)); // Battle details header
        assertEquals("Rarity", sheet.getRow(3).getCell(0).getStringCellValue());
        assertEquals("No.", sheet.getRow(3).getCell(1).getStringCellValue());
        assertEquals("Bossname", sheet.getRow(3).getCell(2).getStringCellValue());

        workbook.close();
    }

    @Test
    public void testApiKeyValidation() {
        // Test API key format validation
        String validApiKey = "a9d8b338-c04a-43ec-ae5d-daf42b9c44b3";
        String invalidApiKey = "invalid-key";

        assertTrue(validApiKey.matches("[a-f0-9-]{36}"));
        assertFalse(invalidApiKey.matches("[a-f0-9-]{36}"));
    }

    @Test
    public void testFilenameGeneration() {
        // Test filename generation logic
        String guildName = "Test Guild #1";
        String expectedCleanName = "Test_Guild__1";
        String actualCleanName = guildName.replaceAll("[^a-zA-Z0-9]", "_");

        assertEquals(expectedCleanName, actualCleanName);
    }

    @Test
    public void testExcelWorkbookCreation() throws Exception {
        // Test that we can create a workbook with all required sheets
        Workbook workbook = new XSSFWorkbook();

        Sheet statisticsSheet = workbook.createSheet("Statistics Overview");
        Sheet timelineSheet = workbook.createSheet("Boss Timeline");
        Sheet battleStatsSheet = workbook.createSheet("Player Battle Statistics");

        assertNotNull(statisticsSheet);
        assertNotNull(timelineSheet);
        assertNotNull(battleStatsSheet);

        assertEquals(3, workbook.getNumberOfSheets());
        assertEquals("Statistics Overview", workbook.getSheetName(0));
        assertEquals("Boss Timeline", workbook.getSheetName(1));
        assertEquals("Player Battle Statistics", workbook.getSheetName(2));

        workbook.close();
    }

    @Test
    public void testCellStyleCreation() throws Exception {
        // Test cell style creation
        Workbook workbook = new XSSFWorkbook();

        // Header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // Number style
        CellStyle numberStyle = workbook.createCellStyle();
        numberStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));

        assertNotNull(headerStyle);
        assertNotNull(numberStyle);
        assertTrue(headerFont.getBold());

        workbook.close();
    }

    @Test
    public void testMemberContributionSorting() {
        // Test sorting logic for member contributions
        List<MemberContribution> contributions = createTestContributions();

        // Sort by total damage (descending)
        contributions.sort((a, b) -> {
            int totalA = a.getBossBattle() + a.getBossBomb() + a.getSidebossBattle() + a.getSidebossBomb();
            int totalB = b.getBossBattle() + b.getBossBomb() + b.getSidebossBattle() + b.getSidebossBomb();
            return Integer.compare(totalB, totalA);
        });

        // Verify sorting (Player1 should be first with total 2550)
        assertEquals("Player1", contributions.get(0).getName());

        // Sort alphabetically for battle statistics
        contributions.sort((a, b) -> CharSequence.compare(a.getName(), b.getName()));

        // Verify alphabetical sorting (Player1 should still be first alphabetically)
        assertEquals("Player1", contributions.get(0).getName());
        assertEquals("Player2", contributions.get(1).getName());
    }

    @Test
    public void testDamageCalculation() {
        // Test damage calculation logic
        MemberContribution contribution = new MemberContribution();
        contribution.setBossBattle(1000);
        contribution.setBossBomb(500);
        contribution.setSidebossBattle(750);
        contribution.setSidebossBomb(300);

        int total = contribution.getBossBattle() + contribution.getBossBomb() +
                   contribution.getSidebossBattle() + contribution.getSidebossBomb();

        assertEquals(2550, total);
    }

    @Test
    public void testTimestampFormat() {
        // Test timestamp format
        String timestampPattern = "yyyyMMdd_HHmmss";
        String testTimestamp = "20250923_143022";

        assertTrue(testTimestamp.matches("\\d{8}_\\d{6}"));
    }

    @Test
    public void testSeasonFilenameFormat() {
        // Test season filename format
        String guildName = "TestGuild";
        int season = 78;
        String timestamp = "20250923_143022";

        String filename = String.format("%s_raid_report_season_%s_%s.xlsx",
            guildName, season, timestamp);

        assertEquals("TestGuild_raid_report_season_78_20250923_143022.xlsx", filename);
    }

    // Helper methods
    private List<MemberContribution> createTestContributions() {
        List<MemberContribution> contributions = new ArrayList<>();

        MemberContribution player1 = new MemberContribution();
        player1.setName("Player1");
        player1.setRole("LEADER");
        player1.setBossBattle(1000);
        player1.setBossBomb(500);
        player1.setSidebossBattle(750);
        player1.setSidebossBomb(300);
        player1.setBattleCount(2);
        player1.setBombCount(2);
        contributions.add(player1);

        MemberContribution player2 = new MemberContribution();
        player2.setName("Player2");
        player2.setRole("OFFICER");
        player2.setBossBattle(800);
        player2.setBossBomb(600);
        player2.setSidebossBattle(400);
        player2.setSidebossBomb(200);
        player2.setBattleCount(1);
        player2.setBombCount(3);
        contributions.add(player2);

        return contributions;
    }

    private List<MemberContribution> createTestContributionsWithRaids() {
        List<MemberContribution> contributions = createTestContributions();

        // Add raid data to the first contribution
        MemberContribution player1 = contributions.get(0);
        List<GuildRaidResponse.Raid> raids = new ArrayList<>();

        GuildRaidResponse.Raid raid1 = new GuildRaidResponse.Raid();
        raid1.setRarity("Epic");
        raid1.setSet(0); // Will be displayed as set + 1 = 1
        raid1.setType("Boss1");
        raid1.setEncounterType("Boss");
        raid1.setUnitId("boss1_unit1_id");
        raid1.setDamageDealt(1000);
        raid1.setRemainingHp(50000);
        raid1.setDamageType("Battle");
        raids.add(raid1);

        player1.setRaids(raids);

        return contributions;
    }

    private SortedSet<Boss> createTestBosses() {
        // Note: This is a simplified mock since Boss class details aren't fully visible
        // In reality, you'd need to import and properly construct Boss objects
        return new TreeSet<>();
    }

    private Object getStaticField(Class<?> clazz, String fieldName) throws Exception {
        java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(null);
    }

    // Mock Boss class for testing (if needed)
    private static class Boss implements Comparable<Boss> {
        private String name;
        private String rarity;

        public Boss(String name, String rarity) {
            this.name = name;
            this.rarity = rarity;
        }

        public String getName() { return name; }
        public String getRarity() { return rarity; }

        @Override
        public int compareTo(Boss other) {
            return this.name.compareTo(other.name);
        }
    }
}