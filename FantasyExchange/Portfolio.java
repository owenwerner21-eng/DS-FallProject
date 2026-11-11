import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
One entry in ownedPlayers represents one share, so repeated references are allowed.
The history keeps original prices for calculating profit after the market changes.
@author Rohan Dhameja, Sohan Vaddepuri, Owen Werner
*/
public class Portfolio {
    public static final int MAX_SHARES = 100;
    private final ArrayList<Player> ownedPlayers = new ArrayList<>();
    private final ArrayList<Transaction> history = new ArrayList<>();

    /** Creates an empty portfolio. */
    public Portfolio() { }

    /** Adds one share and records its purchase. */
    public void addPlayer(Player p) {
        record(new Transaction(p, "BUY", p.getMarketPrice(), timestamp()));
    }

    /** Sells one owned share; a missing share leaves the portfolio unchanged. */
    public void removePlayer(Player p) {
        if (ownedPlayers.contains(p)) {
            record(new Transaction(p, "SELL", p.getMarketPrice(), timestamp()));
        }
    }

    private static String timestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /** Replays a validated receipt when loading a saved account. */
    void record(Transaction transaction) {
        if (transaction.getType().equals("BUY")) {
            if (ownedPlayers.size() >= MAX_SHARES) {
                throw new IllegalArgumentException("Portfolio limit: 100 shares.");
            }
            ownedPlayers.add(transaction.getPlayer());
        } else if (!ownedPlayers.remove(transaction.getPlayer())) {
            throw new IllegalArgumentException("Cannot sell an unowned share.");
        }
        history.add(transaction);
    }

    /** PRACTICE: count matching references with one loop, O(n). */
    public int countShares(Player player) {
        int count = 0;
        for (Player owned : ownedPlayers) {
            if (owned == player) { count++; }
        }
        return count;
    }

    /** PRACTICE: sum the current value of every owned share, O(n). */
    public double getPortfolioValue() {
        double total = 0;
        for (Player p : ownedPlayers) {
            total += p.getMarketPrice();
        }
        return Player.round(total);
    }

    /** Returns realized and unrealized profit: holdings + sales - purchases. */
    public double getTotalProfit() {
        double profit = getPortfolioValue();
        for (Transaction transaction : history) {
            if (transaction.getType().equals("BUY")) { profit -= transaction.getPrice(); }
            else { profit += transaction.getPrice(); }
        }
        return Player.round(profit);
    }

    /** Returns the average purchase price of shares still held, using FIFO sales. */
    public double getAverageCost(Player player) {
        ArrayList<Double> costs = new ArrayList<>();
        for (Transaction transaction : history) {
            if (transaction.getPlayer() == player) {
                if (transaction.getType().equals("BUY")) { costs.add(transaction.getPrice()); }
                else { costs.remove(0); }
            }
        }
        if (costs.isEmpty()) { return 0; }
        double total = 0;
        for (double cost : costs) { total += cost; }
        return total / costs.size();
    }

    /** Returns each owned player once, in order of first purchase. */
    public ArrayList<Player> getUniquePlayers() {
        ArrayList<Player> unique = new ArrayList<>();
        for (Player p : ownedPlayers) {
            if (!unique.contains(p)) { unique.add(p); }
        }
        return unique;
    }

    /** Returns a copy of the share list. */
    public ArrayList<Player> getOwnedPlayers() { return new ArrayList<>(ownedPlayers); }
    /** Returns a copy of the receipt list. */
    public ArrayList<Transaction> getHistory() { return new ArrayList<>(history); }
}
