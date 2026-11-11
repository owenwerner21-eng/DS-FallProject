/**
A local trader profile. The integer tokens field stores hundredths of a token.
This prevents the original compound-assignment rounding error on every trade.
@author Rohan Dhameja, Sohan Vaddepuri, Owen Werner
*/
public class User {
    private final String username;
    private int tokens;
    private final Portfolio portfolio;

    /** Creates a profile with a whole-token starting balance. */
    public User(String username, int initialTokens) {
        if (username == null || !username.trim().matches("[A-Za-z0-9_]{1,18}")
                || initialTokens < 0 || initialTokens > 1000000) {
            throw new IllegalArgumentException("Use 1-18 letters, numbers, or underscores.");
        }
        this.username = username.trim();
        tokens = initialTokens * 100;
        portfolio = new Portfolio();
    }

    /** PRACTICE: decide whether the balance covers one share, O(1). */
    public boolean canAfford(Player player) {
        return player != null && tokens >= Math.round(player.getMarketPrice() * 100);
    }

    /** Buys one share and returns false if the balance or holding limit prevents it. */
    public boolean buyPlayer(Player player) {
        if (!canAfford(player) || portfolio.getOwnedPlayers().size() >= Portfolio.MAX_SHARES) {
            return false;
        }
        portfolio.addPlayer(player);
        tokens -= (int) Math.round(player.getMarketPrice() * 100);
        return true;
    }

    /** Sells one share, returning false when no share is owned. */
    public boolean sellPlayer(Player player) {
        if (player == null || portfolio.countShares(player) == 0) { return false; }
        int proceeds = (int) Math.round(player.getMarketPrice() * 100);
        if (tokens > Integer.MAX_VALUE - proceeds) { return false; }
        portfolio.removePlayer(player);
        tokens += proceeds;
        return true;
    }

    /** Adds a positive whole-token amount; the competitive GUI does not offer deposits. */
    public void addTokens(int amount) {
        if (amount <= 0 || amount > (Integer.MAX_VALUE - tokens) / 100) {
            throw new IllegalArgumentException("Deposit must be positive and fit in the balance.");
        }
        tokens += amount * 100;
    }

    /** Restores a saved balance in hundredths of tokens. */
    void restoreBalance(int cents) {
        if (cents < 0) { throw new IllegalArgumentException("Negative balance."); }
        tokens = cents;
    }
    /** Returns the balance in hundredths for lossless saving. */
    int getTokenCents() { return tokens; }
    /** Returns spendable tokens including the fractional part. */
    public double getBalance() { return tokens / 100.0; }
    /** Returns the profile name. */
    public String getUsername() { return username; }
    /** Returns the owned shares and trade history. */
    public Portfolio getPortfolio() { return portfolio; }
    /** Returns cash plus current holdings. */
    public double getNetWorth() { return Player.round(getBalance() + portfolio.getPortfolioValue()); }
}
