package com.codingame.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.core.MultiplayerGameManager;
import com.codingame.gameengine.module.endscreen.EndScreenModule;
import com.codingame.gameengine.module.entities.Curve;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Group;
import com.codingame.gameengine.module.entities.Rectangle;
import com.codingame.gameengine.module.entities.Sprite;
import com.codingame.gameengine.module.entities.Text;
import com.codingame.gameengine.module.toggle.ToggleModule;
import com.codingame.gameengine.module.tooltip.TooltipModule;
import com.google.inject.Inject;

public class Referee extends AbstractReferee {
    private static final int MAX_TURNS = 120;
    private static final int CELL_SIZE = 76;
    private static final int BOARD_ORIGIN_X = (1920 - Grid.WIDTH * CELL_SIZE) / 2;
    private static final int BOARD_ORIGIN_Y = (1080 - Grid.HEIGHT * CELL_SIZE) / 2 + 25;
    private static final int ACTION_COOLDOWN = 2;
    private static final int BOMB_RANGE = 2;

    @Inject private MultiplayerGameManager<Player> gameManager;
    @Inject private GraphicEntityModule graphicEntityModule;
    @Inject private TooltipModule tooltipModule;
    @Inject private EndScreenModule endScreenModule;
    @Inject private ToggleModule toggleModule;

    private final Grid grid = new Grid();
    private final List<Unit> allUnits = new ArrayList<>();
    private Sprite[][] tileSprites;
    private Text[][] tileOverlayTexts;
    private Text turnText;
    private Sprite fxExplosionPool;
    private Sprite fxPushPool;
    private int leagueLevel;
    private int collapseRing = 5;

    @Override
    public void init() {
        Random rng = gameManager.getRandom();
        leagueLevel = Math.max(1, gameManager.getLeagueLevel());

        gameManager.setMaxTurns(MAX_TURNS);
        gameManager.setFirstTurnMaxTime(1000);
        gameManager.setTurnMaxTime(50);
        gameManager.setFrameDuration(550);

        List<int[]> p0Spawns = new ArrayList<>();
        List<int[]> p1Spawns = new ArrayList<>();
        grid.generate(rng, leagueLevel, p0Spawns, p1Spawns);

        Player p0 = gameManager.getPlayer(0);
        Player p1 = gameManager.getPlayer(1);

        int unitId = 0;
        for (int[] pos : p0Spawns) {
            Unit u = new Unit(unitId++, p0, pos[0], pos[1]);
            p0.addUnit(u);
            allUnits.add(u);
        }
        for (int[] pos : p1Spawns) {
            Unit u = new Unit(unitId++, p1, pos[0], pos[1]);
            p1.addUnit(u);
            allUnits.add(u);
        }

        initGraphics();

        // Send initialization input to players (3=Wood 3, 2=Wood 2, 1=Wood 1)
        int woodDivision = Math.max(1, 4 - leagueLevel);
        for (Player player : gameManager.getActivePlayers()) {
            player.sendInputLine(String.format("%d %d %d", Grid.WIDTH, Grid.HEIGHT, woodDivision));
        }
    }

    private int cellCenterX(int x) {
        return BOARD_ORIGIN_X + x * CELL_SIZE + CELL_SIZE / 2;
    }

    private int cellCenterY(int y) {
        return BOARD_ORIGIN_Y + y * CELL_SIZE + CELL_SIZE / 2;
    }

