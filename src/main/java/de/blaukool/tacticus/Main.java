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

import java.io.IOException;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Please provide an API key as an argument");
            return;
        }

        String api_key = args[0];
        System.out.println("API Key: " + api_key);

        boolean printDetails = false;
        if (args.length > 1) {
            if ("details".equals(args[1])) {
                printDetails = true;
            }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Step 1: Call guild data API and create base member data
            Map<String, String> memberNames = new HashMap<>();
            Map<String, String> memberRoles = new HashMap<>();
            List<Integer> guildRaidSeasons = new ArrayList<>();

            GuildResponse guildResponse = makeApiCall(httpClient, "/api/v1/guild", api_key, "Guild Data", GuildResponse.class, objectMapper);
            if (guildResponse != null) {
                System.out.println("Guild Name: " + guildResponse.getGuild().getName());
                System.out.println("Guild Tag: " + guildResponse.getGuild().getGuildTag());
                System.out.println("Guild Level: " + guildResponse.getGuild().getLevel());
                System.out.println("Members Count: " + (guildResponse.getGuild().getMembers() != null ? guildResponse.getGuild().getMembers().size() : 0));

                // Store member names and roles for later use
                if (guildResponse.getGuild().getMembers() != null) {
                    for (GuildResponse.GuildMember member : guildResponse.getGuild().getMembers()) {
                        memberNames.put(member.getUserId(), member.getName());
                        memberRoles.put(member.getUserId(), member.getRole());
                    }
                }

                // Save guild raid seasons
                if (guildResponse.getGuild().getGuildRaidSeasons() != null) {
                    guildRaidSeasons.addAll(guildResponse.getGuild().getGuildRaidSeasons());
                    System.out.println("Available Guild Raid Seasons: " + guildRaidSeasons);
                }
            }

            // Step 2: Call guild raid API for each season
            for (Integer season : guildRaidSeasons) {
                Map<String, MemberContribution> memberContributions = new HashMap<>();

                // Initialize member contributions for this season
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

                // Call season-specific guild raid API
                GuildRaidResponse guildRaidResponse = makeApiCall(httpClient, "/api/v1/guildRaid/" + season, api_key, "Guild Raid Season " + season, GuildRaidResponse.class, objectMapper);
                if (guildRaidResponse != null) {
                    System.out.println("Season: " + guildRaidResponse.getSeason());
                    System.out.println("Raid Entries: " + (guildRaidResponse.getEntries() != null ? guildRaidResponse.getEntries().size() : 0));

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

                    // Step 3: Print the list of member contributions for this season
                    System.out.println("\n=== Guild Raid Season " + season + " Member Contributions ===");
                    List<MemberContribution> sortedContributions = new ArrayList<>(memberContributions.values());
                    // Sort by total damage (sum of all contribution types) descending
                    sortedContributions.sort((a, b) -> {
                        int totalA = a.getBossBattle() + a.getBossBomb() + a.getSidebossBattle() + a.getSidebossBomb();
                        int totalB = b.getBossBattle() + b.getBossBomb() + b.getSidebossBattle() + b.getSidebossBomb();
                        return Integer.compare(totalB, totalA);
                    });

                    // Print table header
                    System.out.println(String.format("%-4s %-15s %-10s %12s %12s %12s %12s %12s %8s %8s",
                            "Rank", "Member Name", "Role", "Boss Battle", "Boss Bomb", "Sideb. Battle", "Sideb. Bomb", "Total", "Battles", "Bombs"));
                    System.out.println("─".repeat(125));

                    // Print table rows and calculate totals
                    int rank = 1;
                    int totalBossBattle = 0, totalBossBomb = 0, totalSidebossBattle = 0, totalSidebossBomb = 0;
                    int totalBattles = 0, totalBombs = 0;

                    for (MemberContribution contribution : sortedContributions) {
                        int total = contribution.getBossBattle() + contribution.getBossBomb() +
                                contribution.getSidebossBattle() + contribution.getSidebossBomb();
                        System.out.println(String.format("%-4d %-15s %-10s %,12d %,12d %,12d %,12d %,12d %8d %8d",
                                rank,
                                contribution.getName().length() > 15 ? contribution.getName().substring(0, 12) + "..." : contribution.getName(),
                                contribution.getRole() != null ? contribution.getRole() : "N/A",
                                contribution.getBossBattle(),
                                contribution.getBossBomb(),
                                contribution.getSidebossBattle(),
                                contribution.getSidebossBomb(),
                                total,
                                contribution.getBattleCount(),
                                contribution.getBombCount()));

                        // Accumulate totals
                        totalBossBattle += contribution.getBossBattle();
                        totalBossBomb += contribution.getBossBomb();
                        totalSidebossBattle += contribution.getSidebossBattle();
                        totalSidebossBomb += contribution.getSidebossBomb();
                        totalBattles += contribution.getBattleCount();
                        totalBombs += contribution.getBombCount();

                        rank++;
                    }

                    // Print summary line
                    int grandTotal = totalBossBattle + totalBossBomb + totalSidebossBattle + totalSidebossBomb;
                    System.out.println("─".repeat(125));
                    System.out.println(String.format("%-4s %-15s %-10s %,12d %,12d %,12d %,12d %,12d %8d %8d",
                            "", "TOTAL", "",
                            totalBossBattle, totalBossBomb, totalSidebossBattle, totalSidebossBomb,
                            grandTotal, totalBattles, totalBombs));
                    System.out.println(); // Add blank line between seasons

                    if (printDetails) {
                        sortedContributions.sort((a, b) -> {
                            return CharSequence.compare(a.getName(), b.getName());
                        });

                        for (MemberContribution contribution : sortedContributions) {
                            System.out.println(contribution.getName());
                            System.out.println(String.format("| %-10s | %-3s | %-20s | %-10s | %-25s | %-10s | %-10s |",
                                    "Rarity", "No.", "Bossname", "Typ", "Encounter", "Damage", "HP Left"));
                            System.out.println("─".repeat(110));
                            for (GuildRaidResponse.Raid raid : contribution.getRaids()) {
                                if (!"Bomb".equals(raid.getDamageType())) {
                                    System.out.println(raid.toString());
                                }
                            }
                            System.out.println("─".repeat(110));
                        }
                    }

                    // Bossviews
                    if (guildRaidResponse.getEntries() != null) {
                        UserIDTranslator translator = new UserIDTranslator();
                        Map<String, Boss> bosses = new HashMap<>();
                        for (GuildRaidResponse.Raid raid : guildRaidResponse.getEntries()) {
                            Boss thisBoss = bosses.get(raid.getRarity() + raid.getType());
                            if (thisBoss == null) {
                                thisBoss = new Boss(raid.getType(),raid.getSet()+1,raid.getRarity());
                                bosses.put(raid.getRarity() + raid.getType(), thisBoss);
                            }
                            Attack thisAttack = new Attack(translator.getUserName(raid.getUserId()),raid.getDamageDealt(), raid.getRemainingHp());
                            if("SideBoss".equals(raid.getEncounterType())) {
                                String sidebossName = raid.getUnitIdFormated();
                                Sideboss thisSideboss = null;
                                for(Sideboss sb : thisBoss.getSideboss()){
                                    if (sidebossName.equals(sb.getName())) {
                                        thisSideboss = sb;
                                    }
                                }
                                if(thisSideboss == null){
                                    thisSideboss = new Sideboss(sidebossName);
                                    thisBoss.getSideboss().add(thisSideboss);
                                }
                                thisSideboss.getAttacks().add(thisAttack);
                            }else {
                                thisBoss.getAttacks().add(thisAttack);
                            }
                        }
                        SortedSet<Boss> output = new TreeSet<>(bosses.values());
                        System.out.println(output.toString());
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Error making API calls: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static <T> T makeApiCall(CloseableHttpClient httpClient, String endpoint, String apiKey, String description, Class<T> responseType, ObjectMapper objectMapper) throws IOException {
        System.out.println("\n=== " + description + " ===");

        HttpGet request = new HttpGet("https://api.tacticusgame.com" + endpoint);
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
