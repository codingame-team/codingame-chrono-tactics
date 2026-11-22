package com.codingame.game;

import java.util.ArrayList;
import java.util.List;
import com.codingame.gameengine.core.AbstractMultiplayerPlayer;
import com.codingame.gameengine.module.entities.Text;

public class Player extends AbstractMultiplayerPlayer {
    private final List<Unit> units = new ArrayList<>();
    private Text scoreText;
    private Text statusText;

    @Override
    public int getExpectedOutputLines() {
        int aliveCount = 0;
        for (Unit u : units) {
            if (u.isAlive()) {
                aliveCount++;
            }
        }
        return Math.max(1, aliveCount);
    }

    public List<Unit> getUnits() {
        return units;
    }

    public List<Unit> getAliveUnits() {
        List<Unit> alive = new ArrayList<>();
        for (Unit u : units) {
            if (u.isAlive()) {
                alive.add(u);
            }
        }
        return alive;
    }

    public void addUnit(Unit unit) {
        units.add(unit);
    }

    public Text getScoreText() {
        return scoreText;
    }

    public void setScoreText(Text scoreText) {
        this.scoreText = scoreText;
    }

    public Text getStatusText() {
        return statusText;
    }

    public void setStatusText(Text statusText) {
        this.statusText = statusText;
    }
}
