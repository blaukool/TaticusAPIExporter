package de.blaukool.tacticus.api;

import java.util.List;

public class Progress {
    private List<CampaignProgress> campaigns;
    private Arena arena;
    private GuildRaid guildRaid;
    private Onslaught onslaught;
    private SalvageRun salvageRun;

    public List<CampaignProgress> getCampaigns() { return campaigns; }
    public void setCampaigns(List<CampaignProgress> campaigns) { this.campaigns = campaigns; }
    public Arena getArena() { return arena; }
    public void setArena(Arena arena) { this.arena = arena; }
    public GuildRaid getGuildRaid() { return guildRaid; }
    public void setGuildRaid(GuildRaid guildRaid) { this.guildRaid = guildRaid; }
    public Onslaught getOnslaught() { return onslaught; }
    public void setOnslaught(Onslaught onslaught) { this.onslaught = onslaught; }
    public SalvageRun getSalvageRun() { return salvageRun; }
    public void setSalvageRun(SalvageRun salvageRun) { this.salvageRun = salvageRun; }

    public static class CampaignProgress {
        private String id;
        private String name;
        private String type;
        private List<CampaignLevel> battles;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public List<CampaignLevel> getBattles() { return battles; }
        public void setBattles(List<CampaignLevel> battles) { this.battles = battles; }
    }

    public static class CampaignLevel {
        private int battleIndex;
        private int attemptsLeft;
        private int attemptsUsed;

        public int getBattleIndex() { return battleIndex; }
        public void setBattleIndex(int battleIndex) { this.battleIndex = battleIndex; }
        public int getAttemptsLeft() { return attemptsLeft; }
        public void setAttemptsLeft(int attemptsLeft) { this.attemptsLeft = attemptsLeft; }
        public int getAttemptsUsed() { return attemptsUsed; }
        public void setAttemptsUsed(int attemptsUsed) { this.attemptsUsed = attemptsUsed; }
    }

    public static class Arena {
        private Token tokens;

        public Token getTokens() { return tokens; }
        public void setTokens(Token tokens) { this.tokens = tokens; }
    }

    public static class GuildRaid {
        private Token tokens;
        private Token bombTokens;

        public Token getTokens() { return tokens; }
        public void setTokens(Token tokens) { this.tokens = tokens; }
        public Token getBombTokens() { return bombTokens; }
        public void setBombTokens(Token bombTokens) { this.bombTokens = bombTokens; }
    }

    public static class Onslaught {
        private Token tokens;

        public Token getTokens() { return tokens; }
        public void setTokens(Token tokens) { this.tokens = tokens; }
    }

    public static class SalvageRun {
        private Token tokens;

        public Token getTokens() { return tokens; }
        public void setTokens(Token tokens) { this.tokens = tokens; }
    }

    public static class Token {
        private int current;
        private int max;
        private Integer nextTokenInSeconds;
        private int regenDelayInSeconds;

        public int getCurrent() { return current; }
        public void setCurrent(int current) { this.current = current; }
        public int getMax() { return max; }
        public void setMax(int max) { this.max = max; }
        public Integer getNextTokenInSeconds() { return nextTokenInSeconds; }
        public void setNextTokenInSeconds(Integer nextTokenInSeconds) { this.nextTokenInSeconds = nextTokenInSeconds; }
        public int getRegenDelayInSeconds() { return regenDelayInSeconds; }
        public void setRegenDelayInSeconds(int regenDelayInSeconds) { this.regenDelayInSeconds = regenDelayInSeconds; }
    }
}