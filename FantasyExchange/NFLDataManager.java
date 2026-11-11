import java.util.Random;

/**
Generates an offline classroom season. No values are claimed to be live NFL data.
The same player and week always produce the same simulated result.
@author Rohan Dhameja, Sohan Vaddepuri, Owen Werner
*/
public class NFLDataManager {
    private final Market market;
    private int week;
    public static final int FINAL_WEEK = 18;

    /** Creates a standalone demo data manager. */
    public NFLDataManager() { this(new Market(), 4); }
    /** Connects the data manager to a shared market. */
    public NFLDataManager(Market market, int week) {
        if (market == null || week < 4 || week > FINAL_WEEK) {
            throw new IllegalArgumentException("Invalid season.");
        }
        this.market = market;
        this.week = week;
    }
    /** Describes the actual source of the data. */
    public String fetchPlayerStats() { return "Offline simulated stats. No live NFL connection."; }
    /** Advances all players by one week; the season stops at week 18. */
    public void updatePlayerStats() {
        if (week >= FINAL_WEEK) { throw new IllegalStateException("The 18-week season is complete."); }
        week++;
        for (Player player : market.getAvailablePlayers()) {
            player.addWeek(createWeek(player, week));
        }
    }
    /** Calculates one deterministic sample week from the player's position. */
    public int[] createWeek(Player player, int number) {
        Random random = new Random(7301L + player.getName().hashCode() * 31L + number * 997L);
        int yards;
        int touchdowns;
        if (player.getPosition().equals("QB")) {
            yards = 150 + random.nextInt(251);
            touchdowns = random.nextInt(5);
        } else {
            yards = 20 + random.nextInt(player.getPosition().equals("TE") ? 101 : 161);
            touchdowns = random.nextInt(3);
        }
        int turnovers = random.nextInt(5) == 0 ? 1 : 0;
        return new int[]{yards, touchdowns, turnovers};
    }
    /** Returns total four-week points across the current market. */
    public double calculateFantasyPoints() {
        double total = 0;
        for (Player p : market.getAvailablePlayers()) { total += p.calculateFantasyPoints(); }
        return Player.round(total);
    }
    /** Returns the last completed week. */
    public int getWeek() { return week; }
}
