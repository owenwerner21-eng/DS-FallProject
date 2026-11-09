import java.util.ArrayList;

/**
	Represents the central marketplace containing available player stocks.
	
	@author Rohan Dhameja, Sohan Vaddepuri, Owen Werner
*/
public class Market {
    private ArrayList<Player> availablePlayers;

    /**
		Initializes market registry.
    */
    public Market() {
        this.availablePlayers = new ArrayList<>();
    }

    public void addPlayerToMarket(Player p) {
        availablePlayers.add(p);
    }

    /**
		Displays all available market listings to standard system output.
    */
    public void listPlayers() {
        for (Player p : availablePlayers) {
            System.out.printf("%s (%s - %s) - $%.2f%n", p.getName(), p.getPosition(), p.getTeam(), p.getMarketPrice());
        }
    }

    /**
		Searches available inventory for player matching the query name.
		
		@param name Target query string
		@return Matching Player object, or null if unlisted
    */
    public Player searchPlayer(String name) {
        for (Player p : availablePlayers) {
            if (p.getName().equalsIgnoreCase(name)) {
                return p;
            }
        }
        return null;
    }

    /**
		Sorts available player list by market price in ascending order.
    */
    public void sortByPrice() {
        availablePlayers.sort((p1, p2) -> Double.compare(p1.getMarketPrice(), p2.getMarketPrice()));
    }

    /**
		Triggers market recalculations across all listed assets.
    */
    public void updatePrices() {
        for (Player p : availablePlayers) {
            p.updateMarketPrice();
        }
    }

    public ArrayList<Player> getAvailablePlayers() {
        return availablePlayers;
    }
}