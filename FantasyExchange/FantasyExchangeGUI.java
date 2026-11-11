import acm.graphics.GLabel;
import acm.graphics.GLine;
import acm.graphics.GRect;
import acm.graphics.GCanvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
ACM graphics interface for the group's fantasy football exchange.
All cards, tables, buttons, and charts are drawn with ACM objects.
Swing dialogs handle text entry and confirmations.
@author Rohan Dhameja, Sohan Vaddepuri, Owen Werner
*/
public class FantasyExchangeGUI extends GCanvas {
    private static final long serialVersionUID = 1L;
    public static final int APPLICATION_WIDTH = 1180;
    public static final int APPLICATION_HEIGHT = 800;
    private static final Color PAPER = new Color(244, 245, 240);
    private static final Color INK = new Color(25, 43, 43);
    private static final Color MUTED = new Color(99, 116, 113);
    private static final Color GREEN = new Color(26, 105, 76);
    private static final Color PALE = new Color(227, 239, 229);
    private static final Color LINE = new Color(217, 223, 215);
    private static final Color RED = new Color(166, 60, 53);
    private static final int LEFT = 205;
    private static final int ROW_HEIGHT = 46;
    private final ArrayList<ClickArea> clicks = new ArrayList<>();
    private FantasyExchange exchange;
    private User currentUser;
    private Player selected;
    private String page = "MARKET";
    private String query = "";
    private String position = "ALL";
    private int sort = 0;
    private int pageNumber = 0;
    private int rowsPerPage = 7;
    private String message = "Select a player to inspect stats and trade.";
    private boolean messageError;
    private boolean saveBlocked;
    private Path savePath;
    private final SaveManager saves = new SaveManager();