    private void initGraphics() {
        // Background
        graphicEntityModule.createSprite()
            .setImage("background.jpg")
            .setBaseWidth(1920)
            .setBaseHeight(1080)
            .setZIndex(-10);

        // Dark arena backdrop
        graphicEntityModule.createRectangle()
            .setX(BOARD_ORIGIN_X - 16)
            .setY(BOARD_ORIGIN_Y - 16)
            .setWidth(Grid.WIDTH * CELL_SIZE + 32)
            .setHeight(Grid.HEIGHT * CELL_SIZE + 32)
            .setFillColor(0x111622)
            .setAlpha(0.82)
            .setLineWidth(4)
            .setLineColor(0x3a4a6b)
            .setZIndex(-5);

        // Title & Turn Counter (Font size >= 36px per guidelines)
        graphicEntityModule.createText("CHRONO-TACTICS")
            .setX(960)
            .setY(42)
            .setAnchor(0.5)
            .setFontFamily("Lato")
            .setFontSize(44)
            .setFillColor(0xffffff)
            .setStrokeThickness(4)
            .setStrokeColor(0x000000)
            .setZIndex(20);

        turnText = graphicEntityModule.createText("TURN 0 / " + MAX_TURNS)
            .setX(960)
            .setY(92)
            .setAnchor(0.5)
            .setFontFamily("Lato")
            .setFontSize(36)
            .setFillColor(0xffffff)
            .setStrokeThickness(4)
            .setStrokeColor(0x000000)
            .setZIndex(20);

        // Player HUD panels
        for (Player p : gameManager.getPlayers()) {
            int idx = p.getIndex();
            int hudX = idx == 0 ? 220 : 1700;

            Rectangle panel = graphicEntityModule.createRectangle()
                .setX(hudX - 180)
                .setY(180)
                .setWidth(360)
                .setHeight(320)
                .setFillColor(0x182030)
                .setAlpha(0.88)
                .setLineWidth(4)
                .setLineColor(p.getColorToken())
                .setZIndex(10);

            graphicEntityModule.createSprite()
                .setImage(p.getAvatarToken())
                .setX(hudX)
                .setY(250)
                .setAnchor(0.5)
                .setBaseWidth(88)
                .setBaseHeight(88)
                .setZIndex(12);

            graphicEntityModule.createText(p.getNicknameToken())
                .setX(hudX)
                .setY(325)
                .setAnchor(0.5)
                .setFontFamily("Lato")
                .setFontSize(36)
                .setFillColor(p.getColorToken())
                .setZIndex(12);

            Text scoreTxt = graphicEntityModule.createText("SCORE: 0")
                .setX(hudX)
                .setY(390)
                .setAnchor(0.5)
                .setFontFamily("Lato")
                .setFontSize(40)
                .setFillColor(0xffffff)
                .setZIndex(12);
            p.setScoreText(scoreTxt);

            Text statusTxt = graphicEntityModule.createText("UNITS: " + p.getAliveUnits().size())
                .setX(hudX)
                .setY(445)
                .setAnchor(0.5)
                .setFontFamily("Lato")
                .setFontSize(36)
                .setFillColor(0xaad4ff)
                .setZIndex(12);
            p.setStatusText(statusTxt);
        }

        // Board Tiles (< 130 sprites)
        tileSprites = new Sprite[Grid.HEIGHT][Grid.WIDTH];
        tileOverlayTexts = new Text[Grid.HEIGHT][Grid.WIDTH];

        for (int y = 0; y < Grid.HEIGHT; y++) {
            for (int x = 0; x < Grid.WIDTH; x++) {
                int cx = cellCenterX(x);
                int cy = cellCenterY(y);

                Sprite s = graphicEntityModule.createSprite()
                    .setX(cx)
                    .setY(cy)
                    .setAnchor(0.5)
                    .setBaseWidth(CELL_SIZE - 4)
                    .setBaseHeight(CELL_SIZE - 4)
                    .setZIndex(1);
                tileSprites[y][x] = s;
                updateTileSprite(x, y);
            }
        }

        // Beacons
        for (Beacon b : grid.getBeacons()) {
            Sprite bs = graphicEntityModule.createSprite()
                .setImage("beacon_neutral.png")
                .setX(cellCenterX(b.getX()))
                .setY(cellCenterY(b.getY()))
                .setAnchor(0.5)
                .setBaseWidth(44)
                .setBaseHeight(44)
                .setZIndex(4);
            b.setSprite(bs);
            tooltipModule.setTooltipText(bs, String.format("Beacon (%d, %d)\nOwner: Neutral", b.getX(), b.getY()));
        }

        // Units
        for (Unit u : allUnits) {
            String img = u.getOwner().getIndex() == 0 ? "unit_p0.png" : "unit_p1.png";
            Sprite us = graphicEntityModule.createSprite()
                .setImage(img)
                .setAnchor(0.5)
                .setBaseWidth(54)
                .setBaseHeight(54);

            Text lbl = graphicEntityModule.createText(String.valueOf(u.getId()))
                .setAnchor(0.5)
                .setFontFamily("Lato")
                .setFontSize(36)
                .setFillColor(0xffffff)
                .setStrokeThickness(3)
                .setStrokeColor(0x000000);
            toggleModule.displayOnToggleState(lbl, "debugOverlay", true);

            Group g = graphicEntityModule.createGroup(us, lbl)
                .setX(cellCenterX(u.getX()))
                .setY(cellCenterY(u.getY()))
                .setZIndex(8);

            u.setSprite(us);
            u.setLabel(lbl);
            u.setGroup(g);
            updateUnitTooltip(u);
        }

        // Reusable FX sprites (to avoid creating entities during turns!)
        fxExplosionPool = graphicEntityModule.createSprite()
            .setImage("explosion.png")
            .setAnchor(0.5)
            .setBaseWidth(72)
            .setBaseHeight(72)
            .setVisible(false)
            .setZIndex(15);

        fxPushPool = graphicEntityModule.createSprite()
            .setImage("push_fx.png")
            .setAnchor(0.5)
            .setBaseWidth(60)
            .setBaseHeight(60)
            .setVisible(false)
            .setZIndex(14);
    }

