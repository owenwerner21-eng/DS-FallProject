import acm.graphics.GLabel;
import acm.graphics.GObject;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/** Renders the actual ACM objects and checks the main non-dialog click paths. */
public class GuiTest {
    private static int checks;

    /** Uses a temporary save so the user's season stays untouched. */
    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("fantasy-gui-test-");
        Path save = directory.resolve("exchange.dat");
        System.setProperty("fantasy.save", save.toString());
        SwingUtilities.invokeAndWait(() -> {
            try {
                FantasyExchangeGUI gui = new FantasyExchangeGUI();
                gui.init();
                gui.drawDashboard();
                check(hasText(gui, "Player market"), "market page");
                render(gui, "market.png");
                gui.handleMouseInput(900, 578);
                check(hasText(gui, "Bought Patrick Mahomes"), "buy click");
                gui.handleMouseInput(30, 215);
                check(hasText(gui, "Your portfolio"), "portfolio navigation");
                check(hasText(gui, "Patrick Mahomes"), "holding appears");
                render(gui, "portfolio.png");
                gui.handleMouseInput(1060, 578);
                check(hasText(gui, "Your portfolio is empty."), "sell last share");
                gui.handleMouseInput(1060, 578);
                gui.handleMouseInput(30, 265);
                check(hasText(gui, "Trade history"), "history navigation");
                check(hasText(gui, "SELL"), "sale receipt");
                render(gui, "history.png");
                gui.handleMouseInput(30, 320);
                check(hasText(gui, "Trader1"), "leaderboard profile");
                render(gui, "league.png");
                gui.keyPressed(new KeyEvent(gui, KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_1, '1'));
                gui.handleMouseInput(230, 263);
                gui.handleMouseInput(775, 263);
                check(hasText(gui, "Sort: Price low"), "sort click");
                gui.handleMouseInput(520, 734);
                check(hasText(gui, "page 2 of 2"), "pagination");
                gui.setSize(1000, 720);
                gui.drawDashboard();
                check(hasText(gui, "Player market"), "minimum layout");
                render(gui, "market-small.png");
                gui.setSize(800, 600);
                gui.drawDashboard();
                check(hasText(gui, "Enlarge the window"), "small-window guidance");
                FantasyExchange restored = new SaveManager().load(save);
                check(restored.getUsers().get(0).getPortfolio().getHistory().size() == 2,
                        "disabled sell click does not create another receipt");
                check(restored.getUsers().get(0).getBalance() == 500, "round trip trade preserves balance");
            } catch (Exception exception) { throw new RuntimeException(exception); }
        });
        Files.deleteIfExists(save);
        Files.deleteIfExists(directory);
        System.out.println("Passed " + checks + " GUI checks. Renders are in build/screenshots.");
    }

    private static boolean hasText(FantasyExchangeGUI gui, String fragment) {
        for (int i = 0; i < gui.getElementCount(); i++) {
            GObject object = gui.getElement(i);
            if (object instanceof GLabel && ((GLabel) object).getLabel().contains(fragment)) { return true; }
        }
        return false;
    }
    private static void check(boolean condition, String name) {
        if (!condition) { throw new AssertionError(name); }
        checks++;
    }
    private static void render(FantasyExchangeGUI gui, String name) throws Exception {
        BufferedImage image = new BufferedImage(gui.getWidth(), gui.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(gui.getBackground());
        graphics.fillRect(0, 0, gui.getWidth(), gui.getHeight());
        graphics.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        for (int i = 0; i < gui.getElementCount(); i++) {
            GObject object = gui.getElement(i);
            Graphics2D layer = (Graphics2D) graphics.create();
            layer.setColor(object.getColor());
            object.paint(layer);
            layer.dispose();
        }
        graphics.dispose();
        Path directory = Path.of("build/screenshots");
        Files.createDirectories(directory);
        ImageIO.write(image, "png", directory.resolve(name).toFile());
    }
}
