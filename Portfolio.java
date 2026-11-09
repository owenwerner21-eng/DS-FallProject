import java.util.ArrayList;

/**
	Represents a user's holdings and historical stock ledger.
	
	@author Rohan Dhameja, Sohan Vaddepuri, Owen Werner
*/
public class Portfolio {
    private ArrayList<Player> ownedPlayers;
    private ArrayList<Transaction> history;

    /**
		Constructs an empty portfolio with active dynamic collections.
    */
    public Portfolio() {
        this.ownedPlayers = new ArrayList<>();
        this.history = new ArrayList<>();
    }

    /**
		Adds a player to the owned list and logs the transaction record.
		
		@param p Player object to purchase
    */
    public void addPlayer(Player p) {
        ownedPlayers.add(p);
        history.add(new Transaction(p, "BUY", p.getMarketPrice(), "2026-11-08"));
    }

    /**
		Removes a player from the owned list and logs the sale transaction record.
		
		@param p Player object to sell
    */
    public void removePlayer(Player p) {
        if (ownedPlayers.remove(p)) {
            history.add(new Transaction(p, "SELL", p.getMarketPrice(), "2026-11-08"));
        }
    }

    /**
		Calculates total market valuation of all currently held player shares.
		
		@return Summed current market value of owned assets.
    */
    public double getPortfolioValue() {
        double total = 0.0;
        for (Player p : ownedPlayers) {
            total += p.getMarketPrice();
        }
        return total;
    }

    /**
		Summarizes aggregate profit from all trade operations.
		
		@return Calculated net profit.
    */
    public double getTotalProfit() {
        return getPortfolioValue();
    }

    public ArrayList<Player> getOwnedPlayers() {
        return ownedPlayers;
    }
}