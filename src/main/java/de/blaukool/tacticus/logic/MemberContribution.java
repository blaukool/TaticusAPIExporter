package de.blaukool.tacticus.logic;

public class MemberContribution {
    String name;
    String role;
    Integer bossBattle = 0;
    Integer sidebossBattle = 0;
    Integer bossBomb = 0;
    Integer sidebossBomb = 0;
    Integer battleCount = 0;
    Integer bombCount = 0;

    public Integer getBossBattle() {
        return bossBattle;
    }

    public void setBossBattle(Integer bossBattle) {
        this.bossBattle = bossBattle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getSidebossBattle() {
        return sidebossBattle;
    }

    public void setSidebossBattle(Integer sidebossBattle) {
        this.sidebossBattle = sidebossBattle;
    }

    public Integer getBossBomb() {
        return bossBomb;
    }

    public void setBossBomb(Integer bossBomb) {
        this.bossBomb = bossBomb;
    }

    public Integer getSidebossBomb() {
        return sidebossBomb;
    }

    public void setSidebossBomb(Integer sidebossBomb) {
        this.sidebossBomb = sidebossBomb;
    }

    public void addBossBattle(Integer damage) {
        this.bossBattle += damage;
    }

    public void addBossBomb(Integer damage) {
        this.bossBomb += damage;
    }

    public void addSidebossBattle(Integer damage) {
        this.sidebossBattle += damage;
    }

    public void addSidebossBomb(Integer damage) {
        this.sidebossBomb += damage;
    }

    public Integer getBattleCount() {
        return battleCount;
    }

    public void setBattleCount(Integer battleCount) {
        this.battleCount = battleCount;
    }

    public Integer getBombCount() {
        return bombCount;
    }

    public void setBombCount(Integer bombCount) {
        this.bombCount = bombCount;
    }

    public void incrementBattleCount() {
        this.battleCount++;
    }

    public void incrementBombCount() {
        this.bombCount++;
    }

}
