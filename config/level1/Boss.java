import java.util.*;

class Player {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int width = in.nextInt();
        int height = in.nextInt();
        int leagueLevel = in.nextInt();

        while (true) {
            int turn = in.nextInt();
            String[] grid = new String[height];
            for (int y = 0; y < height; y++) {
                grid[y] = in.next();
            }
            int unitCount = in.nextInt();
            List<int[]> myUnits = new ArrayList<>();
            for (int i = 0; i < unitCount; i++) {
                int id = in.nextInt();
                int owner = in.nextInt();
                int x = in.nextInt();
                int y = in.nextInt();
                int cd = in.nextInt();
                if (owner == 0) {
                    myUnits.add(new int[]{id, x, y, cd});
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

            for (int[] u : myUnits) {
                int ux = u[1], uy = u[2];
                int bestX = ux, bestY = uy;
                int bestDist = 9999;
                for (int[] b : beacons) {
                    if (b[2] == -1) {
                        int d = Math.abs(ux - b[0]) + Math.abs(uy - b[1]);
                        if (d < bestDist) {
                            bestDist = d;
                            bestX = b[0];
                            bestY = b[1];
                        }
                    }
                }
                System.out.println("MOVE " + bestX + " " + bestY);
            }
        }
    }
}
