package de.blaukool.tacticus.api;

import java.util.List;

public class Unit {
    private String id;
    private String name;
    private String faction;
    private String grandAlliance;
    private int progressionIndex;
    private int xp;
    private int xpLevel;
    private int rank;
    private List<Ability> abilities;
    private List<Integer> upgrades;
    private List<UnitItem> items;
    private int shards;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getFaction() { return faction; }
    public void setFaction(String faction) { this.faction = faction; }
    public String getGrandAlliance() { return grandAlliance; }
    public void setGrandAlliance(String grandAlliance) { this.grandAlliance = grandAlliance; }
    public int getProgressionIndex() { return progressionIndex; }
    public void setProgressionIndex(int progressionIndex) { this.progressionIndex = progressionIndex; }
    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }
    public int getXpLevel() { return xpLevel; }
    public void setXpLevel(int xpLevel) { this.xpLevel = xpLevel; }
    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
    public List<Ability> getAbilities() { return abilities; }
    public void setAbilities(List<Ability> abilities) { this.abilities = abilities; }
    public List<Integer> getUpgrades() { return upgrades; }
    public void setUpgrades(List<Integer> upgrades) { this.upgrades = upgrades; }
    public List<UnitItem> getItems() { return items; }
    public void setItems(List<UnitItem> items) { this.items = items; }
    public int getShards() { return shards; }
    public void setShards(int shards) { this.shards = shards; }

    public static class Ability {
        private String id;
        private int level;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
    }

    public static class UnitItem {
        private String slotId;
        private int level;
        private String id;
        private String name;
        private String rarity;

        public String getSlotId() { return slotId; }
        public void setSlotId(String slotId) { this.slotId = slotId; }
        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getRarity() { return rarity; }
        public void setRarity(String rarity) { this.rarity = rarity; }
    }
}