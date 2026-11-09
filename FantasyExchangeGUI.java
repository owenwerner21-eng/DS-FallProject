import acm.graphics.*;
import acm.program.GraphicsProgram;
import java.awt.Color;
import java.awt.event.MouseEvent;

/**
	Graphical Interface for Fantasy Exchange using the ACM Graphics Library.
	
	@author Rohan Dhameja, Sohan Vaddepuri, Owen Werner
*/
public class FantasyExchangeGUI extends GraphicsProgram {

    private FantasyExchange exchange;
    private User currentUser;

    @Override
    public void init() {
        setSize(800, 600);
        exchange = new FantasyExchange();

        currentUser = new User("Trader1", 500);
        exchange.registerUser(currentUser);

        addMouseListeners();
        drawDashboard();
    }

    /**
		Renders main UI header and user status info.
    */
    public void drawDashboard() {
        removeAll();

        GRect header = new GRect(0, 0, getWidth(), 60);
        header.setFilled(true);
        header.setFillColor(new Color(24, 43, 73));
        add(header);

        GLabel title = new GLabel("FANTASY FOOTBALL STOCK EXCHANGE", 20, 40);
        title.setFont("Arial-Bold-20");
        title.setColor(Color.WHITE);
        add(title);

        GLabel userLabel = new GLabel("Tokens: $" + currentUser.getBalance(), getWidth() - 150, 40);
        userLabel.setFont("Arial-Bold-16");
        userLabel.setColor(Color.YELLOW);
        add(userLabel);

        drawMarketplace();
        drawPortfolio();
    }

    /**
		Draws market trading section and player listings.
    */
    public void drawMarketplace() {
        GLabel marketHeading = new GLabel("Marketplace (Click to Buy):", 30, 100);
        marketHeading.setFont("Arial-Bold-16");
        add(marketHeading);

        int y = 130;
        for (Player p : exchange.getMarket().getAvailablePlayers()) {
            GRect card = new GRect(30, y, 320, 45);
            card.setFilled(true);
            card.setFillColor(new Color(230, 240, 250));
            add(card);

            GLabel info = new GLabel(p.getName() + " (" + p.getPosition() + ") - $" + String.format("%.2f", p.getMarketPrice()), 40, y + 28);
            info.setFont("Arial-14");
            add(info);

            y += 55;
        }
    }

    /**
		Draws individual active holdings panel.
    */
    public void drawPortfolio() {
        GLabel portfolioHeading = new GLabel("Your Portfolio:", 400, 100);
        portfolioHeading.setFont("Arial-Bold-16");
        add(portfolioHeading);

        int y = 130;
        for (Player p : currentUser.getPortfolio().getOwnedPlayers()) {
            GRect card = new GRect(400, y, 350, 45);
            card.setFilled(true);
            card.setFillColor(new Color(230, 250, 230));
            add(card);

            GLabel info = new GLabel(p.getName() + " - Valued: $" + String.format("%.2f", p.getMarketPrice()), 410, y + 28);
            info.setFont("Arial-14");
            add(info);

            y += 55;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        handleMouseInput(e.getX(), e.getY());
    }

    /**
		Handles mouse selection and purchasing actions on UI elements.
		
		@param x Coordinate mouse click X position
		@param y Coordinate mouse click Y position
    */
    public void handleMouseInput(double x, double y) {
        if (x >= 30 && x <= 350 && y >= 130 && y <= 175) {
            Player p = exchange.getMarket().getAvailablePlayers().get(0);
            currentUser.buyPlayer(p);
            drawDashboard();
        } else if (x >= 30 && x <= 350 && y >= 185 && y <= 230) {
            Player p = exchange.getMarket().getAvailablePlayers().get(1);
            currentUser.buyPlayer(p);
            drawDashboard();
        }
    }

    public static void main(String[] args) {
        new FantasyExchangeGUI().start(args);
    }
}