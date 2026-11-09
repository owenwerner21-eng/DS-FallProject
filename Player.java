import java.util.Arrays;

/**
	Represents an NFL player traded on the fantasy stock exchange.
	Stores player information and weekly historical statistics.
	
	@author Rohan Dhameja, Sohan Vaddepuri, Owen Werner
*/
public class Player {
    private String name;
    private String position;
    private String team;
    private double marketPrice;

    /**
		Multi-dimensional array required by project guidelines.
		Dimensions: [weekIndex][statType]
		Row 0: Yards, Row 1: Touchdowns, Row 2: Turnovers
    */
    private int[][] weeklyStats;

    /**
		Constructs a new Player object with basic details and initial market price.
		
		@param name The name of the player
		@param position Player's position (e.g., QB, RB, WR)
		@param team NFL Team abbreviation
		@param initialPrice Starting stock price for the player
    */
    public Player(String name, String position, String team, double initialPrice) {
        this.name = name;
        this.position = position;
        this.team = team;
        this.marketPrice = initialPrice;
        this.weeklyStats = new int[4][3];
    }

    /**
		Calculates estimated fantasy points based on recent weekly performance stats.
		
		@return Double value representing total calculated fantasy points.
    */
    public double calculateFantasyPoints() {
        double points = 0.0;
        for (int i = 0; i < weeklyStats.length; i++) {
            int yards = weeklyStats[i][0];
            int tds = weeklyStats[i][1];
            int turnovers = weeklyStats[i][2];

            points += (yards * 0.1) + (tds * 6.0) - (turnovers * 2.0);
        }
        return points;
    }

    /**
		Recalculates and updates the player's dynamic market value.
    */
    public void updateMarketPrice() {
        double fp = calculateFantasyPoints();
        if (fp > 0) {
            this.marketPrice = 10.0 + (fp * 1.5);
        }
    }

    public String getName() {
        return name;
    }

    public String getPosition() {
        return position;
    }

    public String getTeam() {
        return team;
    }

    public double getMarketPrice() {
        return marketPrice;
    }

    public void setWeeklyStats(int[][] stats) {
        this.weeklyStats = stats;
        updateMarketPrice();
    }
}