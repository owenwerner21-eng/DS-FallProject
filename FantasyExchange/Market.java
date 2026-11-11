import java.util.ArrayList;
import java.util.Locale;

/**
Holds the shared Player objects, so every profile sees the same market prices.
@author Rohan Dhameja, Sohan Vaddepuri, Owen Werner
*/
public class Market {
    private final ArrayList<Player> availablePlayers = new ArrayList<>();

    /** Creates an empty market. */
    public Market() { }
    /** Adds a unique player name to the market. */
    public void addPlayerToMarket(Player player) {
        if (player == null || searchPlayer(player.getName()) != null) {
            throw new IllegalArgumentException("Player is missing or already listed.");
        }
        availablePlayers.add(player);
    }
    /** Prints all listings for console debugging. */
    public void listPlayers() {
        for (Player p : availablePlayers) {
            System.out.printf(Locale.US, "%s (%s - %s) %.2f tokens%n",
                    p.getName(), p.getPosition(), p.getTeam(), p.getMarketPrice());
        }
    }
    /** PRACTICE: case-insensitive sequential search by full name, O(n). */
    public Player searchPlayer(String name) {
        if (name == null) { return null; }
        for (Player p : availablePlayers) {
            if (p.getName().equalsIgnoreCase(name.trim())) { return p; }
        }
        return null;
    }
    /** Returns partial name/team matches, optionally restricted by position. */
    public ArrayList<Player> filterPlayers(String query, String position) {
        String search = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        ArrayList<Player> matches = new ArrayList<>();
        for (Player p : availablePlayers) {
            boolean matchesText = p.getName().toLowerCase(Locale.ROOT).contains(search)
                    || p.getTeam().toLowerCase(Locale.ROOT).contains(search);
            if (matchesText && ("ALL".equals(position) || p.getPosition().equals(position))) {
                matches.add(p);
            }
        }
        return matches;
    }
    /** Sorts the actual market using the recursive merge sort below. */
    public void sortByPrice() {
        ArrayList<Player> sorted = sortedByPrice(availablePlayers);
        availablePlayers.clear();
        availablePlayers.addAll(sorted);
    }
    /**
    Returns a new ascending-price list using stable merge sort.
    Splitting and merging take O(n log n) time and O(n) peak extra space.
    */
    public static ArrayList<Player> sortedByPrice(ArrayList<Player> players) {
        if (players.size() < 2) { return new ArrayList<>(players); }
        int middle = players.size() / 2;
        ArrayList<Player> left = sortedByPrice(new ArrayList<>(players.subList(0, middle)));
        ArrayList<Player> right = sortedByPrice(new ArrayList<>(players.subList(middle, players.size())));
        ArrayList<Player> result = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < left.size() && j < right.size()) {
            if (left.get(i).getMarketPrice() <= right.get(j).getMarketPrice()) {
                result.add(left.get(i++));
            } else { result.add(right.get(j++)); }
        }
        while (i < left.size()) { result.add(left.get(i++)); }
        while (j < right.size()) { result.add(right.get(j++)); }
        return result;
    }
    /** Recalculates all current quotes without advancing a week. */
    public void updatePrices() {
        for (Player p : availablePlayers) { p.updateMarketPrice(); }
    }
    /** Returns a copy of the list containing the shared player references. */
    public ArrayList<Player> getAvailablePlayers() { return new ArrayList<>(availablePlayers); }
}
