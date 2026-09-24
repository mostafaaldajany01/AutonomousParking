package automaticparking;

public class Street {
    public static final int FREE_DISTANCE = 150;
    public static final int BLOCKED_DISTANCE = 30;
    public static final int LENGTH = 500;

    private final boolean[] free = new boolean[LENGTH];

    public Street(int[][] places) {
        for (int[] place : places)
            for (int m = place[0]; m < place[0] + place[1]; m++)
                free[m] = true;
    }

    public int distanceAt(int meter) {
        return free[meter] ? FREE_DISTANCE : BLOCKED_DISTANCE;
    }
}