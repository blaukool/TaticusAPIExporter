package de.blaukool.tacticus.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class Sideboss {
    private String name;
    private SortedSet<Attack> attacks = new TreeSet<>();

    public Sideboss(String name) {
        this.name = name;
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

    @Override
    public String toString() {
        return "\n\tSideboss{" +
                "name='" + name + '\'' +
                ", attacks=" + attacks +
                '}';
    }
}
