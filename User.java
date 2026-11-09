/**
	Represents an active trader on the Fantasy Exchange platform.
	
	@author Rohan Dhameja, Sohan Vaddepuri, Owen Werner
*/
public class User {
    private String username;
    private int tokens;
    private Portfolio portfolio;

    /**
		Creates a new user profile with a starting allocation of trade tokens.
		
		@param username Account screen name
		@param initialTokens Starting token balance
    */
    public User(String username, int initialTokens) {
        this.username = username;
        this.tokens = initialTokens;
        this.portfolio = new Portfolio();
    }

    /**
		Executes purchase of player stock if user balance permits.
		
		@param p Target Player stock to buy
    */
    public void buyPlayer(Player p) {
        if (tokens >= p.getMarketPrice()) {
            tokens -= p.getMarketPrice();
            portfolio.addPlayer(p);
        }
    }

    /**
		Liquidates player stock back to token currency.
		
		@param p Target Player stock to liquidate
    */
    public void sellPlayer(Player p) {
        if (portfolio.getOwnedPlayers().contains(p)) {
            tokens += p.getMarketPrice();
            portfolio.removePlayer(p);
        }
    }

    /**
		Increases user token balance by specified value.
		
		@param amount Tokens to deposit
    */
    public void addTokens(int amount) {
        this.tokens += amount;
    }

    public int getBalance() {
        return tokens;
    }

    public String getUsername() {
        return username;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }
}