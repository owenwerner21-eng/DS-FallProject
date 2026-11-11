import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

/** Tests the trading rules, algorithms, and save format without opening the GUI. */
public class ExchangeTest {
    private static int checks;

    /** Runs all checks and exits with an error if any assertion fails. */
    public static void main(String[] args) throws Exception {
        scoringAndArrays();
        trading();
        searchingAndSorting();
        profilesAndSeason();
        persistence();
        System.out.println("Passed " + checks + " checks.");
    }

    private static void scoringAndArrays() {
        Player qb = new Player("Quarterback", "QB", "TEST", 100);
        int[][] stats = {{300, 3, 1}, {0, 0, 0}, {100, 1, 0}, {200, 2, 1}};
        qb.setWeeklyStats(stats);
        equal(22, qb.getWeekPoints(0), "QB scoring");
        equal(44, qb.calculateFantasyPoints(), "four-week total");
        equal(11, qb.getAveragePoints(), "four-week average");
        equal(54, qb.getMarketPrice(), "price formula");
        stats[0][0] = 999;
        equal(22, qb.getWeekPoints(0), "input copied");
        int[][] copy = qb.getWeeklyStats();
        copy[0][0] = 0;
        equal(22, qb.getWeekPoints(0), "output copied");
        qb.addWeek(new int[]{250, 2, 0});
        equal(54, qb.getPreviousPrice(), "previous quote");
        equal(0, qb.getWeekPoints(0), "old week dropped");
        equal(18, qb.getWeekPoints(3), "new week added");
        Player wr = new Player("Receiver", "WR", "TEST", 20);
        wr.setWeeklyStats(new int[][]{{120, 1, 0}, {0,0,0}, {0,0,0}, {0,0,0}});
        equal(18, wr.getWeekPoints(0), "receiver scoring");
        wr.setWeeklyStats(new int[][]{{0,0,15},{0,0,15},{0,0,15},{0,0,15}});
        equal(5, wr.getMarketPrice(), "price floor");
        invalid(() -> qb.setWeeklyStats(new int[][]{{1,2,3}}), "reject short array");
        invalid(() -> qb.addWeek(new int[]{-1,0,0}), "reject negative stats");
        invalid(() -> new Player("bad", "QB", "KC", Double.NaN), "reject NaN");
        invalid(() -> new Player("bad", "K", "KC", 10), "reject unsupported position");
    }

    private static void trading() {
        User user = new User("Test", 100);
        Player p = new Player("Fraction", "WR", "KC", 10.25);
        check(user.canAfford(p), "affordable");
        check(user.buyPlayer(p), "buy succeeds");
        equal(89.75, user.getBalance(), "cents preserved");
        equal(0, user.getPortfolio().getTotalProfit(), "buy is not profit");
        check(user.buyPlayer(p), "second share succeeds");
        equal(2, user.getPortfolio().countShares(p), "quantity");
        equal(20.50, user.getPortfolio().getPortfolioValue(), "duplicate shares valued");
        equal(10.25, user.getPortfolio().getAverageCost(p), "average cost");
        p.setWeeklyStats(new int[][]{{100,1,0},{100,1,0},{100,1,0},{100,1,0}});
        equal(74, p.getMarketPrice(), "new quote");
        equal(127.5, user.getPortfolio().getTotalProfit(), "unrealized profit");
        equal(10.25, user.getPortfolio().getHistory().get(0).getPrice(), "receipt stays fixed");
        check(user.sellPlayer(p), "sell succeeds");
        equal(153.5, user.getBalance(), "sale proceeds");
        equal(127.5, user.getPortfolio().getTotalProfit(), "combined profit after partial sale");
        check(user.sellPlayer(p), "last share sells");
        equal(0, user.getPortfolio().getPortfolioValue(), "empty holdings");
        equal(127.5, user.getPortfolio().getTotalProfit(), "realized profit remains");
        check(!user.sellPlayer(p), "no unowned sale");
        equal(4, user.getPortfolio().getHistory().size(), "failed sale not recorded");
        User poor = new User("Poor", 0);
        check(!poor.buyPlayer(p), "insufficient balance");
        check(!poor.buyPlayer(null), "null buy");
        equal(0, poor.getPortfolio().getHistory().size(), "failed buy not recorded");
        invalid(() -> poor.addTokens(-1), "negative deposit");
        poor.addTokens(10);
        equal(10, poor.getBalance(), "deposit");
        User exact = new User("Exact", 10);
        Player ten = new Player("Ten", "RB", "TEST", 10);
        check(exact.buyPlayer(ten), "exact balance purchase");
        equal(0, exact.getBalance(), "exact balance zero");
        User many = new User("Many", 10000);
        for (int i = 0; i < Portfolio.MAX_SHARES; i++) { check(many.buyPlayer(ten), "share " + i); }
        double balance = many.getBalance();
        check(!many.buyPlayer(ten), "share limit");
        equal(balance, many.getBalance(), "failed capped trade preserves cash");
        many.getPortfolio().getOwnedPlayers().clear();
        equal(100, many.getPortfolio().countShares(ten), "returned holdings cannot clear state");
    }

