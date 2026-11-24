import java.util.*;

/**
 * Advanced Algorithmic Agent for Chrono-Tactics:
 * - True BFS shortest-path distance matrix & optimal unit-to-beacon assignment
 * - 1-Turn Simultaneous Lookahead: predicts enemy movement toward their nearest target beacon
 *   before evaluating PUSH (effective threat radius 2) and BOMB (effective threat radius 3)
 * - Lethal PUSH & Lethal BOMB targeting both enemy's current and predicted post-move positions
 */
public class Agent1 {
    static final int[] DX = {0, 1, 0, -1, 0};
    static final int[] DY = {-1, 0, 1, 0, 0};

    static boolean isWalkable(String[] grid, int w, int h, int x, int y) {
        if (x < 0 || x >= w || y < 0 || y >= h) return false;
        char c = grid[y].charAt(x);
        return c >= '1' && c <= '3';
    }

    static boolean isVoid(String[] grid, int w, int h, int x, int y) {
        if (x < 0 || x >= w || y < 0 || y >= h) return true;
        return grid[y].charAt(x) == '0';
    }

    static int[][] bfsDistances(String[] grid, int w, int h, int sx, int sy) {
        int[][] dist = new int[h][w];
        for (int y = 0; y < h; y++) Arrays.fill(dist[y], 999);
        if (!isWalkable(grid, w, h, sx, sy)) return dist;
        Queue<int[]> q = new ArrayDeque<>();
        dist[sy][sx] = 0;
        q.add(new int[]{sx, sy});
        while (!q.isEmpty()) {
            int[] cur = q.poll();
            int cx = cur[0], cy = cur[1];
            for (int d = 0; d < 4; d++) {
                int nx = cx + DX[d], ny = cy + DY[d];
                if (isWalkable(grid, w, h, nx, ny) && dist[ny][nx] > dist[cy][cx] + 1) {
                    dist[ny][nx] = dist[cy][cx] + 1;
                    q.add(new int[]{nx, ny});
                }
            }
        }
        return dist;
    }

