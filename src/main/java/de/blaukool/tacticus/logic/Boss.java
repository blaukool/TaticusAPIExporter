package de.blaukool.tacticus.logic;

import java.util.*;

public class Boss implements Comparable<Boss>{
    private String name;
    private SortedSet<Attack> attacks = new TreeSet<>();
    private List<Sideboss> sideboss = new ArrayList<>();
    private String rarity;
    private int number;

    static private final Map<String, Integer> RARITIES = new HashMap<>();
    static {
        RARITIES.put("Common", 1);
        RARITIES.put("Uncommon", 2);
        RARITIES.put("Rare", 3);
        RARITIES.put("Epic", 4);
        RARITIES.put("Legendary", 5);
        RARITIES.put("Mythic", 6);
    }

    public Boss(String name, int number, String rarity) {
        this.name = name;
        this.number = number;
        this.rarity = rarity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SortedSet<Attack> getAttacks() {
        return attacks;
    }

    public void setAttacks(SortedSet<Attack> attacks) {
        this.attacks = attacks;
    }

    public List<Sideboss> getSideboss() {
        return sideboss;
    }

    public void setSideboss(List<Sideboss> sideboss) {
        this.sideboss = sideboss;
    }

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "\nBoss{" +
                "name='" + name + '\'' +
                ", rarity='" + rarity + '\'' +
                ", number=" + number +
                ", attacks=" + attacks +
                ", sideboss=" + sideboss +
                "'}'";
    }

    @Override
    public int compareTo(Boss o) {
        int rare = Integer.compare(RARITIES.get(this.rarity), RARITIES.get(o.rarity));
        if (rare != 0) return rare;
        return Integer.compare(this.number, o.number);
    }
}
