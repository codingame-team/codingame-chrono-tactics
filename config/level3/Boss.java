import java.util.*;

class Player {
    static boolean isVoid(String[] grid, int w, int h, int x, int y) {
        if (x < 0 || x >= w || y < 0 || y >= h) return true;
        return grid[y].charAt(x) == '0';
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int width = in.nextInt();
        int height = in.nextInt();
        int leagueLevel = in.nextInt();

        while (true) {
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

            Set<Integer> claimed = new HashSet<>();
            for (int[] u : myUnits) {
                int ux = u[1], uy = u[2], cd = u[3];
                int bestX = width / 2, bestY = height / 2;
                int bestCost = 9999;
                int chosenIdx = -1;

                for (int bi = 0; bi < beacons.size(); bi++) {
                    int[] b = beacons.get(bi);
                    if (b[2] == 0) continue;
                    int cost = Math.abs(ux - b[0]) + Math.abs(uy - b[1]);
                    if (b[2] == 1) cost -= 1; // prioritize stealing opponent beacons
                    if (claimed.contains(bi)) cost += 8;
                    if (cost < bestCost) {
                        bestCost = cost;
                        bestX = b[0];
                        bestY = b[1];
                        chosenIdx = bi;
                    }
                }
                if (chosenIdx != -1) claimed.add(chosenIdx);

                String special = "";
                if (cd == 0) {
                    // 1. Check lethal PUSH first
                    for (int[] e : enemyUnits) {
                        int dist = Math.abs(ux - e[1]) + Math.abs(uy - e[2]);
                        if (dist == 1) {
                            int px = e[1] + (e[1] - ux);
                            int py = e[2] + (e[2] - uy);
                            if (isVoid(grid, width, height, px, py)) {
                                special = " PUSH " + e[1] + " " + e[2];
                                break;
                            }
                        }
                    }
                    // 2. Otherwise check BOMB on fragile tile under enemy
                    if (special.isEmpty()) {
                        for (int[] e : enemyUnits) {
                            int dist = Math.abs(ux - e[1]) + Math.abs(uy - e[2]);
                            if (dist >= 1 && dist <= 2) {
                                char c = grid[e[2]].charAt(e[1]);
                                if (c == '1' || c == '2') {
                                    special = " BOMB " + e[1] + " " + e[2];
                                    break;
                                }
                            }
                        }
                    }
                }
                System.out.println("MOVE " + bestX + " " + bestY + special);
            }
        }
    }
}
