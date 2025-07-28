package de.blaukool.tacticus;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import de.blaukool.tacticus.api.PlayerResponse;
import de.blaukool.tacticus.api.GuildResponse;
import de.blaukool.tacticus.api.GuildRaidResponse;
import de.blaukool.tacticus.logic.MemberContribution;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Please provide an API key as an argument");
            return;
        }
        
        String api_key = args[0];
        System.out.println("API Key: " + api_key);
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Step 1: Call guild data API and create base member data
            Map<String, String> memberNames = new HashMap<>();
            List<Integer> guildRaidSeasons = new ArrayList<>();
            
            GuildResponse guildResponse = makeApiCall(httpClient, "/api/v1/guild", api_key, "Guild Data", GuildResponse.class, objectMapper);
            if (guildResponse != null) {
                System.out.println("Guild Name: " + guildResponse.getGuild().getName());
                System.out.println("Guild Tag: " + guildResponse.getGuild().getGuildTag());
                System.out.println("Guild Level: " + guildResponse.getGuild().getLevel());
                System.out.println("Members Count: " + (guildResponse.getGuild().getMembers() != null ? guildResponse.getGuild().getMembers().size() : 0));
                
                // Store member names for later use
                if (guildResponse.getGuild().getMembers() != null) {
                    for (GuildResponse.GuildMember member : guildResponse.getGuild().getMembers()) {
                        memberNames.put(member.getUserId(), member.getName());
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
                    contribution.setDamage(0);
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
                            
                            MemberContribution contribution = memberContributions.get(userId);
                            if (contribution != null) {
                                contribution.addDamage(damage);
                            }
                        }
                    }
                    
                    // Step 3: Print the list of member contributions for this season
                    System.out.println("\n=== Guild Raid Season " + season + " Member Contributions ===");
                    List<MemberContribution> sortedContributions = new ArrayList<>(memberContributions.values());
                    sortedContributions.sort((a, b) -> b.getDamage().compareTo(a.getDamage())); // Sort by damage descending
                    
                    // Print table header
                    System.out.println(String.format("%-4s %-20s %15s", "Rank", "Member Name", "Total Damage"));
                    System.out.println("─".repeat(43));
                    
                    // Print table rows
                    int rank = 1;
                    for (MemberContribution contribution : sortedContributions) {
                        System.out.println(String.format("%-4d %-20s %,15d", 
                            rank, 
                            contribution.getName().length() > 20 ? contribution.getName().substring(0, 17) + "..." : contribution.getName(),
                            contribution.getDamage()));
                        rank++;
                    }
                    System.out.println(); // Add blank line between seasons
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