    private static void searchingAndSorting() {
        Market market = new Market();
        Player a = new Player("Alpha", "QB", "KC", 30);
        Player b = new Player("Beta", "WR", "MIN", 10);
        Player c = new Player("Gamma", "TE", "KC", 10);
        market.addPlayerToMarket(a); market.addPlayerToMarket(b); market.addPlayerToMarket(c);
        check(market.searchPlayer("  ALPHA ") == a, "case-insensitive search");
        check(market.searchPlayer("absent") == null, "missing search");
        check(market.searchPlayer(null) == null, "null search");
        equal(2, market.filterPlayers("kc", "ALL").size(), "team search");
        equal(1, market.filterPlayers("a", "WR").size(), "combined filter");
        equal(0, market.filterPlayers("not found", "ALL").size(), "empty filter result");
        ArrayList<Player> sorted = Market.sortedByPrice(market.getAvailablePlayers());
        check(sorted.get(0) == b && sorted.get(1) == c && sorted.get(2) == a, "stable ascending merge");
        check(market.getAvailablePlayers().get(0) == a, "copy sort preserves original");
        equal(0, Market.sortedByPrice(new ArrayList<>()).size(), "empty sort");
        equal(1, Market.sortedByPrice(new ArrayList<>(Arrays.asList(a))).size(), "singleton sort");
        market.sortByPrice();
        check(market.getAvailablePlayers().get(0) == b, "in-place market sort");
        invalid(() -> market.addPlayerToMarket(a), "duplicate player rejected");
    }

    private static void profilesAndSeason() {
        FantasyExchange exchange = new FantasyExchange();
        equal(16, exchange.getMarket().getAvailablePlayers().size(), "sixteen players");
        User user = new User("Owen", 500);
        exchange.registerUser(user);
        check(exchange.login("owen") == user, "profile lookup");
        invalid(() -> exchange.registerUser(new User("OWEN", 500)), "duplicate profile");
        invalid(() -> new User("bad name", 500), "invalid name");
        Player player = exchange.getMarket().getAvailablePlayers().get(0);
        user.buyPlayer(player);
        exchange.updateMarket();
        check(user.getPortfolio().getOwnedPlayers().get(0) == player, "shared player reference");
        equal(5, exchange.getDataManager().getWeek(), "advance one week");
        FantasyExchange other = new FantasyExchange(); other.updateMarket();
        equal(player.getMarketPrice(), other.getMarket().searchPlayer(player.getName()).getMarketPrice(), "repeatable simulation");
        while (exchange.getDataManager().getWeek() < 18) { exchange.updateMarket(); }
        try { exchange.updateMarket(); throw new AssertionError("season did not stop"); }
        catch (IllegalStateException expected) { checks++; }
        equal(18, exchange.getDataManager().getWeek(), "week cap unchanged");
    }

    private static void persistence() throws Exception {
        Path directory = Files.createTempDirectory("fantasy-exchange-test-");
        Path file = directory.resolve("save.dat");
        try {
            FantasyExchange original = new FantasyExchange();
            User one = new User("One", 500);
            User two = new User("Two", 500);
            original.registerUser(one); original.registerUser(two);
            Player p = original.getMarket().searchPlayer("Patrick Mahomes");
            one.buyPlayer(p); one.buyPlayer(p); two.buyPlayer(p);
            original.updateMarket(); one.sellPlayer(p);
            SaveManager manager = new SaveManager(); manager.save(original, file);
            FantasyExchange loaded = manager.load(file);
            equal(one.getBalance(), loaded.login("One").getBalance(), "balance round-trip");
            equal(one.getPortfolio().getTotalProfit(), loaded.login("One").getPortfolio().getTotalProfit(), "profit round-trip");
            equal(3, loaded.login("One").getPortfolio().getHistory().size(), "receipts round-trip");
            Player restored = loaded.getMarket().searchPlayer(p.getName());
            check(restored == loaded.login("Two").getPortfolio().getOwnedPlayers().get(0), "canonical restored references");
            equal(p.getPreviousPrice(), restored.getPreviousPrice(), "previous price round-trip");
            original.updateMarket(); loaded.updateMarket();
            equal(p.getMarketPrice(), restored.getMarketPrice(), "same next week after load");
            manager.save(loaded, file);
            equal(6, manager.load(file).getDataManager().getWeek(), "overwrite round-trip");
            byte[] bytes = Files.readAllBytes(file);
            Files.write(file, Arrays.copyOf(bytes, bytes.length / 2));
            try { manager.load(file); throw new AssertionError("truncated save accepted"); }
            catch (IOException expected) { checks++; }
            equal(bytes.length / 2, Files.size(file), "failed load leaves save untouched");
            Files.write(file, new byte[]{1,2,3,4});
            try { manager.load(file); throw new AssertionError("bad format accepted"); }
            catch (IOException expected) { checks++; }
        } finally { Files.deleteIfExists(file); Files.deleteIfExists(directory); }
    }

    private static void invalid(Runnable action, String message) {
        try { action.run(); throw new AssertionError(message); }
        catch (IllegalArgumentException expected) { checks++; }
    }
    private static void check(boolean result, String message) {
        if (!result) { throw new AssertionError(message); }
        checks++;
    }
    private static void equal(double expected, double actual, String message) {
        check(Math.abs(expected - actual) < 0.000001, message + ": expected " + expected + ", got " + actual);
    }
}
