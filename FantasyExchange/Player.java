/**
An NFL player and the last four weeks of simulated statistics.
Rows are weeks; columns are yards, touchdowns, and turnovers.
Based on the group's original Player class.
@author Rohan Dhameja, Sohan Vaddepuri, Owen Werner
*/
public class Player {
    public static final int WEEKS = 4;
    public static final int YARDS = 0;
    public static final int TOUCHDOWNS = 1;
    public static final int TURNOVERS = 2;
    private final String name;
    private final String position;
    private final String team;
    private double marketPrice;
    private double previousPrice;
    private int[][] weeklyStats;

    /** Creates a player with an empty four-week history. */
    public Player(String name, String position, String team, double initialPrice) {
        if (name == null || name.trim().isEmpty() || team == null || team.trim().isEmpty()
                || !("QB".equals(position) || "RB".equals(position)
                || "WR".equals(position) || "TE".equals(position))
                || !Double.isFinite(initialPrice) || initialPrice <= 0 || initialPrice > 100000) {
            throw new IllegalArgumentException("Invalid player information.");
        }
        this.name = name.trim();
        this.position = position;
        this.team = team.trim();
        marketPrice = round(initialPrice);
        previousPrice = marketPrice;
        weeklyStats = new int[WEEKS][3];
    }

    /** Returns the total points across all four stored weeks. */
    public double calculateFantasyPoints() {
        double points = 0;
        for (int week = 0; week < weeklyStats.length; week++) {
            points += getWeekPoints(week);
        }
        return round(points);
    }

    /** Returns simplified non-PPR points for a single week. */
    public double getWeekPoints(int week) {
        double yardRate = position.equals("QB") ? 0.04 : 0.1;
        int touchdownPoints = position.equals("QB") ? 4 : 6;
        return round(weeklyStats[week][YARDS] * yardRate
                + weeklyStats[week][TOUCHDOWNS] * touchdownPoints
                - weeklyStats[week][TURNOVERS] * 2);
    }

    /** PRACTICE: average the points in a fixed-size two-dimensional array. */
    public double getAveragePoints() {
        return calculateFantasyPoints() / weeklyStats.length;
    }

    /** Uses four-week average points to set a price, with a five-token floor. */
    public void updateMarketPrice() {
        marketPrice = round(Math.max(5, 10 + getAveragePoints() * 4));
    }

    /** Replaces all four weeks, copying the array to protect the stored data. */
    public void setWeeklyStats(int[][] stats) {
        if (stats == null || stats.length != WEEKS) {
            throw new IllegalArgumentException("Exactly four weeks are required.");
        }
        int[][] copy = new int[WEEKS][3];
        for (int week = 0; week < WEEKS; week++) {
            validateWeek(stats[week]);
            System.arraycopy(stats[week], 0, copy[week], 0, 3);
        }
        previousPrice = marketPrice;
        weeklyStats = copy;
        updateMarketPrice();
    }

    /** Drops the oldest week, shifts the other rows, and adds a new week. */
    public void addWeek(int[] stats) {
        validateWeek(stats);
        previousPrice = marketPrice;
        for (int week = 0; week < WEEKS - 1; week++) {
            weeklyStats[week] = weeklyStats[week + 1];
        }
        weeklyStats[WEEKS - 1] = stats.clone();
        updateMarketPrice();
    }

    private static void validateWeek(int[] stats) {
        if (stats == null || stats.length != 3 || stats[0] < 0 || stats[0] > 1000
                || stats[1] < 0 || stats[1] > 15 || stats[2] < 0 || stats[2] > 15) {
            throw new IllegalArgumentException("Invalid weekly statistics.");
        }
    }

    /** Returns an independent copy of every row. */
    public int[][] getWeeklyStats() {
        int[][] copy = new int[WEEKS][3];
        for (int week = 0; week < WEEKS; week++) {
            copy[week] = weeklyStats[week].clone();
        }
        return copy;
    }

    /** Establishes the starting price or restores a saved previous quote. */
    void setPreviousPrice(double price) {
        if (!Double.isFinite(price) || price <= 0 || price > 100000) {
            throw new IllegalArgumentException("Invalid previous price.");
        }
        previousPrice = round(price);
    }

    /** Rounds token values to two decimal places. */
    public static double round(double value) { return Math.round(value * 100.0) / 100.0; }
    /** Returns the player's display name. */
    public String getName() { return name; }
    /** Returns QB, RB, WR, or TE. */
    public String getPosition() { return position; }
    /** Returns the sample team abbreviation. */
    public String getTeam() { return team; }
    /** Returns the current price in tokens. */
    public double getMarketPrice() { return marketPrice; }
    /** Returns the price before the most recent week. */
    public double getPreviousPrice() { return previousPrice; }
    /** Returns the percentage change from the previous quote. */
    public double getPriceChangePercent() { return (marketPrice / previousPrice - 1) * 100; }
}
