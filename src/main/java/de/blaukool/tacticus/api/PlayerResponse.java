package de.blaukool.tacticus.api;

import java.util.List;

public class PlayerResponse {
    private Player player;
    private PlayerMetaData metaData;

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
    public PlayerMetaData getMetaData() { return metaData; }
    public void setMetaData(PlayerMetaData metaData) { this.metaData = metaData; }

    public static class Player {
        private PlayerDetails details;
        private List<Unit> units;
        private Inventory inventory;
        private Progress progress;

        public PlayerDetails getDetails() { return details; }
        public void setDetails(PlayerDetails details) { this.details = details; }
        public List<Unit> getUnits() { return units; }
        public void setUnits(List<Unit> units) { this.units = units; }
        public Inventory getInventory() { return inventory; }
        public void setInventory(Inventory inventory) { this.inventory = inventory; }
        public Progress getProgress() { return progress; }
        public void setProgress(Progress progress) { this.progress = progress; }
    }

    public static class PlayerDetails {
        private String name;
        private int powerLevel;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getPowerLevel() { return powerLevel; }
        public void setPowerLevel(int powerLevel) { this.powerLevel = powerLevel; }
    }

    public static class PlayerMetaData {
        private String configHash;
        private Long apiKeyExpiresOn;
        private long lastUpdatedOn;
        private List<String> scopes;

        public String getConfigHash() { return configHash; }
        public void setConfigHash(String configHash) { this.configHash = configHash; }
        public Long getApiKeyExpiresOn() { return apiKeyExpiresOn; }
        public void setApiKeyExpiresOn(Long apiKeyExpiresOn) { this.apiKeyExpiresOn = apiKeyExpiresOn; }
        public long getLastUpdatedOn() { return lastUpdatedOn; }
        public void setLastUpdatedOn(long lastUpdatedOn) { this.lastUpdatedOn = lastUpdatedOn; }
        public List<String> getScopes() { return scopes; }
        public void setScopes(List<String> scopes) { this.scopes = scopes; }
    }
}