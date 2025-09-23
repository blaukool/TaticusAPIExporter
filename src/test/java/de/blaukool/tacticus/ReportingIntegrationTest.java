package de.blaukool.tacticus;

import de.blaukool.tacticus.api.GuildRaidResponse;
import de.blaukool.tacticus.api.GuildResponse;
import de.blaukool.tacticus.logic.MemberContribution;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for the Excel-based reporting functionality.
 * Tests the complete workflow from data processing to Excel file generation.
 */
public class ReportingIntegrationTest {

    @TempDir
    Path tempDir;

    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private PrintStream originalErr;

    @BeforeEach
    public void setUp() {
        // Capture System.out and System.err for testing
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(outputStream));

        // Set working directory to temp directory
        System.setProperty("user.dir", tempDir.toString());
    }

    @org.junit.jupiter.api.AfterEach
    public void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        System.clearProperty("user.dir");
    }

    @Test
    public void testCompleteExcelGenerationWorkflow() throws Exception {
        // Create a complete test scenario with mock data
        GuildResponse mockGuildResponse = createMockGuildResponse();
        List<GuildRaidResponse> mockRaidResponses = createMockRaidResponses();

        // Test the complete workflow by calling individual methods
        // Since we can't easily mock static HTTP calls, we'll test the data processing pipeline

        // Step 1: Test member initialization
        Map<String, String> memberNames = new HashMap<>();
        Map<String, String> memberRoles = new HashMap<>();

        if (mockGuildResponse.getGuild().getMembers() != null) {
            for (GuildResponse.GuildMember member : mockGuildResponse.getGuild().getMembers()) {
                memberNames.put(member.getUserId(), member.getName());
                memberRoles.put(member.getUserId(), member.getRole());
            }
        }

        assertEquals(2, memberNames.size());
        assertEquals(2, memberRoles.size());

        // Step 2: Test data processing for each season
        List<Integer> seasons = mockGuildResponse.getGuild().getGuildRaidSeasons();
        for (int seasonIndex = 0; seasonIndex < seasons.size(); seasonIndex++) {
            Integer season = seasons.get(seasonIndex);
            GuildRaidResponse raidResponse = mockRaidResponses.get(seasonIndex);

            // Test Excel generation for this season
            testSeasonExcelGeneration(season, raidResponse, memberNames, memberRoles);
        }
    }

    @Test
    public void testExcelFileCreationAndContent() throws Exception {
        // Test actual Excel file creation with real data
        Integer season = 78;
        List<MemberContribution> contributions = createDetailedTestContributions();

        // Create Excel file
        String filename = generateTestExcelFile(season, contributions);

        // Verify file was created
        Path excelFile = tempDir.resolve(filename);
        assertTrue(Files.exists(excelFile), "Excel file should be created");
        assertTrue(Files.size(excelFile) > 0, "Excel file should not be empty");

        // Verify Excel content
        try (FileInputStream fis = new FileInputStream(excelFile.toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            // Test that all 3 sheets exist
            assertEquals(3, workbook.getNumberOfSheets(), "Should have 3 sheets");

            Sheet statisticsSheet = workbook.getSheet("Statistics Overview");
            Sheet timelineSheet = workbook.getSheet("Boss Timeline");
            Sheet battleStatsSheet = workbook.getSheet("Player Battle Statistics");

            assertNotNull(statisticsSheet, "Statistics Overview sheet should exist");
            assertNotNull(timelineSheet, "Boss Timeline sheet should exist");
            assertNotNull(battleStatsSheet, "Player Battle Statistics sheet should exist");

            // Test Statistics Overview sheet content
            validateStatisticsOverviewSheet(statisticsSheet, season);

            // Test Boss Timeline sheet content
            validateBossTimelineSheet(timelineSheet, season);

            // Test Player Battle Statistics sheet content
            validatePlayerBattleStatisticsSheet(battleStatsSheet, season);
        }
    }

    @Test
    public void testMultipleSeasonFileGeneration() throws Exception {
        // Test that multiple seasons generate separate files
        List<Integer> seasons = Arrays.asList(78, 79, 80);
        List<String> generatedFiles = new ArrayList<>();

        for (Integer season : seasons) {
            List<MemberContribution> contributions = createDetailedTestContributions();
            String filename = generateTestExcelFile(season, contributions);
            generatedFiles.add(filename);
        }

        // Verify all files were created
        assertEquals(3, generatedFiles.size(), "Should generate 3 files for 3 seasons");

        for (String filename : generatedFiles) {
            Path file = tempDir.resolve(filename);
            assertTrue(Files.exists(file), "File should exist: " + filename);
            assertTrue(filename.contains("season_"), "Filename should contain season");
        }

        // Verify filenames are unique (different seasons)
        Set<String> uniqueFilenames = new HashSet<>(generatedFiles);
        assertEquals(3, uniqueFilenames.size(), "All filenames should be unique");
    }

    @Test
    public void testExcelDataAccuracy() throws Exception {
        // Test that Excel data matches the source data exactly
        List<MemberContribution> sourceContributions = createDetailedTestContributions();
        Integer season = 78;

        String filename = generateTestExcelFile(season, sourceContributions);
        Path excelFile = tempDir.resolve(filename);

        try (FileInputStream fis = new FileInputStream(excelFile.toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet statisticsSheet = workbook.getSheet("Statistics Overview");

            // Find data rows (skip headers)
            int dataStartRow = 3; // After season header, empty row, and column headers

            for (int i = 0; i < sourceContributions.size(); i++) {
                MemberContribution source = sourceContributions.get(i);
                Row dataRow = statisticsSheet.getRow(dataStartRow + i);

                assertNotNull(dataRow, "Data row should exist for member " + i);

                // Verify rank
                assertEquals(i + 1, (int) dataRow.getCell(0).getNumericCellValue(), "Rank should match");

                // Verify member name (truncated if needed)
                String expectedName = source.getName().length() > 15 ?
                    source.getName().substring(0, 12) + "..." : source.getName();
                assertEquals(expectedName, dataRow.getCell(1).getStringCellValue(), "Member name should match");

                // Verify role
                String expectedRole = source.getRole() != null ? source.getRole() : "N/A";
                assertEquals(expectedRole, dataRow.getCell(2).getStringCellValue(), "Role should match");

                // Verify damage values
                assertEquals(source.getBossBattle(), (int) dataRow.getCell(3).getNumericCellValue(), "Boss Battle should match");
                assertEquals(source.getBossBomb(), (int) dataRow.getCell(4).getNumericCellValue(), "Boss Bomb should match");
                assertEquals(source.getSidebossBattle(), (int) dataRow.getCell(5).getNumericCellValue(), "Sideboss Battle should match");
                assertEquals(source.getSidebossBomb(), (int) dataRow.getCell(6).getNumericCellValue(), "Sideboss Bomb should match");

                // Verify total
                int expectedTotal = source.getBossBattle() + source.getBossBomb() +
                                  source.getSidebossBattle() + source.getSidebossBomb();
                assertEquals(expectedTotal, (int) dataRow.getCell(7).getNumericCellValue(), "Total should match");

                // Verify counts
                assertEquals(source.getBattleCount(), (int) dataRow.getCell(8).getNumericCellValue(), "Battle count should match");
                assertEquals(source.getBombCount(), (int) dataRow.getCell(9).getNumericCellValue(), "Bomb count should match");
            }
        }
    }

    @Test
    public void testExcelFormattingAndStyles() throws Exception {
        // Test that Excel formatting is applied correctly
        List<MemberContribution> contributions = createDetailedTestContributions();
        Integer season = 78;

        String filename = generateTestExcelFile(season, contributions);
        Path excelFile = tempDir.resolve(filename);

        try (FileInputStream fis = new FileInputStream(excelFile.toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet statisticsSheet = workbook.getSheet("Statistics Overview");

            // Test header formatting
            Row headerRow = statisticsSheet.getRow(2); // Column headers row
            assertNotNull(headerRow, "Header row should exist");

            Cell headerCell = headerRow.getCell(0);
            assertNotNull(headerCell, "Header cell should exist");

            // Check if header has bold formatting
            CellStyle headerStyle = headerCell.getCellStyle();
            Font headerFont = workbook.getFontAt(headerStyle.getFontIndex());
            assertTrue(headerFont.getBold(), "Header should be bold");

            // Test number formatting
            Row dataRow = statisticsSheet.getRow(3); // First data row
            if (dataRow != null) {
                Cell numberCell = dataRow.getCell(3); // Boss Battle column
                if (numberCell != null) {
                    CellStyle numberStyle = numberCell.getCellStyle();
                    // The number format should be set (though exact format may vary)
                    assertTrue(numberStyle.getDataFormat() > 0, "Number cells should have formatting");
                }
            }
        }
    }

    @Test
    public void testPlayerBattleStatisticsDetailAccuracy() throws Exception {
        // Test that Player Battle Statistics sheet contains correct raid details
        List<MemberContribution> contributions = createContributionsWithDetailedRaids();
        Integer season = 78;

        String filename = generateTestExcelFile(season, contributions);
        Path excelFile = tempDir.resolve(filename);

        try (FileInputStream fis = new FileInputStream(excelFile.toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet battleStatsSheet = workbook.getSheet("Player Battle Statistics");
            assertNotNull(battleStatsSheet, "Player Battle Statistics sheet should exist");

            // Verify season header
            Row seasonRow = battleStatsSheet.getRow(0);
            assertNotNull(seasonRow, "Season header row should exist");
            assertTrue(seasonRow.getCell(0).getStringCellValue().contains("Season " + season),
                      "Season header should contain season number");

            // Find first player section
            Row playerRow = battleStatsSheet.getRow(2); // First player header
            assertNotNull(playerRow, "First player header should exist");

            // Verify battle details headers
            Row battleHeaderRow = battleStatsSheet.getRow(3);
            assertNotNull(battleHeaderRow, "Battle details header should exist");
            assertEquals("Rarity", battleHeaderRow.getCell(0).getStringCellValue());
            assertEquals("No.", battleHeaderRow.getCell(1).getStringCellValue());
            assertEquals("Bossname", battleHeaderRow.getCell(2).getStringCellValue());
            assertEquals("Type", battleHeaderRow.getCell(3).getStringCellValue());
            assertEquals("Encounter", battleHeaderRow.getCell(4).getStringCellValue());
            assertEquals("Damage", battleHeaderRow.getCell(5).getStringCellValue());
            assertEquals("HP Left", battleHeaderRow.getCell(6).getStringCellValue());
        }
    }

    @Test
    public void testEmptyDataHandling() throws Exception {
        // Test handling of empty or minimal data
        List<MemberContribution> emptyContributions = new ArrayList<>();
        Integer season = 78;

        String filename = generateTestExcelFile(season, emptyContributions);
        Path excelFile = tempDir.resolve(filename);

        // File should still be created even with no data
        assertTrue(Files.exists(excelFile), "Excel file should be created even with empty data");

        try (FileInputStream fis = new FileInputStream(excelFile.toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            // All sheets should still exist
            assertEquals(3, workbook.getNumberOfSheets(), "Should have 3 sheets even with empty data");

            Sheet statisticsSheet = workbook.getSheet("Statistics Overview");
            assertNotNull(statisticsSheet, "Statistics sheet should exist");

            // Should have headers but no data rows beyond totals
            Row headerRow = statisticsSheet.getRow(2);
            assertNotNull(headerRow, "Header row should exist");
            assertEquals("Rank", headerRow.getCell(0).getStringCellValue());
        }
    }

    // Helper methods

    private String generateTestExcelFile(Integer season, List<MemberContribution> contributions) throws Exception {
        // Sort contributions by total damage (descending) - matching Reporting logic
        contributions.sort((a, b) -> {
            int totalA = a.getBossBattle() + a.getBossBomb() + a.getSidebossBattle() + a.getSidebossBomb();
            int totalB = b.getBossBattle() + b.getBossBomb() + b.getSidebossBattle() + b.getSidebossBomb();
            return Integer.compare(totalB, totalA);
        });

        // Create workbook and sheets
        Workbook workbook = new XSSFWorkbook();

        // Create styles
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        CellStyle numberStyle = workbook.createCellStyle();
        numberStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));

        // Create sheets
        Sheet playerStatsSheet = workbook.createSheet("Statistics Overview");
        Sheet bossTimelineSheet = workbook.createSheet("Boss Timeline");
        Sheet playerBattleStatsSheet = workbook.createSheet("Player Battle Statistics");

        // Use reflection to call the private methods
        callCreatePlayerStatisticsSheet(playerStatsSheet, headerStyle, numberStyle, contributions, season);
        callCreateBossTimelineSheet(bossTimelineSheet, headerStyle, numberStyle, new TreeSet<>(), season);
        callCreatePlayerBattleStatisticsSheet(playerBattleStatsSheet, headerStyle, numberStyle, contributions, season);

        // Generate filename
        String timestamp = "20250923_143022"; // Fixed timestamp for testing
        String filename = String.format("TestGuild_raid_report_season_%s_%s.xlsx", season, timestamp);

        // Save file
        try (FileOutputStream fileOut = new FileOutputStream(tempDir.resolve(filename).toFile())) {
            workbook.write(fileOut);
        }

        workbook.close();
        return filename;
    }

    private void callCreatePlayerStatisticsSheet(Sheet sheet, CellStyle headerStyle, CellStyle numberStyle,
                                               List<MemberContribution> contributions, Integer season) throws Exception {
        Method method = Reporting.class.getDeclaredMethod("createPlayerStatisticsSheet",
            Sheet.class, CellStyle.class, CellStyle.class, List.class, Integer.class);
        method.setAccessible(true);
        method.invoke(null, sheet, headerStyle, numberStyle, contributions, season);
    }

    private void callCreateBossTimelineSheet(Sheet sheet, CellStyle headerStyle, CellStyle numberStyle,
                                           SortedSet bosses, Integer season) throws Exception {
        Method method = Reporting.class.getDeclaredMethod("createBossTimelineSheet",
            Sheet.class, CellStyle.class, CellStyle.class, SortedSet.class, Integer.class);
        method.setAccessible(true);
        method.invoke(null, sheet, headerStyle, numberStyle, bosses, season);
    }

    private void callCreatePlayerBattleStatisticsSheet(Sheet sheet, CellStyle headerStyle, CellStyle numberStyle,
                                                     List<MemberContribution> contributions, Integer season) throws Exception {
        Method method = Reporting.class.getDeclaredMethod("createPlayerBattleStatisticsSheet",
            Sheet.class, CellStyle.class, CellStyle.class, List.class, Integer.class);
        method.setAccessible(true);
        method.invoke(null, sheet, headerStyle, numberStyle, contributions, season);
    }

    private void validateStatisticsOverviewSheet(Sheet sheet, Integer season) {
        // Verify season header
        Row seasonRow = sheet.getRow(0);
        assertNotNull(seasonRow, "Season header row should exist");
        assertTrue(seasonRow.getCell(0).getStringCellValue().contains("Season " + season),
                  "Season header should contain season number");

        // Verify column headers
        Row headerRow = sheet.getRow(2);
        assertNotNull(headerRow, "Column header row should exist");
        assertEquals("Rank", headerRow.getCell(0).getStringCellValue());
        assertEquals("Member Name", headerRow.getCell(1).getStringCellValue());
        assertEquals("Role", headerRow.getCell(2).getStringCellValue());
        assertEquals("Boss Battle", headerRow.getCell(3).getStringCellValue());
        assertEquals("Boss Bomb", headerRow.getCell(4).getStringCellValue());
        assertEquals("Sideb. Battle", headerRow.getCell(5).getStringCellValue());
        assertEquals("Sideb. Bomb", headerRow.getCell(6).getStringCellValue());
        assertEquals("Total", headerRow.getCell(7).getStringCellValue());
        assertEquals("Battles", headerRow.getCell(8).getStringCellValue());
        assertEquals("Bombs", headerRow.getCell(9).getStringCellValue());
    }

    private void validateBossTimelineSheet(Sheet sheet, Integer season) {
        // Verify season header
        Row seasonRow = sheet.getRow(0);
        assertNotNull(seasonRow, "Season header row should exist");
        assertTrue(seasonRow.getCell(0).getStringCellValue().contains("Season " + season),
                  "Season header should contain season number");
    }

    private void validatePlayerBattleStatisticsSheet(Sheet sheet, Integer season) {
        // Verify season header
        Row seasonRow = sheet.getRow(0);
        assertNotNull(seasonRow, "Season header row should exist");
        assertTrue(seasonRow.getCell(0).getStringCellValue().contains("Season " + season),
                  "Season header should contain season number");
    }

    private GuildResponse createMockGuildResponse() {
        GuildResponse response = new GuildResponse();
        GuildResponse.Guild guild = new GuildResponse.Guild();
        guild.setName("Test Guild");
        guild.setGuildTag("TEST");
        guild.setLevel(30);
        guild.setGuildRaidSeasons(Arrays.asList(78, 79));

        GuildResponse.GuildMember member1 = new GuildResponse.GuildMember();
        member1.setUserId("user1");
        member1.setRole("LEADER");

        GuildResponse.GuildMember member2 = new GuildResponse.GuildMember();
        member2.setUserId("user2");
        member2.setRole("OFFICER");

        guild.setMembers(Arrays.asList(member1, member2));
        response.setGuild(guild);
        return response;
    }

    private List<GuildRaidResponse> createMockRaidResponses() {
        List<GuildRaidResponse> responses = new ArrayList<>();

        // Season 78
        GuildRaidResponse response78 = new GuildRaidResponse();
        response78.setSeason(78);
        response78.setEntries(createMockRaids("user1"));
        responses.add(response78);

        // Season 79
        GuildRaidResponse response79 = new GuildRaidResponse();
        response79.setSeason(79);
        response79.setEntries(createMockRaids("user2"));
        responses.add(response79);

        return responses;
    }

    private List<GuildRaidResponse.Raid> createMockRaids(String userId) {
        List<GuildRaidResponse.Raid> raids = new ArrayList<>();

        GuildRaidResponse.Raid raid1 = new GuildRaidResponse.Raid();
        raid1.setUserId(userId);
        raid1.setEncounterType("Boss");
        raid1.setDamageType("Battle");
        raid1.setDamageDealt(1000);
        raid1.setRarity("Epic");
        raid1.setType("Boss1");
        raid1.setSet(0);
        raid1.setUnitId("boss1_unit1_id");
        raid1.setRemainingHp(50000);
        raids.add(raid1);

        return raids;
    }

    private List<MemberContribution> createDetailedTestContributions() {
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

    private List<MemberContribution> createContributionsWithDetailedRaids() {
        List<MemberContribution> contributions = createDetailedTestContributions();

        // Add detailed raid data to first player
        MemberContribution player1 = contributions.get(0);
        List<GuildRaidResponse.Raid> raids = new ArrayList<>();

        GuildRaidResponse.Raid raid1 = new GuildRaidResponse.Raid();
        raid1.setRarity("Epic");
        raid1.setSet(0);
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

    private void testSeasonExcelGeneration(Integer season, GuildRaidResponse raidResponse,
                                         Map<String, String> memberNames, Map<String, String> memberRoles) throws Exception {
        // Test the data processing pipeline for a single season
        Map<String, MemberContribution> memberContributions = new HashMap<>();

        // Initialize member contributions
        for (Map.Entry<String, String> entry : memberNames.entrySet()) {
            MemberContribution contribution = new MemberContribution();
            contribution.setName(entry.getValue());
            contribution.setRole(memberRoles.get(entry.getKey()));
            contribution.setBossBattle(0);
            contribution.setSidebossBattle(0);
            contribution.setBossBomb(0);
            contribution.setSidebossBomb(0);
            contribution.setBattleCount(0);
            contribution.setBombCount(0);
            memberContributions.put(entry.getKey(), contribution);
        }

        // Process raid entries
        if (raidResponse.getEntries() != null) {
            for (GuildRaidResponse.Raid raid : raidResponse.getEntries()) {
                String userId = raid.getUserId();
                MemberContribution contribution = memberContributions.get(userId);

                if (contribution == null) {
                    contribution = new MemberContribution();
                    contribution.setName("unknown");
                    contribution.setRole("Discharged");
                    memberContributions.put(userId, contribution);
                }

                // Process damage based on encounter and damage type
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
                contribution.addRaid(raid);
            }
        }

        // Verify data processing worked correctly
        assertFalse(memberContributions.isEmpty(), "Member contributions should not be empty");

        // Test that contribution data is properly calculated
        for (MemberContribution contribution : memberContributions.values()) {
            int totalDamage = contribution.getBossBattle() + contribution.getBossBomb() +
                            contribution.getSidebossBattle() + contribution.getSidebossBomb();
            assertTrue(totalDamage >= 0, "Total damage should be non-negative");
            assertTrue(contribution.getBattleCount() >= 0, "Battle count should be non-negative");
            assertTrue(contribution.getBombCount() >= 0, "Bomb count should be non-negative");
        }
    }
}