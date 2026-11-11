import java.util.Locale;

/**
An immutable receipt for one share. Its price stays fixed when the market moves.
@author Rohan Dhameja, Sohan Vaddepuri, Owen Werner
*/
public class Transaction {
    private final Player player;
    private final String type;
    private final double price;
    private final String date;

    /** Records a BUY or SELL at its execution price and time. */
    public Transaction(Player player, String type, double price, String date) {
        if (player == null || !("BUY".equals(type) || "SELL".equals(type))
                || !Double.isFinite(price) || price <= 0 || price > 100000
                || date == null || date.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid transaction.");
        }
        this.player = player;
        this.type = type;
        this.price = Player.round(price);
        this.date = date;
    }

    /** Formats a receipt for the history page. */
    public String getTransactionDetails() {
        return String.format(Locale.US, "[%s] %s %s @ %.2f tokens", date, type, player.getName(), price);
    }
    /** Returns the player traded. */
    public Player getPlayer() { return player; }
    /** Returns BUY or SELL. */
    public String getType() { return type; }
    /** Returns the price paid or received. */
    public double getPrice() { return price; }
    /** Returns the time recorded for this trade. */
    public String getDate() { return date; }
}
