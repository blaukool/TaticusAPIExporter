package de.blaukool.tacticus;

import de.blaukool.tacticus.logic.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import de.blaukool.tacticus.api.GuildResponse;
import de.blaukool.tacticus.api.GuildRaidResponse;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Reporting {

    private static final String API_BASE_URL = "https://api.tacticusgame.com";
    private static final String GUILD_ENDPOINT = "/api/v1/guild";
    private static final String GUILD_RAID_ENDPOINT = "/api/v1/guildRaid/";
    private static final String TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss";
    private static final String FILENAME_FORMAT = "%s_raid_report_season_%s_%s.xlsx";

    private static final String SHEET_STATISTICS_OVERVIEW = "Statistics Overview";
    private static final String SHEET_BOSS_TIMELINE = "Boss Timeline";
    private static final String SHEET_PLAYER_BATTLE_STATS = "Player Battle Statistics";

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Please provide an API key as an argument");
            return;
        }

        String apiKey = args[0];
        System.out.println("API Key: " + apiKey);


        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Step 1: Call guild data API and create base member data
            Map<String, String> memberNames = new HashMap<>();
            Map<String, String> memberRoles = new HashMap<>();
            Map<String, Integer> memberLevel = new HashMap<>();
            List<Integer> guildRaidSeasons = new ArrayList<>();
            String guildName = "";

            GuildResponse guildResponse = makeApiCall(httpClient, GUILD_ENDPOINT, apiKey, "Guild Data", GuildResponse.class, objectMapper);
            if (guildResponse != null) {
                guildName = guildResponse.getGuild().getName();

                // Store member names and roles for later use
                if (guildResponse.getGuild().getMembers() != null) {
                    for (GuildResponse.GuildMember member : guildResponse.getGuild().getMembers()) {
                        memberNames.put(member.getUserId(), member.getName());
                        memberRoles.put(member.getUserId(), member.getRole());
                        memberLevel.put(member.getUserId(), member.getLevel());
                    }
                }

                // Save guild raid seasons
                if (guildResponse.getGuild().getGuildRaidSeasons() != null) {
                    guildRaidSeasons.addAll(guildResponse.getGuild().getGuildRaidSeasons());
                }
            }

            // Step 2: Call guild raid API for each season and create separate files
            for (Integer season : guildRaidSeasons) {
                // Create Excel workbook for this season
                Workbook workbook = new XSSFWorkbook();

                // Create cell styles
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);

                CellStyle numberStyle = workbook.createCellStyle();
                numberStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));

                // Tab 1: Statistics per Player
                Sheet playerStatsSheet = workbook.createSheet(SHEET_STATISTICS_OVERVIEW);

                // Tab 2: Timeline per Boss
                Sheet bossTimelineSheet = workbook.createSheet(SHEET_BOSS_TIMELINE);

                // Tab 3: Player Battle Statistics
                Sheet playerBattleStatsSheet = workbook.createSheet(SHEET_PLAYER_BATTLE_STATS);

                Map<String, MemberContribution> memberContributions = new HashMap<>();

                // Initialize member contributions for this season
                for (Map.Entry<String, String> entry : memberNames.entrySet()) {
                    MemberContribution contribution = new MemberContribution();
                    contribution.setName(entry.getValue());
                    contribution.setLevel(memberLevel.get(entry.getKey()));
                    contribution.setRole(memberRoles.get(entry.getKey()));
                    contribution.setBossBattle(0);
                    contribution.setSidebossBattle(0);
                    contribution.setBossBomb(0);
                    contribution.setSidebossBomb(0);
                    contribution.setBattleCount(0);
                    contribution.setBombCount(0);
                    memberContributions.put(entry.getKey(), contribution);
                }

                // Call season-specific guild raid API
                GuildRaidResponse guildRaidResponse = makeApiCall(httpClient, GUILD_RAID_ENDPOINT + season, apiKey, "Guild Raid Season " + season, GuildRaidResponse.class, objectMapper);
                if (guildRaidResponse != null) {
                    // Add damage to respective member contributions
                    if (guildRaidResponse.getEntries() != null) {
                        for (GuildRaidResponse.Raid raid : guildRaidResponse.getEntries()) {
                            String userId = raid.getUserId();
                            int damage = raid.getDamageDealt();
                            String encounterType = raid.getEncounterType();
                            String damageType = raid.getDamageType();

                            MemberContribution contribution = memberContributions.get(userId);
                            if (contribution == null) {
                                contribution = new MemberContribution();
                                contribution.setName(new UserIDTranslator().getUserName(userId));
                                contribution.setRole("Discharged");
                                memberContributions.put(userId, contribution);
                            }
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

                    // Sort contributions by total damage
                    List<MemberContribution> sortedContributions = new ArrayList<>(memberContributions.values());
                    sortedContributions.sort((a, b) -> {
                        int totalA = a.getBossBattle() + a.getBossBomb() + a.getSidebossBattle() + a.getSidebossBomb();
                        int totalB = b.getBossBattle() + b.getBossBomb() + b.getSidebossBattle() + b.getSidebossBomb();
                        return Integer.compare(totalB, totalA);
                    });

                    // Fill Player Statistics sheet for this season
                    createPlayerStatisticsSheet(playerStatsSheet, headerStyle, numberStyle, sortedContributions, season);

                    // Process boss data
                    SortedSet<Boss> bosses = new TreeSet<>();
                    if (guildRaidResponse.getEntries() != null) {
                        UserIDTranslator translator = new UserIDTranslator();
                        Map<String, Boss> bossMap = new HashMap<>();
                        for (GuildRaidResponse.Raid raid : guildRaidResponse.getEntries()) {
                            Boss thisBoss = bossMap.get(raid.getRarity() + raid.getType());
                            if (thisBoss == null) {
                                thisBoss = new Boss(raid.getType(), raid.getSet() + 1, raid.getRarity());
                                bossMap.put(raid.getRarity() + raid.getType(), thisBoss);
                            }
                            Attack thisAttack = new Attack(translator.getUserName(raid.getUserId()), raid.getDamageDealt(), raid.getRemainingHp());
                            if ("SideBoss".equals(raid.getEncounterType())) {
                                String sidebossName = raid.getUnitIdFormated();
                                Sideboss thisSideboss = null;
                                for (Sideboss sb : thisBoss.getSideboss()) {
                                    if (sidebossName.equals(sb.getName())) {
                                        thisSideboss = sb;
                                    }
                                }
                                if (thisSideboss == null) {
                                    thisSideboss = new Sideboss(sidebossName);
                                    thisBoss.getSideboss().add(thisSideboss);
                                }
                                thisSideboss.getAttacks().add(thisAttack);
                            } else {
                                thisBoss.getAttacks().add(thisAttack);
                            }
                        }
                        bosses.addAll(bossMap.values());
                    }

                    // Fill Boss Timeline sheet for this season
                    createBossTimelineSheet(bossTimelineSheet, headerStyle, numberStyle, bosses, season);

                    // Fill Player Battle Statistics sheet for this season
                    createPlayerBattleStatisticsSheet(playerBattleStatsSheet, headerStyle, numberStyle, sortedContributions, season);

                    // Save the Excel file for this season
                    String timestamp = new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
                    String filename = String.format(FILENAME_FORMAT,
                        guildName.replaceAll("[^a-zA-Z0-9]", "_"), season, timestamp);

                    try (FileOutputStream fileOut = new FileOutputStream(filename)) {
                        workbook.write(fileOut);
                        System.out.println("Excel report created for Season " + season + ": " + filename);
                    }

                    workbook.close();
                }
            }

        } catch (IOException e) {
            System.err.println("Error making API calls or creating Excel: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private static void createPlayerStatisticsSheet(Sheet sheet, CellStyle headerStyle, CellStyle numberStyle, List<MemberContribution> sortedContributions, Integer season) {
        int rowNum = 0;

        // Season header
        Row seasonHeaderRow = sheet.createRow(rowNum++);
        Cell seasonHeaderCell = seasonHeaderRow.createCell(0);
        seasonHeaderCell.setCellValue("Guild Raid Season " + season + " Member Contributions");
        seasonHeaderCell.setCellStyle(headerStyle);

        rowNum++; // Empty row

        // Table header
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Rank", "Member Name", "Role", "Level",  "Boss Battle", "Boss Bomb", "Sideb. Battle", "Sideb. Bomb", "Total", "Battles", "Bombs"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        int rank = 1;
        int totalBossBattle = 0, totalBossBomb = 0, totalSidebossBattle = 0, totalSidebossBomb = 0;
        int totalBattles = 0, totalBombs = 0;

        for (MemberContribution contribution : sortedContributions) {
            Row dataRow = sheet.createRow(rowNum++);

            int total = contribution.getBossBattle() + contribution.getBossBomb() +
                       contribution.getSidebossBattle() + contribution.getSidebossBomb();

            dataRow.createCell(0).setCellValue(rank);
            dataRow.createCell(1).setCellValue(contribution.getName());
            dataRow.createCell(2).setCellValue(contribution.getRole() != null ? contribution.getRole() : "N/A");
            dataRow.createCell(3).setCellValue(contribution.getLevel() != null ? contribution.getLevel().toString() : "N/A");

            Cell bossBattleCell = dataRow.createCell(4);
            bossBattleCell.setCellValue(contribution.getBossBattle());
            bossBattleCell.setCellStyle(numberStyle);

            Cell bossBombCell = dataRow.createCell(5);
            bossBombCell.setCellValue(contribution.getBossBomb());
            bossBombCell.setCellStyle(numberStyle);

            Cell sidebossBattleCell = dataRow.createCell(6);
            sidebossBattleCell.setCellValue(contribution.getSidebossBattle());
            sidebossBattleCell.setCellStyle(numberStyle);

            Cell sidebossBombCell = dataRow.createCell(7);
            sidebossBombCell.setCellValue(contribution.getSidebossBomb());
            sidebossBombCell.setCellStyle(numberStyle);

            Cell totalCell = dataRow.createCell(8);
            totalCell.setCellValue(total);
            totalCell.setCellStyle(numberStyle);

            dataRow.createCell(9).setCellValue(contribution.getBattleCount());
            dataRow.createCell(10).setCellValue(contribution.getBombCount());

            // Accumulate totals
            totalBossBattle += contribution.getBossBattle();
            totalBossBomb += contribution.getBossBomb();
            totalSidebossBattle += contribution.getSidebossBattle();
            totalSidebossBomb += contribution.getSidebossBomb();
            totalBattles += contribution.getBattleCount();
            totalBombs += contribution.getBombCount();

            rank++;
        }

        // Total row
        Row totalRow = sheet.createRow(rowNum++);
        totalRow.createCell(0).setCellValue("");
        Cell totalLabelCell = totalRow.createCell(1);
        totalLabelCell.setCellValue("TOTAL");
        totalLabelCell.setCellStyle(headerStyle);
        totalRow.createCell(2).setCellValue("");
        totalRow.createCell(3).setCellValue("");
        Cell totalBossBattleCell = totalRow.createCell(4);
        totalBossBattleCell.setCellValue(totalBossBattle);
        totalBossBattleCell.setCellStyle(numberStyle);

        Cell totalBossBombCell = totalRow.createCell(5);
        totalBossBombCell.setCellValue(totalBossBomb);
        totalBossBombCell.setCellStyle(numberStyle);

        Cell totalSidebossBattleCell = totalRow.createCell(6);
        totalSidebossBattleCell.setCellValue(totalSidebossBattle);
        totalSidebossBattleCell.setCellStyle(numberStyle);

        Cell totalSidebossBombCell = totalRow.createCell(7);
        totalSidebossBombCell.setCellValue(totalSidebossBomb);
        totalSidebossBombCell.setCellStyle(numberStyle);

        Cell grandTotalCell = totalRow.createCell(8);
        grandTotalCell.setCellValue(totalBossBattle + totalBossBomb + totalSidebossBattle + totalSidebossBomb);
        grandTotalCell.setCellStyle(numberStyle);

        totalRow.createCell(9).setCellValue(totalBattles);
        totalRow.createCell(10).setCellValue(totalBombs);

        // Auto-size columns
        for (int i = 0; i < 11; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static void createBossTimelineSheet(Sheet sheet, CellStyle headerStyle, CellStyle numberStyle, SortedSet<Boss> bosses, Integer season) {
        int rowNum = 0;

        // Season header
        Row seasonHeaderRow = sheet.createRow(rowNum++);
        Cell seasonHeaderCell = seasonHeaderRow.createCell(0);
        seasonHeaderCell.setCellValue("Guild Raid Season " + season + " Boss Timeline");
        seasonHeaderCell.setCellStyle(headerStyle);

        rowNum++; // Empty row

        for (Boss boss : bosses) {
            // Boss header
            Row bossHeaderRow = sheet.createRow(rowNum++);
            Cell bossHeaderCell = bossHeaderRow.createCell(0);
            bossHeaderCell.setCellValue(boss.getRarity() + " " +  boss.getName());
            bossHeaderCell.setCellStyle(headerStyle);

            // Boss attacks header
            Row attacksHeaderRow = sheet.createRow(rowNum++);
            String[] attackHeaders = {"Player", "Damage", "HP Remaining"};
            for (int i = 0; i < attackHeaders.length; i++) {
                Cell cell = attacksHeaderRow.createCell(i);
                cell.setCellValue(attackHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            // Boss attacks
            for (Attack attack : boss.getAttacks()) {
                Row attackRow = sheet.createRow(rowNum++);
                attackRow.createCell(0).setCellValue(attack.getAttackerName());

                Cell damageCell = attackRow.createCell(1);
                damageCell.setCellValue(attack.getDamage());
                damageCell.setCellStyle(numberStyle);

                Cell hpCell = attackRow.createCell(2);
                hpCell.setCellValue(attack.getRemainingHealth());
                hpCell.setCellStyle(numberStyle);
            }

            // Sideboss attacks
            for (Sideboss sideboss : boss.getSideboss()) {
                rowNum++; // Empty row

                Row sidebossHeaderRow = sheet.createRow(rowNum++);
                Cell sidebossBossHeaderCell = sidebossHeaderRow.createCell(0);
                sidebossBossHeaderCell.setCellValue(boss.getRarity() + " " +  boss.getName());
                sidebossBossHeaderCell.setCellStyle(headerStyle);
                Cell sidebossHeaderCell = sidebossHeaderRow.createCell(1);
                sidebossHeaderCell.setCellValue("Sideboss: " + sideboss.getName());
                sidebossHeaderCell.setCellStyle(headerStyle);

                Row sidebossAttacksHeaderRow = sheet.createRow(rowNum++);
                for (int i = 0; i < attackHeaders.length; i++) {
                    Cell cell = sidebossAttacksHeaderRow.createCell(i);
                    cell.setCellValue(attackHeaders[i]);
                    cell.setCellStyle(headerStyle);
                }

                for (Attack attack : sideboss.getAttacks()) {
                    Row attackRow = sheet.createRow(rowNum++);
                    attackRow.createCell(0).setCellValue(attack.getAttackerName());

                    Cell damageCell = attackRow.createCell(1);
                    damageCell.setCellValue(attack.getDamage());
                    damageCell.setCellStyle(numberStyle);

                    Cell hpCell = attackRow.createCell(2);
                    hpCell.setCellValue(attack.getRemainingHealth());
                    hpCell.setCellStyle(numberStyle);
                }
            }

            rowNum += 2; // Empty rows between bosses
        }

        // Auto-size columns
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static void createPlayerBattleStatisticsSheet(Sheet sheet, CellStyle headerStyle, CellStyle numberStyle, List<MemberContribution> sortedContributions, Integer season) {
        int rowNum = 0;

        // Season header
        Row seasonHeaderRow = sheet.createRow(rowNum++);
        Cell seasonHeaderCell = seasonHeaderRow.createCell(0);
        seasonHeaderCell.setCellValue("Guild Raid Season " + season + " Player Battle Statistics");
        seasonHeaderCell.setCellStyle(headerStyle);

        rowNum++; // Empty row

        // Sort contributions alphabetically by name for the details view
        List<MemberContribution> alphabeticalContributions = new ArrayList<>(sortedContributions);
        alphabeticalContributions.sort((a, b) -> CharSequence.compare(a.getName(), b.getName()));

        for (MemberContribution contribution : alphabeticalContributions) {
            // Player name header
            Row playerHeaderRow = sheet.createRow(rowNum++);
            Cell playerHeaderCell = playerHeaderRow.createCell(0);
            playerHeaderCell.setCellValue(contribution.getName());
            playerHeaderCell.setCellStyle(headerStyle);

            // Battle details header
            Row battleHeaderRow = sheet.createRow(rowNum++);
            String[] battleHeaders = {"Rarity", "No.", "Bossname", "Type", "Encounter", "Damage", "HP Left"};
            for (int i = 0; i < battleHeaders.length; i++) {
                Cell cell = battleHeaderRow.createCell(i);
                cell.setCellValue(battleHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            // Battle details (excluding bombs, matching the original logic)
            for (GuildRaidResponse.Raid raid : contribution.getRaids()) {
                if (!"Bomb".equals(raid.getDamageType())) {
                    Row battleRow = sheet.createRow(rowNum++);

                    battleRow.createCell(0).setCellValue(raid.getRarity() != null ? raid.getRarity() : "");
                    battleRow.createCell(1).setCellValue(raid.getSet() + 1);
                    battleRow.createCell(2).setCellValue(raid.getType() != null ? raid.getType() : "");
                    battleRow.createCell(3).setCellValue(raid.getEncounterType() != null ? raid.getEncounterType() : "");
                    battleRow.createCell(4).setCellValue(raid.getUnitIdFormated());

                    Cell damageCell = battleRow.createCell(5);
                    damageCell.setCellValue(raid.getDamageDealt());
                    damageCell.setCellStyle(numberStyle);

                    Cell hpCell = battleRow.createCell(6);
                    hpCell.setCellValue(raid.getRemainingHp());
                    hpCell.setCellStyle(numberStyle);
                }
            }

            rowNum += 2; // Empty rows between players
        }

        // Auto-size columns
        for (int i = 0; i < 7; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static <T> T makeApiCall(CloseableHttpClient httpClient, String endpoint, String apiKey, String description, Class<T> responseType, ObjectMapper objectMapper) throws IOException {
        System.out.println("\n=== " + description + " ===");

        HttpGet request = new HttpGet(API_BASE_URL + endpoint);
        request.addHeader("X-API-KEY", apiKey);
        request.addHeader("Content-Type", "application/json");

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());

            System.out.println("Endpoint: " + endpoint);
            System.out.println("Status Code: " + statusCode);

            if (statusCode == 200) {
                try {
                    T parsedResponse = objectMapper.readValue(responseBody, responseType);
                    System.out.println("Successfully parsed " + description);
                    return parsedResponse;
                } catch (Exception e) {
                    System.err.println("Error parsing JSON response: " + e.getMessage());
                    System.out.println("Raw Response: " + responseBody);
                }
            } else {
                System.out.println("Error Response: " + responseBody);
            }
        }
        return null;
    }
}