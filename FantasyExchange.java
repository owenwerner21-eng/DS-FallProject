import java.util.ArrayList;

/**
	Main system manager coordinating Users, Market transactions, and External Data feeds.
	
	@author Rohan Dhameja, Sohan Vaddepuri, Owen Werner
*/
public class FantasyExchange {
    private ArrayList<User> users;
    private Market market;
    private NFLDataManager dataManager;

    /**
		Constructs core platform engine and populates sample marketplace listings.
    */
    public FantasyExchange() {
        this.users = new ArrayList<>();
        this.market = new Market();
        this.dataManager = new NFLDataManager();

        populateSampleMarket();
    }

    private void populateSampleMarket() {
        Player p1 = new Player("Patrick Mahomes", "QB", "KC", 100.0);
        int[][] stats1 = {{300, 3, 0}, {280, 2, 1}, {310, 4, 0}, {250, 1, 1}};
        p1.setWeeklyStats(stats1);

        Player p2 = new Player("Justin Jefferson", "WR", "MIN", 85.0);
        int[][] stats2 = {{120, 1, 0}, {95, 2, 0}, {140, 1, 0}, {80, 0, 0}};
        p2.setWeeklyStats(stats2);

        market.addPlayerToMarket(p1);
        market.addPlayerToMarket(p2);
    }

    /**
		Registers a new user account on the exchange platform.
		
		@param user Account instance to register
    */
    public void registerUser(User user) {
        users.add(user);
    }

    /**
		Authenticates user session by name.
		
		@param username User name credential
		@return User object instance if verified
    */
    public User login(String username) {
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }

    /**
		Prompts system marketplace recalculation.
    */
    public void updateMarket() {
        market.updatePrices();
    }

    public Market getMarket() {
        return market;
    }
}