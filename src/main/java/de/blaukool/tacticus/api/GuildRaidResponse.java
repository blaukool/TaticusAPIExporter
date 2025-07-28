package de.blaukool.tacticus.api;

import java.time.OffsetDateTime;
import java.util.List;

public class GuildRaidResponse {
    private int season;
    private String seasonConfigId;
    private List<Raid> entries;

    public int getSeason() { return season; }
    public void setSeason(int season) { this.season = season; }
    public String getSeasonConfigId() { return seasonConfigId; }
    public void setSeasonConfigId(String seasonConfigId) { this.seasonConfigId = seasonConfigId; }
    public List<Raid> getEntries() { return entries; }
    public void setEntries(List<Raid> entries) { this.entries = entries; }

    public static class Raid {
        private String userId;
        private int tier;
        private int set;
        private int encounterIndex;
        private int remainingHp;
        private int maxHp;
        private String encounterType;
        private String unitId;
        private String type;
        private String rarity;
        private int damageDealt;
        private String damageType;
        private OffsetDateTime startedOn;
        private OffsetDateTime completedOn;
        private List<PublicHeroDetail> heroDetails;
        private PublicHeroDetail machineOfWarDetails;
        private String globalConfigHash;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public int getTier() { return tier; }
        public void setTier(int tier) { this.tier = tier; }
        public int getSet() { return set; }
        public void setSet(int set) { this.set = set; }
        public int getEncounterIndex() { return encounterIndex; }
        public void setEncounterIndex(int encounterIndex) { this.encounterIndex = encounterIndex; }
        public int getRemainingHp() { return remainingHp; }
        public void setRemainingHp(int remainingHp) { this.remainingHp = remainingHp; }
        public int getMaxHp() { return maxHp; }
        public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
        public String getEncounterType() { return encounterType; }
        public void setEncounterType(String encounterType) { this.encounterType = encounterType; }
        public String getUnitId() { return unitId; }
        public void setUnitId(String unitId) { this.unitId = unitId; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getRarity() { return rarity; }
        public void setRarity(String rarity) { this.rarity = rarity; }
        public int getDamageDealt() { return damageDealt; }
        public void setDamageDealt(int damageDealt) { this.damageDealt = damageDealt; }
        public String getDamageType() { return damageType; }
        public void setDamageType(String damageType) { this.damageType = damageType; }
        public OffsetDateTime getStartedOn() { return startedOn; }
        public void setStartedOn(OffsetDateTime startedOn) { this.startedOn = startedOn; }
        public OffsetDateTime getCompletedOn() { return completedOn; }
        public void setCompletedOn(OffsetDateTime completedOn) { this.completedOn = completedOn; }
        public List<PublicHeroDetail> getHeroDetails() { return heroDetails; }
        public void setHeroDetails(List<PublicHeroDetail> heroDetails) { this.heroDetails = heroDetails; }
        public PublicHeroDetail getMachineOfWarDetails() { return machineOfWarDetails; }
        public void setMachineOfWarDetails(PublicHeroDetail machineOfWarDetails) { this.machineOfWarDetails = machineOfWarDetails; }
        public String getGlobalConfigHash() { return globalConfigHash; }
        public void setGlobalConfigHash(String globalConfigHash) { this.globalConfigHash = globalConfigHash; }
    }

    public static class PublicHeroDetail {
        private String unitId;
        private int power;

        public String getUnitId() { return unitId; }
        public void setUnitId(String unitId) { this.unitId = unitId; }
        public int getPower() { return power; }
        public void setPower(int power) { this.power = power; }
    }
}