# Code review presentation

Use these six sections as slides or as an outline while presenting the program and its source. Decide the speaking parts together. The assignments below describe topics, not a claim that any student authored a particular change.

## 1. What the program does

Show the market screen. A trader starts with 500 tokens, buys shares of football players, and earns or loses virtual value when a simulated season changes their statistics. Every profile on the computer sees the same prices.

Show one buy, one new week, and one sell. Explain that the stats are simulated and that there is no real money.

## 2. Class relationships

Display the UML. `FantasyExchange` coordinates users, market, and data manager. Each User owns a Portfolio. The Market and portfolios reference shared Player objects. Transactions store the price at the moment of the trade. SaveManager writes and restores the whole exchange.

Open `User.buyPlayer` and follow its call to `Portfolio.addPlayer`. Show how the GUI checks the success result before reporting a purchase.

## 3. Data structures

Open `Player.weeklyStats`. Explain `[week][stat]`, with three columns: yards, touchdowns, turnovers. Walk through one scoring calculation. Show how `addWeek` shifts the rows and keeps the latest four weeks.

Open `Portfolio.ownedPlayers`. Explain why `[Mahomes, Mahomes, Jefferson]` represents three shares. Show `countShares` and `getPortfolioValue`. Explain why the returned list is a copy while the Player references remain shared.

## 4. Search and sorting

Open `Market.searchPlayer`. It performs sequential search, with O(n) average and worst-case comparisons. Case-insensitive matching and trimming make profile and player lookup predictable.

Open `Market.sortedByPrice`. Trace four prices. Explain the base case, recursive split, two-index merge, and leftover loops. Each level merges n items and the recursion has log n levels, giving O(n log n). The algorithm keeps equal-priced items stable and returns a new list.

## 5. Money and persistence

Explain the original bug: `int tokens -= double price` truncates the result. Now the integer stores hundredths and the public balance returns tokens with decimals.

Explain total profit with a concrete example: buy at 10.25, sell at 15.00, and profit is 4.75. An unclosed position uses its current market value. Show that Transaction.price does not change when Player.marketPrice changes.

Open SaveManager. Explain the header/version, writing a temporary file before replacement, and reconstructing portfolios from receipts using canonical Player references. Mention that corrupted saves are not overwritten.

## 6. Verification and limitations

Run the tests. They cover scoring, duplicate shares, insufficient funds, exact balances, stable sorting, deterministic weeks, saved balances, and truncated files. The GUI checks exercise visible click handlers and render ACM objects.

Limitations: one save should be open in one app instance, profiles have no passwords, and statistics and team labels are a fixed classroom simulation. The desktop dialogs still need a manual run on the presentation computer.

Suggested split for three students: app flow and GUI; classes and arrays; algorithms and saving. Every student should still be able to explain the small practice methods and the trade flow.