    private void updateTileSprite(int x, int y) {
        int val = grid.getTile(x, y);
        Sprite s = tileSprites[y][x];
        if (val == Grid.VOID) {
            s.setVisible(false);
            tooltipModule.setTooltipText(s, String.format("(%d, %d) VOID", x, y));
        } else if (val == Grid.PILLAR) {
            s.setVisible(true)
                .setImage("pillar.png")
                .setAlpha(1.0);
            tooltipModule.setTooltipText(s, String.format("(%d, %d) Indestructible Pillar (#)", x, y));
        } else {
            s.setVisible(true);
            if (val == 3) {
                s.setImage("tile3.png").setAlpha(1.0);
            } else if (val == 2) {
                s.setImage("tile2.png").setAlpha(0.95);
            } else {
                s.setImage("tile1.png").setAlpha(0.85);
            }
            tooltipModule.setTooltipText(s, String.format("(%d, %d) Integrity: %d", x, y, val));
        }
    }

    private void updateUnitTooltip(Unit u) {
        if (!u.isAlive()) {
            tooltipModule.setTooltipText(u.getGroup(), "Unit " + u.getId() + " (Eliminated)");
            return;
        }
        tooltipModule.setTooltipText(
            u.getGroup(),
            String.format("Unit %d (Player %d)\nPos: (%d, %d)\nCooldown: %d",
                u.getId(), u.getOwner().getIndex(), u.getX(), u.getY(), u.getCooldown())
        );
    }

