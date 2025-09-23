package de.blaukool.tacticus.logic;

import java.io.Serializable;

public class Attack implements Comparable<Attack> {
    private String attackerName;
    private int damage;
    private int remainingHealth;

    public Attack(String attackerName, int damage, int remainingHealth) {
        this.attackerName = attackerName;
        this.damage = damage;
        this.remainingHealth = remainingHealth;
    }

    public String getAttackerName() {
        return attackerName;
    }

    public void setAttackerName(String attackerName) {
        this.attackerName = attackerName;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getRemainingHealth() {
        return remainingHealth;
    }

    public void setRemainingHealth(int remainingHealth) {
        this.remainingHealth = remainingHealth;
    }

    @Override
    public String toString() {
        return "\n\t\t" + attackerName + '\'' +
                ", damage=" + damage +
                ", remainingHealth=" + remainingHealth;
    }

    @Override
    public int compareTo(Attack o) {
        return Integer.compare(o.remainingHealth, this.remainingHealth);
    }
}
