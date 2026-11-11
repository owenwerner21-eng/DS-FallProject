import java.util.ArrayList;

/**
Coordinates the market, local profiles, and simulated season.
Keeps the original eight-class design from the group project.
@author Rohan Dhameja, Sohan Vaddepuri, Owen Werner
*/
public class FantasyExchange {
    private final ArrayList<User> users = new ArrayList<>();
    private final Market market;
    private final NFLDataManager dataManager;

    /** Starts a market with sixteen sample players and four completed weeks. */
    public FantasyExchange() {
        market = new Market();
        dataManager = new NFLDataManager(market, 4);
        populateSampleMarket();
    }

    /** Connects restored data without generating another set of players. */
    FantasyExchange(Market market, int week) {
        this.market = market;
        dataManager = new NFLDataManager(market, week);
    }

    private void populateSampleMarket() {
        String[][] roster = {
            {"Patrick Mahomes", "QB", "KC"}, {"Justin Jefferson", "WR", "MIN"},
            {"Josh Allen", "QB", "BUF"}, {"Lamar Jackson", "QB", "BAL"},
            {"Jalen Hurts", "QB", "PHI"}, {"Christian McCaffrey", "RB", "SF"},
            {"Saquon Barkley", "RB", "PHI"}, {"Derrick Henry", "RB", "BAL"},
            {"Bijan Robinson", "RB", "ATL"}, {"Ja'Marr Chase", "WR", "CIN"},
            {"CeeDee Lamb", "WR", "DAL"}, {"Amon-Ra St. Brown", "WR", "DET"},
            {"A.J. Brown", "WR", "PHI"}, {"George Kittle", "TE", "SF"},
            {"Travis Kelce", "TE", "KC"}, {"Brock Bowers", "TE", "LV"}
        };
        for (String[] info : roster) {
            Player player = new Player(info[0], info[1], info[2], 100);
            int[][] stats = new int[Player.WEEKS][3];
            for (int i = 0; i < stats.length; i++) { stats[i] = dataManager.createWeek(player, i + 1); }
            // These two starting histories come from the group's original implementation.
            if (player.getName().equals("Patrick Mahomes")) {
                stats = new int[][]{{300,3,0},{280,2,1},{310,4,0},{250,1,1}};
            } else if (player.getName().equals("Justin Jefferson")) {
                stats = new int[][]{{120,1,0},{95,2,0},{140,1,0},{80,0,0}};
            }
            player.setWeeklyStats(stats);
            player.setPreviousPrice(player.getMarketPrice());
            market.addPlayerToMarket(player);
        }
    }

    /** Adds a profile, rejecting duplicate names regardless of capitalization. */
    public void registerUser(User user) {
        if (user == null || login(user.getUsername()) != null || users.size() >= 20) {
            throw new IllegalArgumentException("Name already exists, or the 20-profile limit was reached.");
        }
        users.add(user);
    }
    /** Finds a local profile by name; this is profile selection, not password authentication. */
    public User login(String username) {
        if (username == null) { return null; }
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username.trim())) { return user; }
        }
        return null;
    }
    /** Advances the shared season once. Every local profile sees the new prices. */
    public void updateMarket() { dataManager.updatePlayerStats(); }
    /** Returns the shared market. */
    public Market getMarket() { return market; }
    /** Returns the simulated data source. */
    public NFLDataManager getDataManager() { return dataManager; }
    /** Returns a copy of the registered profile list. */
    public ArrayList<User> getUsers() { return new ArrayList<>(users); }
    /** Returns profiles sorted from highest to lowest net worth. */
    public ArrayList<User> getLeaderboard() {
        ArrayList<User> ranked = getUsers();
        ranked.sort((a, b) -> Double.compare(b.getNetWorth(), a.getNetWorth()));
        return ranked;
    }
}