    @Override
    public void gameTurn(int turn) {
        turnText.setText("TURN " + turn + " / " + MAX_TURNS);
        fxExplosionPool.setVisible(false);
        fxPushPool.setVisible(false);

        // Decrement cooldowns and reset turn commands
        for (Unit u : allUnits) {
            if (u.isAlive()) {
                u.setCooldown(u.getCooldown() - 1);
                u.resetTurnCommand();
            }
        }

        // 1. Send inputs to each active player (Owner is ALWAYS 0 for self, 1 for opponent per guidelines!)
        for (Player player : gameManager.getActivePlayers()) {
            sendTurnInput(player, turn);
            player.execute();
        }

        // 2. Read and validate player outputs
        for (Player player : gameManager.getActivePlayers()) {
            try {
                List<String> outputs = player.getOutputs();
                parsePlayerOutputs(player, outputs);
            } catch (TimeoutException e) {
                disqualifyPlayer(player, "timed out!");
            }
        }

        if (gameManager.getActivePlayers().size() < 2) {
            gameManager.endGame();
            return;
        }

        // 3. Resolve Simultaneous Movement
        resolveMovement();
        graphicEntityModule.commitWorldState(0.45);

        // 4. Resolve Simultaneous Actions (PUSH then BOMB)
        resolvePushes();
        resolveBombs();

        // 5. Resolve Periodic Arena Ring Collapse in League 3+
        if (leagueLevel >= 3 && turn > 0 && turn % 20 == 0 && collapseRing >= 2) {
            resolveRingCollapse();
        }

        // 6. Capture Beacons & Update Scores
        updateBeaconsAndScores();

        // 7. Update Visuals & Check End Conditions
        refreshGraphics();
        checkEarlyEnd(turn);
    }

    private void sendTurnInput(Player player, int turn) {
        int myIdx = player.getIndex();
        player.sendInputLine(String.valueOf(turn));
        // Grid rows
        for (int y = 0; y < Grid.HEIGHT; y++) {
            player.sendInputLine(grid.getRowString(y));
        }

        // Alive units (sorted so player's own units come first with owner=0)
        List<Unit> orderedUnits = new ArrayList<>();
        for (Unit u : allUnits) {
            if (u.isAlive() && u.getOwner().getIndex() == myIdx) {
                orderedUnits.add(u);
            }
        }
        for (Unit u : allUnits) {
            if (u.isAlive() && u.getOwner().getIndex() != myIdx) {
                orderedUnits.add(u);
            }
        }

        player.sendInputLine(String.valueOf(orderedUnits.size()));
        for (Unit u : orderedUnits) {
            int relativeOwner = (u.getOwner().getIndex() == myIdx) ? 0 : 1;
            player.sendInputLine(String.format("%d %d %d %d %d",
                u.getId(), relativeOwner, u.getX(), u.getY(), u.getCooldown()));
        }

        // Active beacons
        List<Beacon> activeBeacons = new ArrayList<>();
        for (Beacon b : grid.getBeacons()) {
            if (b.isActive()) {
                activeBeacons.add(b);
            }
        }
        player.sendInputLine(String.valueOf(activeBeacons.size()));
        for (Beacon b : activeBeacons) {
            int relativeOwner = -1;
            if (b.getOwner() != -1) {
                relativeOwner = (b.getOwner() == myIdx) ? 0 : 1;
            }
            player.sendInputLine(String.format("%d %d %d", b.getX(), b.getY(), relativeOwner));
        }
    }

