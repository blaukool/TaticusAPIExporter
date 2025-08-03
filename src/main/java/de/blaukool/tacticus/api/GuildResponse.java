package de.blaukool.tacticus.api;

import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import de.blaukool.tacticus.logic.UserIDTranslator;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;

public class GuildResponse {
    private Guild guild;

    public Guild getGuild() { return guild; }
    public void setGuild(Guild guild) { this.guild = guild; }

    public static class Guild {
        private String guildId;
        private String guildTag;
        private String name;
        private int level;
        private List<GuildMember> members;
        private List<Integer> guildRaidSeasons;

        public String getGuildId() { return guildId; }
        public void setGuildId(String guildId) { this.guildId = guildId; }
        public String getGuildTag() { return guildTag; }
        public void setGuildTag(String guildTag) { this.guildTag = guildTag; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
        public List<GuildMember> getMembers() { return members; }
        public void setMembers(List<GuildMember> members) { this.members = members; }
        public List<Integer> getGuildRaidSeasons() { return guildRaidSeasons; }
        public void setGuildRaidSeasons(List<Integer> guildRaidSeasons) { this.guildRaidSeasons = guildRaidSeasons; }
    }

    public static class GuildMember {
        private String userId;
        private String role;
        private int level;
        private Date lastActivityOn;
        private static final UserIDTranslator userIdTranslator = new UserIDTranslator();

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
        public Date getLastActivityOn() { return lastActivityOn; }
        public void setLastActivityOn(Date lastActivityOn) { this.lastActivityOn = lastActivityOn; }
        
        public String getName() {
            String name = userIdTranslator.getUserName(userId);
            if (name.equals("unknown")) {
                System.out.println(userId + " is new, "+role+" "+level);
            }
            return name;
        }
    }
}