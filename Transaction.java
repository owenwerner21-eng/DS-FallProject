/**
	Represents a buy or sell transaction within a user's exchange account.
	
	@author Rohan Dhameja, Sohan Vaddepuri, Owen Werner
*/
public class Transaction {
    private Player player;
    private String type;
    private double price;
    private String date;

    /**
		Constructs a log entry for a fantasy stock purchase or sale.
		
		@param player The player involved in the transaction
		@param type Transaction type ("BUY" or "SELL")
		@param price Execution price per unit
		@param date Date stamp string of the transaction
    */
    public Transaction(Player player, String type, double price, String date) {
        this.player = player;
        this.type = type;
        this.price = price;
        this.date = date;
    }

    /**
		Formats the full transaction details into readable log format.
		
		@return String output containing full transaction metadata.
    */
    public String getTransactionDetails() {
        return String.format("[%s] %s %s @ $%.2f", date, type, player.getName(), price);
    }
}