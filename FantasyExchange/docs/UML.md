# Fantasy Exchange class diagram

The eight original classes remain. `SaveManager` adds file persistence. The GUI extends ACM `GCanvas`, hosted by a standard Java `JFrame`.

```mermaid
classDiagram
direction TB
class FantasyExchangeGUI {
  -FantasyExchange exchange
  -User currentUser
  -Player selected
  -ArrayList~ClickArea~ clicks
  +init() void
  +drawDashboard() void
  +drawMarketplace() void
  +drawPortfolio() void
  +handleMouseInput(double x, double y) void
}
class FantasyExchange {
  -ArrayList~User~ users
  -Market market
  -NFLDataManager dataManager
  +registerUser(User user) void
  +login(String username) User
  +updateMarket() void
  +getLeaderboard() ArrayList~User~
}
class User {
  -String username
  -int tokens
  -Portfolio portfolio
  +canAfford(Player p) boolean
  +buyPlayer(Player p) boolean
  +sellPlayer(Player p) boolean
  +addTokens(int amount) void
  +getBalance() double
  +getNetWorth() double
}
class Portfolio {
  -ArrayList~Player~ ownedPlayers
  -ArrayList~Transaction~ history
  +addPlayer(Player p) void
  +removePlayer(Player p) void
  +countShares(Player p) int
  +getPortfolioValue() double
  +getTotalProfit() double
  +getAverageCost(Player p) double
}
class Market {
  -ArrayList~Player~ availablePlayers
  +searchPlayer(String name) Player
  +filterPlayers(String query, String position) ArrayList~Player~
  +sortByPrice() void
  +sortedByPrice(ArrayList~Player~ players)$ ArrayList~Player~
  +updatePrices() void
}
class Player {
  -String name
  -String position
  -String team
  -double marketPrice
  -double previousPrice
  -int[][] weeklyStats
  +calculateFantasyPoints() double
  +getWeekPoints(int week) double
  +getAveragePoints() double
  +updateMarketPrice() void
  +addWeek(int[] stats) void
}
class Transaction {
  -Player player
  -String type
  -double price
  -String date
  +getTransactionDetails() String
}
class NFLDataManager {
  -Market market
  -int week
  +fetchPlayerStats() String
  +updatePlayerStats() void
  +calculateFantasyPoints() double
  +getWeek() int
}
class SaveManager {
  +save(FantasyExchange exchange, Path destination) void
  +load(Path file) FantasyExchange
}
GCanvas <|-- FantasyExchangeGUI
FantasyExchangeGUI --> FantasyExchange
FantasyExchangeGUI --> SaveManager
FantasyExchange "1" *-- "0..20" User
FantasyExchange "1" *-- "1" Market
FantasyExchange "1" *-- "1" NFLDataManager
User "1" *-- "1" Portfolio
Portfolio "1" *-- "0..*" Transaction
Portfolio "1" o-- "0..100" Player : shares
Market "1" o-- "1..*" Player : listings
Transaction "0..*" --> "1" Player
NFLDataManager --> Market : updates
SaveManager ..> FantasyExchange : saves and restores
```

`User.tokens` stores hundredths of a token, while the public balance uses token units. Portfolio references can repeat because one list entry represents one share. All profiles reference the same market Player objects, so prices update consistently.
