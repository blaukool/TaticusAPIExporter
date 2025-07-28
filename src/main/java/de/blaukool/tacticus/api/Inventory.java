package de.blaukool.tacticus.api;

import java.util.List;
import java.util.Map;

public class Inventory {
    private List<Item> items;
    private List<Upgrade> upgrades;
    private List<Shard> shards;
    private List<XpBook> xpBooks;
    private Map<String, List<AbilityBadge>> abilityBadges;
    private List<Component> components;
    private List<ForgeBadge> forgeBadges;
    private Map<String, List<Orb>> orbs;
    private RequisitionOrders requisitionOrders;
    private int resetStones;

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }
    public List<Upgrade> getUpgrades() { return upgrades; }
    public void setUpgrades(List<Upgrade> upgrades) { this.upgrades = upgrades; }
    public List<Shard> getShards() { return shards; }
    public void setShards(List<Shard> shards) { this.shards = shards; }
    public List<XpBook> getXpBooks() { return xpBooks; }
    public void setXpBooks(List<XpBook> xpBooks) { this.xpBooks = xpBooks; }
    public Map<String, List<AbilityBadge>> getAbilityBadges() { return abilityBadges; }
    public void setAbilityBadges(Map<String, List<AbilityBadge>> abilityBadges) { this.abilityBadges = abilityBadges; }
    public List<Component> getComponents() { return components; }
    public void setComponents(List<Component> components) { this.components = components; }
    public List<ForgeBadge> getForgeBadges() { return forgeBadges; }
    public void setForgeBadges(List<ForgeBadge> forgeBadges) { this.forgeBadges = forgeBadges; }
    public Map<String, List<Orb>> getOrbs() { return orbs; }
    public void setOrbs(Map<String, List<Orb>> orbs) { this.orbs = orbs; }
    public RequisitionOrders getRequisitionOrders() { return requisitionOrders; }
    public void setRequisitionOrders(RequisitionOrders requisitionOrders) { this.requisitionOrders = requisitionOrders; }
    public int getResetStones() { return resetStones; }
    public void setResetStones(int resetStones) { this.resetStones = resetStones; }

    public static class Item {
        private String id;
        private String name;
        private int level;
        private int amount;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
        public int getAmount() { return amount; }
        public void setAmount(int amount) { this.amount = amount; }
    }

    public static class Upgrade {
        private String id;
        private String name;
        private int amount;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAmount() { return amount; }
        public void setAmount(int amount) { this.amount = amount; }
    }

    public static class Shard {
        private String id;
        private String name;
        private int amount;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAmount() { return amount; }
        public void setAmount(int amount) { this.amount = amount; }
    }

    public static class XpBook {
        private String id;
        private String rarity;
        private int amount;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getRarity() { return rarity; }
        public void setRarity(String rarity) { this.rarity = rarity; }
        public int getAmount() { return amount; }
        public void setAmount(int amount) { this.amount = amount; }
    }

    public static class AbilityBadge {
        private String name;
        private String rarity;
        private int amount;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getRarity() { return rarity; }
        public void setRarity(String rarity) { this.rarity = rarity; }
        public int getAmount() { return amount; }
        public void setAmount(int amount) { this.amount = amount; }
    }

    public static class Component {
        private String name;
        private String grandAlliance;
        private int amount;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getGrandAlliance() { return grandAlliance; }
        public void setGrandAlliance(String grandAlliance) { this.grandAlliance = grandAlliance; }
        public int getAmount() { return amount; }
        public void setAmount(int amount) { this.amount = amount; }
    }

    public static class ForgeBadge {
        private String name;
        private String rarity;
        private int amount;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getRarity() { return rarity; }
        public void setRarity(String rarity) { this.rarity = rarity; }
        public int getAmount() { return amount; }
        public void setAmount(int amount) { this.amount = amount; }
    }

    public static class Orb {
        private String rarity;
        private int amount;

        public String getRarity() { return rarity; }
        public void setRarity(String rarity) { this.rarity = rarity; }
        public int getAmount() { return amount; }
        public void setAmount(int amount) { this.amount = amount; }
    }

    public static class RequisitionOrders {
        private int regular;
        private int blessed;

        public int getRegular() { return regular; }
        public void setRegular(int regular) { this.regular = regular; }
        public int getBlessed() { return blessed; }
        public void setBlessed(int blessed) { this.blessed = blessed; }
    }
}