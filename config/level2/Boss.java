import java.util.*;

class Player {
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

            Set<Integer> claimedBeacons = new HashSet<>();
            for (int[] u : myUnits) {
                int ux = u[1], uy = u[2], cd = u[3];
                int bestX = width / 2, bestY = height / 2;
                int bestScore = 9999;
                int chosenIdx = -1;

                for (int bi = 0; bi < beacons.size(); bi++) {
                    int[] b = beacons.get(bi);
                    if (b[2] == 0) continue;
                    int d = Math.abs(ux - b[0]) + Math.abs(uy - b[1]);
                    if (claimedBeacons.contains(bi)) d += 6;
                    if (d < bestScore) {
                        bestScore = d;
                        bestX = b[0];
                        bestY = b[1];
                        chosenIdx = bi;
                    }
                }
                if (chosenIdx != -1) claimedBeacons.add(chosenIdx);

                String action = "";
                if (cd == 0) {
                    for (int[] e : enemyUnits) {
                        int dist = Math.abs(ux - e[1]) + Math.abs(uy - e[2]);
                        if (dist >= 1 && dist <= 2) {
                            char tile = grid[e[2]].charAt(e[1]);
                            if (tile == '1' || tile == '2') {
                                action = " BOMB " + e[1] + " " + e[2];
                                break;
                            }
                        }
                    }
                }
                System.out.println("MOVE " + bestX + " " + bestY + action);
            }
        }
    }
}