    private void parsePlayerOutputs(Player player, List<String> outputs) {
        List<Unit> myAliveUnits = player.getAliveUnits();
        for (int i = 0; i < myAliveUnits.size() && i < outputs.size(); i++) {
            Unit unit = myAliveUnits.get(i);
            String line = outputs.get(i).trim();
            if (line.isEmpty()) {
                disqualifyPlayer(player, "sent an empty command line!");
                return;
            }

            String[] tokens = line.split("\\s+");
            int cursor = 0;

            try {
                // Primary command: MOVE x y or WAIT
                String cmd = tokens[cursor++].toUpperCase();
                if ("MOVE".equals(cmd)) {
                    if (cursor + 1 >= tokens.length) {
                        disqualifyPlayer(player, "invalid MOVE syntax: " + line);
                        return;
                    }
                    int mx = Integer.parseInt(tokens[cursor++]);
                    int my = Integer.parseInt(tokens[cursor++]);
                    if (grid.inBounds(mx, my)) {
                        unit.setMoveTarget(mx, my);
                    } else {
                        gameManager.addToGameSummary(GameManager.formatErrorMessage(
                            player.getNicknameToken() + ": unit " + unit.getId() + " MOVE out of bounds ignored."));
                    }
                } else if ("WAIT".equals(cmd)) {
                    unit.setMoveTarget(unit.getX(), unit.getY());
                } else {
                    disqualifyPlayer(player, "unrecognized command '" + cmd + "'");
                    return;
                }

                // Optional secondary action: BOMB tx ty | PUSH tx ty | WAIT | MSG text
                if (cursor < tokens.length) {
                    String act = tokens[cursor++].toUpperCase();
                    if ("BOMB".equals(act)) {
                        if (cursor + 1 >= tokens.length) {
                            disqualifyPlayer(player, "invalid BOMB syntax: " + line);
                            return;
                        }
                        int tx = Integer.parseInt(tokens[cursor++]);
                        int ty = Integer.parseInt(tokens[cursor++]);
                        if (leagueLevel >= 2) {
                            unit.setAction(Unit.ActionType.BOMB, tx, ty);
                        } else {
                            gameManager.addToGameSummary(
                                player.getNicknameToken() + ": BOMB is not unlocked in this league.");
                        }
                    } else if ("PUSH".equals(act)) {
                        if (cursor + 1 >= tokens.length) {
                            disqualifyPlayer(player, "invalid PUSH syntax: " + line);
                            return;
                        }
                        int tx = Integer.parseInt(tokens[cursor++]);
                        int ty = Integer.parseInt(tokens[cursor++]);
                        if (leagueLevel >= 3) {
                            unit.setAction(Unit.ActionType.PUSH, tx, ty);
                        } else {
                            gameManager.addToGameSummary(
                                player.getNicknameToken() + ": PUSH is not unlocked in this league.");
                        }
                    } else if ("WAIT".equals(act)) {
                        // Do nothing
                    } else if ("MSG".equals(act)) {
                        unit.setMessage(joinRemaining(tokens, cursor));
                        cursor = tokens.length;
                    } else {
                        disqualifyPlayer(player, "unrecognized secondary action '" + act + "'");
                        return;
                    }
                }

                if (cursor < tokens.length) {
                    String extra = tokens[cursor++].toUpperCase();
                    if ("MSG".equals(extra)) {
                        unit.setMessage(joinRemaining(tokens, cursor));
                    }
                }
            } catch (NumberFormatException e) {
                disqualifyPlayer(player, "expected integer coordinates in '" + line + "'");
                return;
            }
        }
    }

