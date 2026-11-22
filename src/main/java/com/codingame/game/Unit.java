package com.codingame.game;

import com.codingame.gameengine.module.entities.Group;
import com.codingame.gameengine.module.entities.Sprite;
import com.codingame.gameengine.module.entities.Text;

public class Unit {
    public enum ActionType {
        NONE, BOMB, PUSH
    }

    private final int id;
    private final Player owner;
    private int x;
    private int y;
    private int cooldown;
    private boolean alive = true;

    // Planned turn commands
    private int moveTargetX;
    private int moveTargetY;
    private ActionType actionType = ActionType.NONE;
    private int actionTargetX;
    private int actionTargetY;
    private String message = "";

    // Graphics
    private Group group;
    private Sprite sprite;
    private Text label;
    private Text messageText;

    public Unit(int id, Player owner, int x, int y) {
        this.id = id;
        this.owner = owner;
        this.x = x;
        this.y = y;
        this.moveTargetX = x;
        this.moveTargetY = y;
    }

    public void resetTurnCommand() {
        this.moveTargetX = this.x;
        this.moveTargetY = this.y;
        this.actionType = ActionType.NONE;
        this.actionTargetX = -1;
        this.actionTargetY = -1;
        this.message = "";
    }

    public int getId() {
        return id;
    }

    public Player getOwner() {
        return owner;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = Math.max(0, cooldown);
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public int getMoveTargetX() {
        return moveTargetX;
    }

    public int getMoveTargetY() {
        return moveTargetY;
    }

    public void setMoveTarget(int tx, int ty) {
        this.moveTargetX = tx;
        this.moveTargetY = ty;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public int getActionTargetX() {
        return actionTargetX;
    }

    public int getActionTargetY() {
        return actionTargetY;
    }

    public void setAction(ActionType type, int tx, int ty) {
        this.actionType = type;
        this.actionTargetX = tx;
        this.actionTargetY = ty;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        if (message != null && message.length() > 18) {
            this.message = message.substring(0, 18);
        } else {
            this.message = message == null ? "" : message;
        }
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }

    public Text getLabel() {
        return label;
    }

    public void setLabel(Text label) {
        this.label = label;
    }

    public Text getMessageText() {
        return messageText;
    }

    public void setMessageText(Text messageText) {
        this.messageText = messageText;
    }
}
