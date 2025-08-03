package de.blaukool.tacticus;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import de.blaukool.tacticus.api.GuildResponse;

import java.io.IOException;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OfftimeChecker {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Please provide an API key as an argument");
            return;
        }
        
        String apiKey = args[0];
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            GuildResponse guildResponse = makeApiCall(httpClient, "/api/v1/guild", apiKey, objectMapper);
            
            if (guildResponse != null && guildResponse.getGuild() != null && guildResponse.getGuild().getMembers() != null) {
                System.out.println("Guild Name: " + guildResponse.getGuild().getName());
                System.out.println("Guild Tag: " + guildResponse.getGuild().getGuildTag());
                System.out.println();
                
                List<MemberActivityInfo> allMembers = getAllMembersWithActivity(guildResponse.getGuild().getMembers());
                
                System.out.println("=== All Guild Members Activity Status ===");
                System.out.println(String.format("%-20s %-12s %-8s %s", 
                    "Member Name", "Role", "Level", "Time Since Last Activity"));
                System.out.println("─".repeat(80));
                
                int inactiveCount = 0;
                for (MemberActivityInfo member : allMembers) {
                    String nameDisplay = member.name.length() > 20 ? member.name.substring(0, 17) + "..." : member.name;
                    String roleDisplay = member.role != null ? member.role : "N/A";
                    
                    if (member.isInactive) {
                        // Red color for inactive members
                        System.out.println(String.format("\u001B[31m%-20s %-12s %-8d %s\u001B[0m", 
                            nameDisplay, roleDisplay, member.level, member.timeSinceActivity));
                        inactiveCount++;
                    } else {
                        System.out.println(String.format("%-20s %-12s %-8d %s", 
                            nameDisplay, roleDisplay, member.level, member.timeSinceActivity));
                    }
                }
                
                System.out.println();
                System.out.println("Total members: " + allMembers.size());
                System.out.println("Inactive members (>48h): " + inactiveCount);
            }
            
        } catch (IOException e) {
            System.err.println("Error making API call: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static List<MemberActivityInfo> getAllMembersWithActivity(List<GuildResponse.GuildMember> members) {
        List<MemberActivityInfo> allMembers = new ArrayList<>();
        long now = System.currentTimeMillis();
        long fortyEightHours = 48 * 60 * 60 * 1000; // 48 hours in milliseconds
        
        for (GuildResponse.GuildMember member : members) {
            MemberActivityInfo memberInfo = new MemberActivityInfo();
            memberInfo.name = member.getName();
            memberInfo.role = member.getRole();
            memberInfo.level = member.getLevel();
            
            if (member.getLastActivityOn() != null) {
                long timeSinceActivity = now - member.getLastActivityOn().getTime();
                memberInfo.timeSinceActivity = formatDurationDetailed(timeSinceActivity);
                memberInfo.isInactive = timeSinceActivity > fortyEightHours;
                memberInfo.lastActivityTimestamp = member.getLastActivityOn().getTime();
            } else {
                memberInfo.timeSinceActivity = "Unknown";
                memberInfo.isInactive = true; // Consider unknown activity as inactive
                memberInfo.lastActivityTimestamp = 0;
            }
            
            allMembers.add(memberInfo);
        }
        
        // Sort by role hierarchy first, then by name
        allMembers.sort((a, b) -> {
            // First sort by role priority
            int roleComparison = Integer.compare(getRolePriority(a.role), getRolePriority(b.role));
            if (roleComparison != 0) {
                return roleComparison;
            }
            // Then sort by name alphabetically
            return a.name.compareToIgnoreCase(b.name);
        });
        
        return allMembers;
    }
    
    private static int getRolePriority(String role) {
        if (role == null) return 4; // Unknown roles go last
        switch (role.toUpperCase()) {
            case "LEADER": return 0;
            case "CO_LEADER": return 1;
            case "OFFICER": return 2;
            case "MEMBER": return 3;
            default: return 4; // Unknown roles go last
        }
    }
    
    private static String formatDurationDetailed(long milliseconds) {
        long totalSeconds = milliseconds / 1000;
        long hours = totalSeconds / 3600;
        long remainingSeconds = totalSeconds % 3600;
        long minutes = remainingSeconds / 60;
        long seconds = remainingSeconds % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
    
    private static GuildResponse makeApiCall(CloseableHttpClient httpClient, String endpoint, String apiKey, ObjectMapper objectMapper) throws IOException {
        HttpGet request = new HttpGet("https://api.tacticusgame.com" + endpoint);
        request.addHeader("X-API-KEY", apiKey);
        request.addHeader("Content-Type", "application/json");
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());
            
            if (statusCode == 200) {
                try {
                    return objectMapper.readValue(responseBody, GuildResponse.class);
                } catch (Exception e) {
                    System.err.println("Error parsing JSON response: " + e.getMessage());
                    System.out.println("Raw Response: " + responseBody);
                }
            } else {
                System.out.println("API Error - Status Code: " + statusCode);
                System.out.println("Error Response: " + responseBody);
            }
        }
        return null;
    }
    
    private static class MemberActivityInfo {
        String name;
        String role;
        int level;
        String timeSinceActivity;
        boolean isInactive;
        long lastActivityTimestamp;
    }
}