    private String joinRemaining(String[] tokens, int start) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < tokens.length; i++) {
            if (i > start) sb.append(' ');
            sb.append(tokens[i]);
        }
        return sb.toString();
    }

    private void disqualifyPlayer(Player player, String reason) {
        player.deactivate(player.getNicknameToken() + " " + reason);
        player.setScore(-1);
        gameManager.addTooltip(player, player.getNicknameToken() + " disqualified: " + reason);
        gameManager.addToGameSummary(GameManager.formatErrorMessage(player.getNicknameToken() + " " + reason));
    }

    private void resolveMovement() {
        Map<Unit, int[]> desiredPos = new HashMap<>();
        for (Unit u : allUnits) {
            if (!u.isAlive()) continue;
            int[] step = grid.nextStepTowards(u.getX(), u.getY(), u.getMoveTargetX(), u.getMoveTargetY());
            desiredPos.put(u, step);
        }

        // Resolve collisions iteratively (head-on swap or multiple units targeting same tile)
        boolean conflictFound = true;
        while (conflictFound) {
            conflictFound = false;
            for (int i = 0; i < allUnits.size(); i++) {
                Unit u1 = allUnits.get(i);
                if (!u1.isAlive()) continue;
                int[] p1 = desiredPos.get(u1);

                for (int j = i + 1; j < allUnits.size(); j++) {
                    Unit u2 = allUnits.get(j);
                    if (!u2.isAlive()) continue;
                    int[] p2 = desiredPos.get(u2);

                    boolean sameTarget = (p1[0] == p2[0] && p1[1] == p2[1]);
                    boolean headOnSwap = (p1[0] == u2.getX() && p1[1] == u2.getY()
                        && p2[0] == u1.getX() && p2[1] == u1.getY());

                    if (sameTarget || headOnSwap) {
                        if (p1[0] != u1.getX() || p1[1] != u1.getY()) {
                            p1[0] = u1.getX();
                            p1[1] = u1.getY();
                            conflictFound = true;
                        }
                        if (p2[0] != u2.getX() || p2[1] != u2.getY()) {
                            p2[0] = u2.getX();
                            p2[1] = u2.getY();
                            conflictFound = true;
                        }
                    }
                }
            }
        }

        // Apply moves
        for (Unit u : allUnits) {
            if (!u.isAlive()) continue;
            int[] dest = desiredPos.get(u);
            if (dest[0] != u.getX() || dest[1] != u.getY()) {
                double angle = Math.atan2(dest[1] - u.getY(), dest[0] - u.getX());
                u.getSprite().setRotation(angle);
                u.setX(dest[0]);
                u.setY(dest[1]);
                u.getGroup().setX(cellCenterX(u.getX()), Curve.EASE_IN_AND_OUT)
                            .setY(cellCenterY(u.getY()), Curve.EASE_IN_AND_OUT);
            }
        }
    }

    private Unit getAliveUnitAt(int x, int y) {
        for (Unit u : allUnits) {
            if (u.isAlive() && u.getX() == x && u.getY() == y) {
                return u;
            }
        }
        return null;
    }

    private void resolvePushes() {
        List<int[]> pushEffects = new ArrayList<>(); // [targetUnitId, nx, ny]
        for (Unit u : allUnits) {
            if (!u.isAlive() || u.getActionType() != Unit.ActionType.PUSH) continue;
            if (u.getCooldown() > 0) {
                gameManager.addToGameSummary(u.getOwner().getNicknameToken()
                    + ": Unit " + u.getId() + " cannot PUSH (cooldown=" + u.getCooldown() + ").");
                continue;
            }
            int tx = u.getActionTargetX();
            int ty = u.getActionTargetY();
            int dist = Math.abs(u.getX() - tx) + Math.abs(u.getY() - ty);
            if (dist != 1) {
                gameManager.addToGameSummary(u.getOwner().getNicknameToken()
                    + ": Unit " + u.getId() + " PUSH target (" + tx + "," + ty + ") is not adjacent.");
                continue;
            }
            Unit target = getAliveUnitAt(tx, ty);
            u.setCooldown(ACTION_COOLDOWN);
            if (target == null) {
                continue;
            }
            int dx = tx - u.getX();
            int dy = ty - u.getY();
            pushEffects.add(new int[]{u.getId(), target.getId(), tx + dx, ty + dy});
        }

        for (int[] eff : pushEffects) {
            Unit pusher = allUnits.get(eff[0]);
            Unit target = allUnits.get(eff[1]);
            if (!target.isAlive()) continue;
            int nx = eff[2];
            int ny = eff[3];

            fxPushPool.setX(cellCenterX(target.getX()))
                      .setY(cellCenterY(target.getY()))
                      .setVisible(true);

            // Check if destination is blocked by pillar or another unit
            if (grid.getTile(nx, ny) == Grid.PILLAR || getAliveUnitAt(nx, ny) != null) {
                target.setCooldown(target.getCooldown() + 2);
                gameManager.addToGameSummary(String.format("%s's Unit %d slammed Unit %d into an obstacle (+2 stun)!",
                    pusher.getOwner().getNicknameToken(), pusher.getId(), target.getId()));
            } else if (!grid.inBounds(nx, ny) || grid.getTile(nx, ny) == Grid.VOID) {
                target.setX(Math.max(0, Math.min(Grid.WIDTH - 1, nx)));
                target.setY(Math.max(0, Math.min(Grid.HEIGHT - 1, ny)));
                eliminateUnit(target, "pushed into the abyss by Unit " + pusher.getId());
                if (target.getOwner() != pusher.getOwner()) {
                    pusher.getOwner().setScore(pusher.getOwner().getScore() + 15);
                }
            } else {
                target.setX(nx);
                target.setY(ny);
                target.getGroup().setX(cellCenterX(nx), Curve.EASE_OUT)
                                 .setY(cellCenterY(ny), Curve.EASE_OUT);
                gameManager.addToGameSummary(String.format("%s's Unit %d pushed Unit %d to (%d, %d)!",
                    pusher.getOwner().getNicknameToken(), pusher.getId(), target.getId(), nx, ny));
            }
        }
    }

    private void resolveBombs() {
        List<int[]> bombedCells = new ArrayList<>();
        for (Unit u : allUnits) {
            if (!u.isAlive() || u.getActionType() != Unit.ActionType.BOMB) continue;
            if (u.getCooldown() > 0) {
                gameManager.addToGameSummary(u.getOwner().getNicknameToken()
                    + ": Unit " + u.getId() + " cannot BOMB (cooldown=" + u.getCooldown() + ").");
                continue;
            }
            int tx = u.getActionTargetX();
            int ty = u.getActionTargetY();
            int dist = Math.abs(u.getX() - tx) + Math.abs(u.getY() - ty);
            if (!grid.inBounds(tx, ty) || dist > BOMB_RANGE || dist == 0) {
                gameManager.addToGameSummary(u.getOwner().getNicknameToken()
                    + ": Unit " + u.getId() + " invalid BOMB target (" + tx + "," + ty + ").");
                continue;
            }
            u.setCooldown(ACTION_COOLDOWN);
            bombedCells.add(new int[]{u.getId(), tx, ty});
        }

        for (int[] b : bombedCells) {
            int tx = b[1], ty = b[2];
            int current = grid.getTile(tx, ty);
            if (current >= 1 && current <= 3) {
                grid.setTile(tx, ty, current - 1);
                updateTileSprite(tx, ty);

                fxExplosionPool.setX(cellCenterX(tx))
                               .setY(cellCenterY(ty))
                               .setVisible(true);

                if (current - 1 == Grid.VOID) {
                    handleTileCollapsed(tx, ty, "bomb explosion");
                }
            }
        }
    }

    private void resolveRingCollapse() {
        int c = collapseRing;
        int center = Grid.WIDTH / 2;
        for (int y = 0; y < Grid.HEIGHT; y++) {
            for (int x = 0; x < Grid.WIDTH; x++) {
                int chebyshev = Math.max(Math.abs(x - center), Math.abs(y - center));
                if (chebyshev == c) {
                    int val = grid.getTile(x, y);
                    if (val == Grid.PILLAR) {
                        grid.setTile(x, y, 1);
                        updateTileSprite(x, y);
                    } else if (val >= 1 && val <= 3) {
                        grid.setTile(x, y, val - 1);
                        updateTileSprite(x, y);
                        if (val - 1 == Grid.VOID) {
                            handleTileCollapsed(x, y, "arena ring collapse");
                        }
                    }
                }
            }
        }
        gameManager.addToGameSummary(GameManager.formatSuccessMessage(
            "Chrono-Storm degrades outer ring (radius " + c + ")!"));
        collapseRing--;
    }

    private void handleTileCollapsed(int x, int y, String cause) {
        // Deactivate any beacon on this tile
        for (Beacon b : grid.getBeacons()) {
            if (b.isActive() && b.getX() == x && b.getY() == y) {
                b.setActive(false);
                b.getSprite().setVisible(false);
            }
        }
        // Eliminate any unit standing on this tile
        Unit victim = getAliveUnitAt(x, y);
        if (victim != null) {
            eliminateUnit(victim, "fell into void (" + cause + ")");
        }
    }

    private void eliminateUnit(Unit unit, String reason) {
        unit.setAlive(false);
        unit.getGroup().setX(cellCenterX(unit.getX()), Curve.EASE_IN)
                       .setY(cellCenterY(unit.getY()), Curve.EASE_IN)
                       .setScale(0.1, Curve.EASE_IN)
                       .setAlpha(0.0, Curve.EASE_IN);
        gameManager.addTooltip(unit.getOwner(),
            unit.getOwner().getNicknameToken() + "'s Unit " + unit.getId() + " eliminated!");
        gameManager.addToGameSummary(GameManager.formatErrorMessage(
            unit.getOwner().getNicknameToken() + "'s Unit " + unit.getId() + " " + reason + "!"));
    }

    private void updateBeaconsAndScores() {
        // Check beacon captures
        for (Beacon b : grid.getBeacons()) {
            if (!b.isActive()) continue;
            Unit occupant = getAliveUnitAt(b.getX(), b.getY());
            if (occupant != null) {
                int newOwner = occupant.getOwner().getIndex();
                if (b.getOwner() != newOwner) {
                    b.setOwner(newOwner);
                    b.getSprite().setImage(newOwner == 0 ? "beacon_p0.png" : "beacon_p1.png");
                    tooltipModule.setTooltipText(b.getSprite(),
                        String.format("Beacon (%d, %d)\nOwner: Player %d", b.getX(), b.getY(), newOwner));
                }
            }
        }

        // Award turn points: +2 per controlled beacon, +1 per alive unit
        for (Player p : gameManager.getActivePlayers()) {
            int pIdx = p.getIndex();
            int ownedBeacons = 0;
            for (Beacon b : grid.getBeacons()) {
                if (b.isActive() && b.getOwner() == pIdx) {
                    ownedBeacons++;
                }
            }
            int aliveUnits = p.getAliveUnits().size();
            int turnGain = ownedBeacons * 2 + aliveUnits;
            p.setScore(p.getScore() + turnGain);
        }
    }

    private void refreshGraphics() {
        for (Player p : gameManager.getPlayers()) {
            p.getScoreText().setText("SCORE: " + p.getScore());
            p.getStatusText().setText("UNITS: " + p.getAliveUnits().size());
        }
        for (Unit u : allUnits) {
            updateUnitTooltip(u);
        }
    }

    private void checkEarlyEnd(int turn) {
        Player p0 = gameManager.getPlayer(0);
        Player p1 = gameManager.getPlayer(1);

        int u0 = p0.getAliveUnits().size();
        int u1 = p1.getAliveUnits().size();

        // If either squad is completely eliminated, award survival bonus and end the game
        if (u0 == 0 || u1 == 0) {
            if (u0 > 0) {
                p0.setScore(p0.getScore() + u0 * 20);
                gameManager.addToGameSummary(GameManager.formatSuccessMessage(
                    p0.getNicknameToken() + " eliminated all opposing units!"));
            } else if (u1 > 0) {
                p1.setScore(p1.getScore() + u1 * 20);
                gameManager.addToGameSummary(GameManager.formatSuccessMessage(
                    p1.getNicknameToken() + " eliminated all opposing units!"));
            } else {
                gameManager.addToGameSummary("All units eliminated!");
            }
            refreshGraphics();
            gameManager.endGame();
            return;
        }

        int activeBeacons = 0;
        for (Beacon b : grid.getBeacons()) {
            if (b.isActive()) activeBeacons++;
        }

        // Mathematical victory check
        int turnsLeft = MAX_TURNS - turn;
        int maxGainPerTurn0 = activeBeacons * 2 + u0;
        int maxGainPerTurn1 = activeBeacons * 2 + u1;
        if (p0.getScore() > p1.getScore() + turnsLeft * maxGainPerTurn1) {
            gameManager.endGame();
        } else if (p1.getScore() > p0.getScore() + turnsLeft * maxGainPerTurn0) {
            gameManager.endGame();
        }
    }

    @Override
    public void onEnd() {
        int[] scores = gameManager.getPlayers().stream().mapToInt(Player::getScore).toArray();
        String[] texts = gameManager.getPlayers().stream()
            .map(p -> p.getScore() + " pts (" + p.getAliveUnits().size() + " units alive)")
            .toArray(String[]::new);
        endScreenModule.setScores(scores, texts);
    }
}
