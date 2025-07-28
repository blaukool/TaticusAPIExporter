package de.blaukool.tacticus.logic;

public class MemberContribution {
    String name;
    Integer damage = 0;

    public Integer getDamage() {
        return damage;
    }

    public void setDamage(Integer damage) {
        this.damage = damage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addDamage(Integer damage) {
        this.damage += damage;
    }
}