    /** Loads saved profiles and creates a demo trader on the first run. */
    public void init() {
        setSize(APPLICATION_WIDTH, APPLICATION_HEIGHT);
        setPreferredSize(new Dimension(APPLICATION_WIDTH, APPLICATION_HEIGHT));
        savePath = Paths.get(System.getProperty("fantasy.save", "data/exchange.dat"));
        try {
            exchange = Files.exists(savePath) ? saves.load(savePath) : new FantasyExchange();
        } catch (IOException exception) {
            exchange = new FantasyExchange();
            saveBlocked = true;
            message = "Save could not load. Original kept; this session will not overwrite it.";
            messageError = true;
        }
        if (exchange.getUsers().isEmpty()) { exchange.registerUser(new User("Trader1", 500)); }
        currentUser = exchange.getUsers().get(0);
        selected = exchange.getMarket().getAvailablePlayers().get(0);
        setFocusable(true);
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent event) {
                requestFocusInWindow();
                FantasyExchangeGUI.this.mouseClicked(event);
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent event) { FantasyExchangeGUI.this.keyPressed(event); }
        });
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent event) {
                if (exchange != null) { drawDashboard(); }
            }
        });
        drawDashboard();
    }

    /** Draws the navigation, account overview, and selected page. */
    public void drawDashboard() {
        if (currentUser == null) { return; }
        setAutoRepaintFlag(false);
        removeAll();
        clicks.clear();
        setBackground(PAPER);
        int width = getWidth();
        int height = getHeight();
        if (width < 1000 || height < 720) {
            label("Enlarge the window to at least 1000 x 720 to see the exchange.", 28, 70, 18, INK, true);
            setAutoRepaintFlag(true);
            repaint();
            return;
        }
        drawSidebar(height);
        label(pageTitle(), LEFT, 47, 28, INK, true);
        label("SIMULATED SEASON  /  WEEK " + exchange.getDataManager().getWeek() + " OF 18",
                LEFT, 73, 11, MUTED, true);
        button("Next week", width - 148, 27, 120, 38, "NEXT", null,
                exchange.getDataManager().getWeek() < NFLDataManager.FINAL_WEEK, true);
        double cardWidth = (width - LEFT - 60) / 3.0;
        metric("AVAILABLE TOKENS", money(currentUser.getBalance()), LEFT, 100, cardWidth, INK);
        metric("PORTFOLIO VALUE", money(currentUser.getPortfolio().getPortfolioValue()),
                LEFT + cardWidth + 16, 100, cardWidth, INK);
        double profit = currentUser.getPortfolio().getTotalProfit();
        metric("TOTAL PROFIT / LOSS", signed(profit), LEFT + 2 * (cardWidth + 16), 100, cardWidth,
                profit < 0 ? RED : GREEN);
        rowsPerPage = Math.max(3, (height - 410) / ROW_HEIGHT);
        if (page.equals("MARKET")) { drawMarketplace(); }
        else if (page.equals("PORTFOLIO")) { drawPortfolio(); }
        else if (page.equals("HISTORY")) { drawHistory(); }
        else { drawLeaderboard(); }
        line(LEFT, height - 46, width - 28, height - 46, LINE);
        label(fit(message, width - LEFT - 32, 12), LEFT, height - 22, 12,
                messageError ? RED : MUTED, false);
        setAutoRepaintFlag(true);
        repaint();
    }

    private String pageTitle() {
        if (page.equals("PORTFOLIO")) { return "Your portfolio"; }
        if (page.equals("HISTORY")) { return "Trade history"; }
        if (page.equals("LEAGUE")) { return "Local league"; }
        return "Player market";
    }

    private void drawSidebar(int height) {
        rectangle(0, 0, 176, height, INK, INK);
        label("FX", 23, 49, 32, new Color(192, 224, 155), true);
        label("FANTASY", 23, 79, 13, Color.WHITE, true);
        label("EXCHANGE", 23, 98, 13, Color.WHITE, true);
        String[] pages = {"MARKET", "PORTFOLIO", "HISTORY", "LEAGUE"};
        String[] titles = {"01  Market", "02  Portfolio", "03  History", "04  League"};
        for (int i = 0; i < pages.length; i++) {
            Color fill = page.equals(pages[i]) ? GREEN : INK;
            rectangle(12, 142 + i * 51, 152, 41, fill, fill);
            label(titles[i], 24, 168 + i * 51, 13, Color.WHITE, page.equals(pages[i]));
            clicks.add(new ClickArea(12, 142 + i * 51, 152, 41, pages[i], null));
        }
        label("CURRENT PROFILE", 22, height - 167, 10, new Color(169, 189, 181), true);
        label(currentUser.getUsername(), 22, height - 142, 14, Color.WHITE, true);
        button("Switch / create", 17, height - 122, 142, 34, "PROFILE", null, true, false);
        button("Save", 17, height - 78, 65, 30, "SAVE", null, !saveBlocked, false);
        button("Rules", 91, height - 78, 68, 30, "RULES", null, true, false);
        label("OFFLINE  /  DEMO DATA", 20, height - 22, 9, new Color(169, 189, 181), false);
    }

    /** Draws searchable listings and the selected player's trading card. */
    public void drawMarketplace() {
        double tableWidth = getWidth() - LEFT - 350;
        button(query.isEmpty() ? "Search name / team" : fit("Search: " + query, tableWidth - 22, 12),
                LEFT, 207, tableWidth, 32, "SEARCH", null, true, false);
        String[] positions = {"ALL", "QB", "RB", "WR", "TE"};
        for (int i = 0; i < positions.length; i++) {
            button(positions[i], LEFT + i * 46, 250, 40, 27, "FILTER:" + positions[i], null,
                    true, position.equals(positions[i]));
        }
        String[] sorts = {"Name", "Price low", "Price high", "Points"};
        button("Sort: " + sorts[sort], LEFT + tableWidth - 123, 250, 123, 27, "SORT", null, true, false);
        ArrayList<Player> players = exchange.getMarket().filterPlayers(query, position);
        if (sort == 1 || sort == 2) {
            players = Market.sortedByPrice(players);
            if (sort == 2) { Collections.reverse(players); }
        } else if (sort == 3) {
            players.sort((a, b) -> Double.compare(b.getAveragePoints(), a.getAveragePoints()));
        } else { players.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName())); }
        drawPlayerRows(players, tableWidth, false);
        drawPlayerDetail(getWidth() - 326, 207, 298);
    }

    /** Draws distinct holdings with quantity, current value, and a sell action. */
    public void drawPortfolio() {
        double tableWidth = getWidth() - LEFT - 350;
        label("NET WORTH  " + money(currentUser.getNetWorth()) + " tokens", LEFT, 229, 14, INK, true);
        label(currentUser.getPortfolio().getOwnedPlayers().size() + " shares held  /  100 maximum",
                LEFT, 260, 12, MUTED, false);
        drawPlayerRows(currentUser.getPortfolio().getUniquePlayers(), tableWidth, true);
        drawPlayerDetail(getWidth() - 326, 207, 298);
    }

    private void drawPlayerRows(ArrayList<Player> players, double width, boolean holdings) {
        label("PLAYER", LEFT + 12, 302, 10, MUTED, true);
        rightLabel(holdings ? "SHARES" : "AVG PTS", LEFT + width - 99, 302, 10, MUTED);
        rightLabel("PRICE", LEFT + width - 12, 302, 10, MUTED);
        int pages = Math.max(1, (players.size() + rowsPerPage - 1) / rowsPerPage);
        pageNumber = Math.min(pageNumber, pages - 1);
        if (players.isEmpty()) {
            label(holdings ? "Your portfolio is empty." : "No players match these filters.", LEFT + 12, 350, 14, MUTED, false);
            label(holdings ? "Buy a share from the Market page." : "Click Search to change or clear the text.",
                    LEFT + 12, 375, 11, MUTED, false);
        }
        int end = Math.min(players.size(), (pageNumber + 1) * rowsPerPage);
        for (int i = pageNumber * rowsPerPage; i < end; i++) {
            Player p = players.get(i);
            int y = 314 + (i % rowsPerPage) * ROW_HEIGHT;
            rectangle(LEFT, y, width, ROW_HEIGHT - 3, p == selected ? PALE : Color.WHITE, LINE);
            label(fit(p.getName(), width - 181, 13), LEFT + 12, y + 18, 13, INK, true);
            label(p.getPosition() + "  /  " + p.getTeam(), LEFT + 12, y + 34, 10, MUTED, false);
            rightLabel(holdings ? "" + currentUser.getPortfolio().countShares(p)
                    : String.format(Locale.US, "%.1f", p.getAveragePoints()), LEFT + width - 99, y + 27, 13, MUTED);
            rightLabel(money(p.getMarketPrice()), LEFT + width - 12, y + 27, 13, INK);
            clicks.add(new ClickArea(LEFT, y, width, ROW_HEIGHT - 3, "SELECT", p));
        }
        pagination(players.size(), pages);
    }

    private void pagination(int count, int pages) {
        int y = getHeight() - 88;
        label(count + " results  /  page " + (pageNumber + 1) + " of " + pages, LEFT, y + 22, 11, MUTED, false);
        button("Prev", LEFT + 235, y, 58, 30, "PREV", null, pageNumber > 0, false);
        button("Next", LEFT + 301, y, 58, 30, "MORE", null, pageNumber + 1 < pages, false);
    }

    private void drawPlayerDetail(double x, double y, double width) {
        rectangle(x, y, width, 407, Color.WHITE, LINE);
        if (selected == null) { label("Select a player", x + 18, y + 34, 16, INK, true); return; }
        Player p = selected;
        label(p.getPosition() + "  /  " + p.getTeam(), x + 18, y + 27, 10, GREEN, true);
        label(fit(p.getName(), width - 34, 19), x + 18, y + 57, 19, INK, true);
        label(money(p.getMarketPrice()), x + 18, y + 98, 31, INK, true);
        label("TOKENS / SHARE", x + 18, y + 117, 9, MUTED, true);
        rightLabel(String.format(Locale.US, "%+.1f%%", p.getPriceChangePercent()), x + width - 18,
                y + 94, 13, p.getPriceChangePercent() < 0 ? RED : GREEN);
        label("FANTASY POINTS / LAST 4 WEEKS", x + 18, y + 154, 10, MUTED, true);
        double max = 1;
        for (int i = 0; i < Player.WEEKS; i++) { max = Math.max(max, Math.abs(p.getWeekPoints(i))); }
        double baseline = y + 236;
        line(x + 18, baseline, x + width - 18, baseline, LINE);
        for (int i = 0; i < Player.WEEKS; i++) {
            double points = p.getWeekPoints(i);
            double barHeight = Math.abs(points) / max * 48;
            double barX = x + 31 + i * 62;
            rectangle(barX, points >= 0 ? baseline - barHeight : baseline, 28, barHeight,
                    points < 0 ? RED : GREEN, points < 0 ? RED : GREEN);
            label(String.format(Locale.US, "%.1f", points), barX - 1,
                    points >= 0 ? baseline - barHeight - 7 : baseline + barHeight + 14, 10, INK, false);
            label("W" + (exchange.getDataManager().getWeek() - 3 + i), barX, y + 309, 10, MUTED, false);
        }
        int shares = currentUser.getPortfolio().countShares(p);
        label("Owned: " + shares + "   |   Avg cost: " + money(currentUser.getPortfolio().getAverageCost(p)),
                x + 18, y + 338, 11, MUTED, false);
        button("Buy 1 share", x + 18, y + 355, 125, 35, "BUY", p,
                currentUser.canAfford(p) && currentUser.getPortfolio().getOwnedPlayers().size() < Portfolio.MAX_SHARES, true);
        button("Sell 1 share", x + 153, y + 355, 127, 35, "SELL", p, shares > 0, false);
        label("Quotes follow the 4-week points average.", x + 2, y + 428, 10, MUTED, false);
    }

    private void drawHistory() {
        label("Every trade records one share at its original price. Latest trades appear first.", LEFT, 231, 13, MUTED, false);
        ArrayList<Transaction> history = currentUser.getPortfolio().getHistory();
        int pages = Math.max(1, (history.size() + rowsPerPage - 1) / rowsPerPage);
        pageNumber = Math.min(pageNumber, pages - 1);
        if (history.isEmpty()) { label("No trades yet. Buy a player from the market to begin.", LEFT, 300, 16, INK, false); }
        for (int row = 0; row < rowsPerPage; row++) {
            int index = history.size() - 1 - pageNumber * rowsPerPage - row;
            if (index < 0) { break; }
            Transaction t = history.get(index);
            int y = 272 + row * ROW_HEIGHT;
            rectangle(LEFT, y, getWidth() - LEFT - 28, 43, Color.WHITE, LINE);
            label(t.getType(), LEFT + 12, y + 27, 12, t.getType().equals("BUY") ? GREEN : RED, true);
            label(t.getPlayer().getName(), LEFT + 80, y + 27, 13, INK, true);
            label(t.getDate(), LEFT + 330, y + 27, 11, MUTED, false);
            rightLabel(money(t.getPrice()) + " tokens", getWidth() - 44, y + 27, 13, INK);
        }
        pagination(history.size(), pages);
    }

    private void drawLeaderboard() {
        label("Profiles on this computer share one season. Each new trader starts with 500 tokens.", LEFT, 231, 13, MUTED, false);
        label("RANK / PROFILE", LEFT + 14, 271, 10, MUTED, true);
        rightLabel("NET WORTH", getWidth() - 45, 271, 10, MUTED);
        ArrayList<User> ranked = exchange.getLeaderboard();
        int pages = Math.max(1, (ranked.size() + rowsPerPage - 1) / rowsPerPage);
        pageNumber = Math.min(pageNumber, pages - 1);
        for (int row = 0; row < rowsPerPage; row++) {
            int index = pageNumber * rowsPerPage + row;
            if (index >= ranked.size()) { break; }
            User user = ranked.get(index);
            int y = 286 + row * ROW_HEIGHT;
            rectangle(LEFT, y, getWidth() - LEFT - 28, 43, user == currentUser ? PALE : Color.WHITE, LINE);
            label(String.format(Locale.US, "%02d", index + 1), LEFT + 14, y + 27, 13, MUTED, true);
            label(user.getUsername() + (user == currentUser ? "  (you)" : ""), LEFT + 62, y + 27, 14, INK, true);
            rightLabel(money(user.getNetWorth()), getWidth() - 45, y + 27, 14, INK);
        }
        pagination(ranked.size(), pages);
    }

    /** Converts a mouse click to the matching visible action. */
    public void mouseClicked(MouseEvent event) { handleMouseInput(event.getX(), event.getY()); }

    /** Handles all visible rows and buttons instead of hard-coding two player rectangles. */
    public void handleMouseInput(double x, double y) {
        for (ClickArea area : new ArrayList<>(clicks)) {
            if (area.bounds.contains(x, y)) { act(area.action, area.player); return; }
        }
    }

    /** Supports page shortcuts 1-4, slash for search, and N for the next week. */
    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_1) { act("MARKET", null); }
        else if (event.getKeyCode() == KeyEvent.VK_2) { act("PORTFOLIO", null); }
        else if (event.getKeyCode() == KeyEvent.VK_3) { act("HISTORY", null); }
        else if (event.getKeyCode() == KeyEvent.VK_4) { act("LEAGUE", null); }
        else if (event.getKeyCode() == KeyEvent.VK_SLASH) { act("SEARCH", null); }
        else if (event.getKeyCode() == KeyEvent.VK_N) { act("NEXT", null); }
    }

    private void act(String action, Player player) {
        messageError = false;
        if (action.equals("MARKET") || action.equals("PORTFOLIO") || action.equals("HISTORY") || action.equals("LEAGUE")) {
            page = action;
            pageNumber = 0;
            message = "Select a player to inspect stats and trade.";
        } else if (action.equals("SELECT")) { selected = player; }
        else if (action.equals("BUY") || action.equals("SELL")) {
            boolean success = action.equals("BUY") ? currentUser.buyPlayer(player) : currentUser.sellPlayer(player);
            message = success ? (action.equals("BUY") ? "Bought " : "Sold ") + player.getName()
                    + " for " + money(player.getMarketPrice()) + " tokens." : "Trade could not be completed. Check balance and holdings.";
            messageError = !success;
            if (success) { save(); }
        } else if (action.equals("NEXT")) {
            if (exchange.getDataManager().getWeek() >= NFLDataManager.FINAL_WEEK) {
                message = "Season complete. The League page shows the final rankings.";
            } else if (JOptionPane.showConfirmDialog(this,
                    "Advance the shared season? Prices will change for every profile.", "Next simulated week",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                exchange.updateMarket();
                message = "Week " + exchange.getDataManager().getWeek() + " complete. All market prices updated.";
                save();
            }
        } else if (action.equals("SEARCH")) {
            String input = JOptionPane.showInputDialog(this, "Player name or team. Leave empty to clear the search:", query);
            if (input != null) { query = input.trim(); page = "MARKET"; pageNumber = 0; }
        } else if (action.startsWith("FILTER:")) { position = action.substring(7); pageNumber = 0; }
        else if (action.equals("SORT")) { sort = (sort + 1) % 4; pageNumber = 0; }
        else if (action.equals("PREV")) { pageNumber = Math.max(0, pageNumber - 1); }
        else if (action.equals("MORE")) { pageNumber++; }
        else if (action.equals("PROFILE")) { changeProfile(); }
        else if (action.equals("SAVE")) { message = "Progress saved."; save(); }
        else if (action.equals("RULES")) { showRules(); }
        drawDashboard();
    }

    private void changeProfile() {
        String name = JOptionPane.showInputDialog(this,
                "Enter a profile name to switch or create (1-18 letters, numbers, underscores).\n"
                + "Profiles are local and have no passwords. New profiles start with 500 tokens.", currentUser.getUsername());
        if (name == null) { return; }
        try {
            User existing = exchange.login(name);
            if (existing == null) {
                existing = new User(name, 500);
                exchange.registerUser(existing);
            }
            currentUser = existing;
            pageNumber = 0;
            message = "Using profile " + currentUser.getUsername() + ".";
            save();
        } catch (IllegalArgumentException exception) {
            message = exception.getMessage();
            messageError = true;
        }
    }

    private void save() {
        if (saveBlocked) {
            message = "Not saved: the original file could not load. Close the app and rename data/exchange.dat.";
            messageError = true;
            return;
        }
        try { saves.save(exchange, savePath); }
        catch (IOException exception) {
            message = "Save failed. This session still works; use Save to retry before closing.";
            messageError = true;
        }
    }

    private void showRules() {
        JOptionPane.showMessageDialog(this,
                "FANTASY EXCHANGE\n\nBuy and sell player shares with virtual tokens. No real money.\n"
                + "All statistics are simulated. Team labels are a fixed sample roster.\n"
                + "QB points = yards / 25 + touchdowns x 4 - turnovers x 2.\n"
                + "RB / WR / TE points = yards / 10 + touchdowns x 6 - turnovers x 2.\n"
                + "Price = 10 + four-week average points x 4, with a minimum of 5.\n"
                + "Profit = current holdings + sale proceeds - purchase costs.\n\n"
                + "Next week advances every profile. Week 18 ends the season.\n"
                + "Progress saves after trades, profile changes, and new weeks.\n"
                + "Keys: 1-4 change pages, / searches, N advances a week.\n"
                + "To start a new season, close the app and rename data/exchange.dat.",
                "How to play", JOptionPane.INFORMATION_MESSAGE);
    }

    private void metric(String title, String value, double x, double y, double width, Color color) {
        rectangle(x, y, width, 85, Color.WHITE, LINE);
        label(title, x + 16, y + 24, 10, MUTED, true);
        label(value, x + 16, y + 62, 27, color, true);
    }
    private void button(String text, double x, double y, double width, double height,
            String action, Player player, boolean enabled, boolean primary) {
        Color fill = !enabled ? LINE : primary ? GREEN : Color.WHITE;
        rectangle(x, y, width, height, fill, primary ? fill : LINE);
        GLabel label = label(text, 0, 0, 12, !enabled ? MUTED : primary ? Color.WHITE : INK, true);
        label.setLocation(x + (width - label.getWidth()) / 2, y + (height + label.getAscent()) / 2 - 2);
        if (enabled) { clicks.add(new ClickArea(x, y, width, height, action, player)); }
    }
    private GRect rectangle(double x, double y, double width, double height, Color fill, Color border) {
        GRect rect = new GRect(x, y, width, height);
        rect.setFilled(true);
        rect.setFillColor(fill);
        rect.setColor(border);
        add(rect);
        return rect;
    }
    private GLabel label(String text, double x, double y, int size, Color color, boolean bold) {
        GLabel label = new GLabel(text, x, y);
        label.setFont(new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, size));
        label.setColor(color);
        add(label);
        return label;
    }
    private void rightLabel(String text, double x, double y, int size, Color color) {
        GLabel label = label(text, x, y, size, color, false);
        label.move(-label.getWidth(), 0);
    }
    private void line(double x1, double y1, double x2, double y2, Color color) {
        GLine line = new GLine(x1, y1, x2, y2);
        line.setColor(color);
        add(line);
    }
    private String fit(String text, double width, int size) {
        GLabel measure = new GLabel(text);
        measure.setFont(new Font("SansSerif", Font.BOLD, size));
        if (measure.getWidth() <= width) { return text; }
        String shortText = text;
        while (shortText.length() > 1) {
            shortText = shortText.substring(0, shortText.length() - 1);
            measure.setLabel(shortText + "...");
            if (measure.getWidth() <= width) { break; }
        }
        return shortText + "...";
    }
    private static String money(double value) { return String.format(Locale.US, "%,.2f", value); }
    private static String signed(double value) { return String.format(Locale.US, "%+,.2f", value); }

    private static class ClickArea {
        private final java.awt.geom.Rectangle2D.Double bounds;
        private final String action;
        private final Player player;
        ClickArea(double x, double y, double width, double height, String action, Player player) {
            bounds = new java.awt.geom.Rectangle2D.Double(x, y, width, height);
            this.action = action;
            this.player = player;
        }
    }

    /** Launches the ACM application. Requires a desktop JDK 17 installation. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FantasyExchangeGUI gui = new FantasyExchangeGUI();
            JFrame window = new JFrame("Fantasy Exchange");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.add(gui);
            gui.init();
            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);
            gui.requestFocusInWindow();
        });
    }
}