    static int[] predictEnemyNextPos(String[] grid, int w, int h, int ex, int ey, List<int[]> beacons) {
        int bestBx = w / 2, bestBy = h / 2, bestD = 9999;
        for (int[] b : beacons) {
            if (b[2] == 1) continue; // enemy already owns it
            int d = Math.abs(ex - b[0]) + Math.abs(ey - b[1]);
            if (d < bestD) {
                bestD = d;
                bestBx = b[0];
                bestBy = b[1];
            }
        }
        int[][] distToB = bfsDistances(grid, w, h, bestBx, bestBy);
        int nx = ex, ny = ey, bestStepD = distToB[ey][ex];
        for (int d = 0; d < 4; d++) {
            int tx = ex + DX[d], ty = ey + DY[d];
            if (isWalkable(grid, w, h, tx, ty) && distToB[ty][tx] < bestStepD) {
                bestStepD = distToB[ty][tx];
                nx = tx;
                ny = ty;
            }
        }
        return new int[]{nx, ny};
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int width = in.nextInt();
        int height = in.nextInt();
        int woodDivision = in.nextInt();
        int leagueLevel = 4 - woodDivision;

        while (true) {
            int turn = in.nextInt();
            String[] grid = new String[height];
            for (int y = 0; y < height; y++) {
                grid[y] = in.next();
            }
            int unitCount = in.nextInt();
            List<int[]> myUnits = new ArrayList<>();
            List<int[]> enemyUnits = new ArrayList<>();
            for (int i = 0; i < unitCount; i++) {
                int id = in.nextInt();
                int owner = in.nextInt();
                int x = in.nextInt();
                int y = in.nextInt();
                int cd = in.nextInt();
                if (owner == 0) {
                    myUnits.add(new int[]{id, x, y, cd});
                } else {
                    enemyUnits.add(new int[]{id, x, y, cd});
                }
            }
            int beaconCount = in.nextInt();
            List<int[]> beacons = new ArrayList<>();
            for (int i = 0; i < beaconCount; i++) {
                int bx = in.nextInt();
                int by = in.nextInt();
                int bOwner = in.nextInt();
                beacons.add(new int[]{bx, by, bOwner});
            }

            // Predict post-movement positions of enemies
            List<int[]> predictedEnemies = new ArrayList<>();
            for (int[] e : enemyUnits) {
                int[] pred = predictEnemyNextPos(grid, width, height, e[1], e[2], beacons);
                predictedEnemies.add(new int[]{e[0], pred[0], pred[1], e[3], e[1], e[2]});
            }

            Set<Integer> assignedBeacons = new HashSet<>();
            Set<Integer> reservedCells = new HashSet<>();

            for (int[] u : myUnits) {
                int ux = u[1], uy = u[2], cd = u[3];

                // 1. Choose target beacon via true BFS distance
                int[][] distFromU = bfsDistances(grid, width, height, ux, uy);
                int targetX = width / 2, targetY = height / 2;
                int bestBeaconScore = 99999;
                int chosenBeacon = -1;

                for (int bi = 0; bi < beacons.size(); bi++) {
                    int[] b = beacons.get(bi);
                    if (b[2] == 0) continue; // already ours
                    int d = distFromU[b[1]][b[0]];
                    if (d >= 999) continue;
                    // Check if an enemy is at the exact same distance (risk of symmetric collision stalemate)
                    boolean symmetricStalemate = false;
                    for (int[] e : enemyUnits) {
                        int ed = Math.abs(e[1] - b[0]) + Math.abs(e[2] - b[1]);
                        if (d == 1 && ed == 1) {
                            symmetricStalemate = true;
                        }
                    }
                    int score = d * 10 - (b[2] == 1 ? 15 : 0) + (assignedBeacons.contains(bi) ? 90 : 0)
                        + (symmetricStalemate ? 80 : 0);
                    if (score < bestBeaconScore) {
                        bestBeaconScore = score;
                        targetX = b[0];
                        targetY = b[1];
                        chosenBeacon = bi;
                    }
                }
                if (chosenBeacon != -1) assignedBeacons.add(chosenBeacon);

                // 2. Evaluate all 5 candidate adjacent moves (N, E, S, W, stay)
                int[][] distFromTarget = bfsDistances(grid, width, height, targetX, targetY);
                int bestMoveX = ux, bestMoveY = uy;
                double bestMoveEval = -1e9;

                for (int d = 0; d < 5; d++) {
                    int nx = ux + DX[d];
                    int ny = uy + DY[d];
                    if (!isWalkable(grid, width, height, nx, ny)) continue;
                    if (d < 4 && reservedCells.contains(ny * width + nx)) continue;

                    double eval = -distFromTarget[ny][nx] * 12.0;
                    char tileIntegrity = grid[ny].charAt(nx);

                    // Prefer higher integrity tiles
                    eval += (tileIntegrity - '1') * 3.0;

                    // Lookahead Danger 1: Stepping on integrity '1' when an armed enemy can reach within range 2
                    if (tileIntegrity == '1' && leagueLevel >= 2) {
                        for (int[] pe : predictedEnemies) {
                            int distPred = Math.abs(pe[1] - nx) + Math.abs(pe[2] - ny);
                            int distCur = Math.abs(pe[4] - nx) + Math.abs(pe[5] - ny);
                            if (pe[3] == 0 && (distPred <= 2 || distCur <= 3)) {
                                eval -= 600.0;
                            }
                        }
                    }

                    // Lookahead Danger 2: Being pushed into void in League 3
                    if (leagueLevel >= 3) {
                        for (int[] pe : predictedEnemies) {
                            if (pe[3] == 0 && Math.abs(pe[1] - nx) + Math.abs(pe[2] - ny) == 1) {
                                int pushDestX = nx + (nx - pe[1]);
                                int pushDestY = ny + (ny - pe[2]);
                                if (isVoid(grid, width, height, pushDestX, pushDestY)) {
                                    eval -= 650.0;
                                }
                            }
                        }
                    }

                    // Avoid head-on collision bounce on contested adjacent tiles
                    for (int[] pe : predictedEnemies) {
                        if (pe[1] == nx && pe[2] == ny) {
                            eval -= 140.0; // enemy is predicted to step onto (nx, ny) this turn!
                        }
                    }

                    // Bonus for capturing an unowned beacon right now
                    for (int[] b : beacons) {
                        if (b[0] == nx && b[1] == ny && b[2] != 0) {
                            eval += 55.0;
                        }
                    }

                    if (eval > bestMoveEval) {
                        bestMoveEval = eval;
                        bestMoveX = nx;
                        bestMoveY = ny;
                    }
                }
                reservedCells.add(bestMoveY * width + bestMoveX);

                // 3. Choose special action from post-move position (bestMoveX, bestMoveY)
                String special = "";
                if (cd == 0 && leagueLevel >= 2) {
                    // Priority 1: Lethal PUSH into void against predicted or current enemy pos (League 3)
                    if (leagueLevel >= 3) {
                        for (int[] pe : predictedEnemies) {
                            int[][] targets = {{pe[1], pe[2]}, {pe[4], pe[5]}};
                            for (int[] t : targets) {
                                if (Math.abs(bestMoveX - t[0]) + Math.abs(bestMoveY - t[1]) == 1) {
                                    int px = t[0] + (t[0] - bestMoveX);
                                    int py = t[1] + (t[1] - bestMoveY);
                                    if (isVoid(grid, width, height, px, py)) {
                                        special = " PUSH " + t[0] + " " + t[1] + " MSG ABYSS!";
                                        break;
                                    }
                                }
                            }
                            if (!special.isEmpty()) break;
                        }
                    }

                    // Priority 2: Lethal BOMB on integrity-1 tile where enemy is or will be
                    if (special.isEmpty()) {
                        for (int[] pe : predictedEnemies) {
                            int[][] targets = {{pe[1], pe[2]}, {pe[4], pe[5]}};
                            for (int[] t : targets) {
                                int dist = Math.abs(bestMoveX - t[0]) + Math.abs(bestMoveY - t[1]);
                                if (dist >= 1 && dist <= 2 && !(t[0] == bestMoveX && t[1] == bestMoveY)) {
                                    if (grid[t[1]].charAt(t[0]) == '1') {
                                        special = " BOMB " + t[0] + " " + t[1] + " MSG TRAP!";
                                        break;
                                    }
                                }
                            }
                            if (!special.isEmpty()) break;
                        }
                    }

                    // Priority 3: Tactical PUSH to stun/displace adjacent enemy (League 3)
                    if (special.isEmpty() && leagueLevel >= 3) {
                        for (int[] pe : predictedEnemies) {
                            if (Math.abs(bestMoveX - pe[1]) + Math.abs(bestMoveY - pe[2]) == 1) {
                                special = " PUSH " + pe[1] + " " + pe[2];
                                break;
                            }
                        }
                    }

                    // Priority 4: Bomb tile under predicted enemy position (never bomb our own destination!)
                    if (special.isEmpty()) {
                        for (int[] pe : predictedEnemies) {
                            int tx = pe[1], ty = pe[2];
                            int dist = Math.abs(bestMoveX - tx) + Math.abs(bestMoveY - ty);
                            if (dist >= 1 && dist <= 2 && !reservedCells.contains(ty * width + tx)) {
                                char c = grid[ty].charAt(tx);
                                if (c >= '1' && c <= '3') {
                                    special = " BOMB " + tx + " " + ty;
                                    break;
                                }
                            }
                        }
                    }
                }

                System.out.println("MOVE " + bestMoveX + " " + bestMoveY + special);
            }
        }
    }
}
