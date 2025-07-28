package de.blaukool.tacticus.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class GuildRaidResponseTest {

    private GuildRaidResponse guildRaidResponse;
    private GuildRaidResponse.Raid raid;
    private GuildRaidResponse.PublicHeroDetail heroDetail;

    @BeforeEach
    public void setUp() {
        guildRaidResponse = new GuildRaidResponse();
        raid = new GuildRaidResponse.Raid();
        heroDetail = new GuildRaidResponse.PublicHeroDetail();
    }

    @Test
    public void testGuildRaidResponseSettersAndGetters() {
        int season = 78;
        String seasonConfigId = "season-78-config";
        
        guildRaidResponse.setSeason(season);
        guildRaidResponse.setSeasonConfigId(seasonConfigId);
        guildRaidResponse.setEntries(Arrays.asList(raid));
        
        assertEquals(season, guildRaidResponse.getSeason());
        assertEquals(seasonConfigId, guildRaidResponse.getSeasonConfigId());
        assertEquals(1, guildRaidResponse.getEntries().size());
        assertEquals(raid, guildRaidResponse.getEntries().get(0));
    }

    @Test
    public void testRaidSettersAndGetters() {
        String userId = "test-user-id";
        int tier = 1;
        int set = 2;
        int encounterIndex = 3;
        int remainingHp = 5000;
        int maxHp = 10000;
        String encounterType = "Boss";
        String unitId = "boss-unit-id";
        String type = "BossType";
        String rarity = "Epic";
        int damageDealt = 5000;
        String damageType = "Battle";
        OffsetDateTime startedOn = OffsetDateTime.now();
        OffsetDateTime completedOn = startedOn.plusMinutes(30);
        String globalConfigHash = "config-hash-123";
        
        raid.setUserId(userId);
        raid.setTier(tier);
        raid.setSet(set);
        raid.setEncounterIndex(encounterIndex);
        raid.setRemainingHp(remainingHp);
        raid.setMaxHp(maxHp);
        raid.setEncounterType(encounterType);
        raid.setUnitId(unitId);
        raid.setType(type);
        raid.setRarity(rarity);
        raid.setDamageDealt(damageDealt);
        raid.setDamageType(damageType);
        raid.setStartedOn(startedOn);
        raid.setCompletedOn(completedOn);
        raid.setHeroDetails(Arrays.asList(heroDetail));
        raid.setMachineOfWarDetails(heroDetail);
        raid.setGlobalConfigHash(globalConfigHash);
        
        assertEquals(userId, raid.getUserId());
        assertEquals(tier, raid.getTier());
        assertEquals(set, raid.getSet());
        assertEquals(encounterIndex, raid.getEncounterIndex());
        assertEquals(remainingHp, raid.getRemainingHp());
        assertEquals(maxHp, raid.getMaxHp());
        assertEquals(encounterType, raid.getEncounterType());
        assertEquals(unitId, raid.getUnitId());
        assertEquals(type, raid.getType());
        assertEquals(rarity, raid.getRarity());
        assertEquals(damageDealt, raid.getDamageDealt());
        assertEquals(damageType, raid.getDamageType());
        assertEquals(startedOn, raid.getStartedOn());
        assertEquals(completedOn, raid.getCompletedOn());
        assertEquals(1, raid.getHeroDetails().size());
        assertEquals(heroDetail, raid.getHeroDetails().get(0));
        assertEquals(heroDetail, raid.getMachineOfWarDetails());
        assertEquals(globalConfigHash, raid.getGlobalConfigHash());
    }

    @Test
    public void testPublicHeroDetailSettersAndGetters() {
        String unitId = "hero-unit-id";
        int power = 7500;
        
        heroDetail.setUnitId(unitId);
        heroDetail.setPower(power);
        
        assertEquals(unitId, heroDetail.getUnitId());
        assertEquals(power, heroDetail.getPower());
    }

    @Test
    public void testEncounterTypes() {
        // Test valid encounter types
        String[] encounterTypes = {"Boss", "SideBoss"};
        
        for (String encounterType : encounterTypes) {
            raid.setEncounterType(encounterType);
            assertEquals(encounterType, raid.getEncounterType());
        }
    }

    @Test
    public void testRarityTypes() {
        // Test valid rarity types
        String[] rarities = {"Common", "Uncommon", "Rare", "Epic", "Legendary"};
        
        for (String rarity : rarities) {
            raid.setRarity(rarity);
            assertEquals(rarity, raid.getRarity());
        }
    }

    @Test
    public void testDamageTypes() {
        // Test valid damage types
        String[] damageTypes = {"Battle", "Bomb"};
        
        for (String damageType : damageTypes) {
            raid.setDamageType(damageType);
            assertEquals(damageType, raid.getDamageType());
        }
    }

    @Test
    public void testDamageCalculation() {
        // Test damage calculations
        int maxHp = 10000;
        int remainingHp = 3000;
        int expectedDamage = maxHp - remainingHp;
        
        raid.setMaxHp(maxHp);
        raid.setRemainingHp(remainingHp);
        raid.setDamageDealt(expectedDamage);
        
        assertEquals(expectedDamage, raid.getDamageDealt());
        assertEquals(maxHp, raid.getMaxHp());
        assertEquals(remainingHp, raid.getRemainingHp());
    }

    @Test
    public void testZeroDamage() {
        // Test case where no damage was dealt
        raid.setDamageDealt(0);
        raid.setMaxHp(10000);
        raid.setRemainingHp(10000);
        
        assertEquals(0, raid.getDamageDealt());
        assertEquals(10000, raid.getMaxHp());
        assertEquals(10000, raid.getRemainingHp());
    }

    @Test
    public void testMaxDamage() {
        // Test case where boss was killed (max damage)
        int maxHp = 15000;
        raid.setMaxHp(maxHp);
        raid.setRemainingHp(0);
        raid.setDamageDealt(maxHp);
        
        assertEquals(maxHp, raid.getDamageDealt());
        assertEquals(0, raid.getRemainingHp());
    }

    @Test
    public void testNullValues() {
        // Test that null values are handled correctly
        guildRaidResponse.setSeasonConfigId(null);
        guildRaidResponse.setEntries(null);
        assertNull(guildRaidResponse.getSeasonConfigId());
        assertNull(guildRaidResponse.getEntries());
        
        raid.setUserId(null);
        raid.setEncounterType(null);
        raid.setUnitId(null);
        raid.setType(null);
        raid.setRarity(null);
        raid.setDamageType(null);
        raid.setStartedOn(null);
        raid.setCompletedOn(null);
        raid.setHeroDetails(null);
        raid.setMachineOfWarDetails(null);
        raid.setGlobalConfigHash(null);
        
        assertNull(raid.getUserId());
        assertNull(raid.getEncounterType());
        assertNull(raid.getUnitId());
        assertNull(raid.getType());
        assertNull(raid.getRarity());
        assertNull(raid.getDamageType());
        assertNull(raid.getStartedOn());
        assertNull(raid.getCompletedOn());
        assertNull(raid.getHeroDetails());
        assertNull(raid.getMachineOfWarDetails());
        assertNull(raid.getGlobalConfigHash());
        
        heroDetail.setUnitId(null);
        assertNull(heroDetail.getUnitId());
    }

    @Test
    public void testEmptyCollections() {
        // Test with empty collections
        guildRaidResponse.setEntries(Collections.emptyList());
        assertTrue(guildRaidResponse.getEntries().isEmpty());
        
        raid.setHeroDetails(Collections.emptyList());
        assertTrue(raid.getHeroDetails().isEmpty());
    }

    @Test
    public void testMultipleHeroDetails() {
        // Test with multiple hero details
        GuildRaidResponse.PublicHeroDetail hero1 = new GuildRaidResponse.PublicHeroDetail();
        hero1.setUnitId("hero1");
        hero1.setPower(5000);
        
        GuildRaidResponse.PublicHeroDetail hero2 = new GuildRaidResponse.PublicHeroDetail();
        hero2.setUnitId("hero2");
        hero2.setPower(6000);
        
        raid.setHeroDetails(Arrays.asList(hero1, hero2));
        
        assertEquals(2, raid.getHeroDetails().size());
        assertEquals("hero1", raid.getHeroDetails().get(0).getUnitId());
        assertEquals("hero2", raid.getHeroDetails().get(1).getUnitId());
        assertEquals(5000, raid.getHeroDetails().get(0).getPower());
        assertEquals(6000, raid.getHeroDetails().get(1).getPower());
    }

    @Test
    public void testSeasonBoundaries() {
        // Test various season values
        int[] seasons = {1, 50, 78, 100};
        
        for (int season : seasons) {
            guildRaidResponse.setSeason(season);
            assertEquals(season, guildRaidResponse.getSeason());
        }
    }

    @Test
    public void testNegativeValues() {
        // Test with negative values (edge cases)
        raid.setTier(-1);
        raid.setSet(-1);
        raid.setEncounterIndex(-1);
        raid.setRemainingHp(-100);
        raid.setMaxHp(-500);
        raid.setDamageDealt(-1000);
        
        assertEquals(-1, raid.getTier());
        assertEquals(-1, raid.getSet());
        assertEquals(-1, raid.getEncounterIndex());
        assertEquals(-100, raid.getRemainingHp());
        assertEquals(-500, raid.getMaxHp());
        assertEquals(-1000, raid.getDamageDealt());
        
        heroDetail.setPower(-2000);
        assertEquals(-2000, heroDetail.getPower());
    }

    @Test
    public void testTimestampHandling() {
        // Test timestamp handling
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime past = now.minusHours(2);
        OffsetDateTime future = now.plusHours(1);
        
        raid.setStartedOn(past);
        raid.setCompletedOn(now);
        
        assertEquals(past, raid.getStartedOn());
        assertEquals(now, raid.getCompletedOn());
        assertTrue(raid.getStartedOn().isBefore(raid.getCompletedOn()));
        
        // Test with future timestamp
        raid.setCompletedOn(future);
        assertTrue(raid.getCompletedOn().isAfter(raid.getStartedOn()));
    }

    @Test
    public void testCompleteRaidStructure() {
        // Test a complete raid entry structure
        guildRaidResponse.setSeason(78);
        guildRaidResponse.setSeasonConfigId("season-78-complete");
        
        GuildRaidResponse.Raid completeRaid = new GuildRaidResponse.Raid();
        completeRaid.setUserId("complete-user-id");
        completeRaid.setTier(2);
        completeRaid.setSet(3);
        completeRaid.setEncounterIndex(5);
        completeRaid.setRemainingHp(2500);
        completeRaid.setMaxHp(20000);
        completeRaid.setEncounterType("Boss");
        completeRaid.setUnitId("complete-boss-id");
        completeRaid.setType("CompleteBoss");
        completeRaid.setRarity("Legendary");
        completeRaid.setDamageDealt(17500);
        completeRaid.setDamageType("Battle");
        completeRaid.setStartedOn(OffsetDateTime.now().minusHours(1));
        completeRaid.setCompletedOn(OffsetDateTime.now());
        completeRaid.setGlobalConfigHash("complete-config-hash");
        
        GuildRaidResponse.PublicHeroDetail hero1 = new GuildRaidResponse.PublicHeroDetail();
        hero1.setUnitId("hero-1");
        hero1.setPower(8500);
        
        GuildRaidResponse.PublicHeroDetail hero2 = new GuildRaidResponse.PublicHeroDetail();
        hero2.setUnitId("hero-2");
        hero2.setPower(9200);
        
        completeRaid.setHeroDetails(Arrays.asList(hero1, hero2));
        completeRaid.setMachineOfWarDetails(hero1);
        
        guildRaidResponse.setEntries(Arrays.asList(completeRaid));
        
        // Verify complete structure
        assertEquals(78, guildRaidResponse.getSeason());
        assertEquals("season-78-complete", guildRaidResponse.getSeasonConfigId());
        assertEquals(1, guildRaidResponse.getEntries().size());
        
        GuildRaidResponse.Raid retrievedRaid = guildRaidResponse.getEntries().get(0);
        assertEquals("complete-user-id", retrievedRaid.getUserId());
        assertEquals(17500, retrievedRaid.getDamageDealt());
        assertEquals(2, retrievedRaid.getHeroDetails().size());
        assertEquals("hero-1", retrievedRaid.getHeroDetails().get(0).getUnitId());
        assertNotNull(retrievedRaid.getMachineOfWarDetails());
    }
}