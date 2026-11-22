package com.codingame.game;

import com.codingame.gameengine.module.entities.Sprite;

public class Beacon {
    private final int x;
    private final int y;
    private int owner = -1; // -1 = neutral, 0 = player 0, 1 = player 1
    private boolean active = true;
    private Sprite sprite;

    public Beacon(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }
}
