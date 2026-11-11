import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

/**
Saves the shared market and every local profile in a small versioned data file.
Writes a temporary file first so an interrupted write does not destroy the last save.
*/
public class SaveManager {
    private static final String HEADER = "FANTASY_EXCHANGE_1";

    /** Creates a file manager. */
    public SaveManager() { }

    /** Saves the entire exchange, replacing the destination only after a complete write. */
    public void save(FantasyExchange exchange, Path destination) throws IOException {
        Path file = destination.toAbsolutePath();
        Files.createDirectories(file.getParent());
        Path temporary = Files.createTempFile(file.getParent(), "fantasy-", ".tmp");
        try {
            try (DataOutputStream out = new DataOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(temporary)))) {
                out.writeUTF(HEADER);
                out.writeInt(exchange.getDataManager().getWeek());
                ArrayList<Player> players = exchange.getMarket().getAvailablePlayers();
                out.writeInt(players.size());
                for (Player p : players) {
                    out.writeUTF(p.getName());
                    out.writeUTF(p.getPosition());
                    out.writeUTF(p.getTeam());
                    out.writeDouble(p.getPreviousPrice());
                    for (int[] row : p.getWeeklyStats()) {
                        for (int stat : row) { out.writeInt(stat); }
                    }
                }
                out.writeInt(exchange.getUsers().size());
                for (User user : exchange.getUsers()) {
                    out.writeUTF(user.getUsername());
                    out.writeInt(user.getTokenCents());
                    ArrayList<Transaction> history = user.getPortfolio().getHistory();
                    out.writeInt(history.size());
                    for (Transaction t : history) {
                        out.writeUTF(t.getPlayer().getName());
                        out.writeUTF(t.getType());
                        out.writeDouble(t.getPrice());
                        out.writeUTF(t.getDate());
                    }
                }
            }
            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    /** Loads a complete exchange, rejecting truncated, unsupported, or invalid files. */
    public FantasyExchange load(Path file) throws IOException {
        if (Files.size(file) > 20000000) { throw new IOException("Save file is too large."); }
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(file)))) {
            if (!HEADER.equals(in.readUTF())) { throw new IOException("Unknown save format."); }
            int week = checkedCount(in.readInt(), 4, NFLDataManager.FINAL_WEEK);
            Market market = new Market();
            int playerCount = checkedCount(in.readInt(), 1, 200);
            for (int i = 0; i < playerCount; i++) {
                Player player = new Player(in.readUTF(), in.readUTF(), in.readUTF(), 10);
                double previousPrice = in.readDouble();
                int[][] stats = new int[Player.WEEKS][3];
                for (int row = 0; row < stats.length; row++) {
                    for (int col = 0; col < stats[row].length; col++) { stats[row][col] = in.readInt(); }
                }
                player.setWeeklyStats(stats);
                player.setPreviousPrice(previousPrice);
                market.addPlayerToMarket(player);
            }
            FantasyExchange exchange = new FantasyExchange(market, week);
            int userCount = checkedCount(in.readInt(), 0, 20);
            for (int i = 0; i < userCount; i++) {
                User user = new User(in.readUTF(), 0);
                user.restoreBalance(in.readInt());
                int tradeCount = checkedCount(in.readInt(), 0, 100000);
                for (int j = 0; j < tradeCount; j++) {
                    Player player = market.searchPlayer(in.readUTF());
                    if (player == null) { throw new IOException("Receipt refers to a missing player."); }
                    user.getPortfolio().record(new Transaction(player, in.readUTF(), in.readDouble(), in.readUTF()));
                }
                exchange.registerUser(user);
            }
            if (in.read() != -1) { throw new IOException("Unexpected data after the save."); }
            return exchange;
        } catch (IllegalArgumentException | IndexOutOfBoundsException exception) {
            throw new IOException("Save contains invalid data.", exception);
        }
    }

    private int checkedCount(int value, int minimum, int maximum) throws IOException {
        if (value < minimum || value > maximum) { throw new IOException("Invalid save count."); }
        return value;
    }
}
