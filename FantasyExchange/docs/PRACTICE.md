# Methods to reconstruct

Make a copy of the project before practicing. Replace only the body of one method, compile, and run the relevant checks. Keep the working project intact.

## 1. User.canAfford(Player player)

Return false for null. Convert the player's price to hundredths of a token and compare it with the stored integer balance. Do not subtract tokens.

Examples: 100 tokens with a 10.25 quote returns true; zero tokens returns false. An exact balance is enough.

Time: O(1). Extra space: O(1).

## 2. Portfolio.countShares(Player player)

Loop through `ownedPlayers`, count entries referring to the given player, and return the count. Multiple entries can reference the same object. Use identity because the market owns one canonical Player object per listing.

Examples: `[A, B, A]` gives 2 for A; an empty list gives 0.

Time: O(n). Extra space: O(1).

## 3. Portfolio.getPortfolioValue()

Start a total at zero. Add the market price of every list entry, including repeated shares, then round to two decimal places.

Example: two shares worth 10.25 each and one worth 20.00 total 40.50.

Time: O(n). Extra space: O(1).

## 4. Market.searchPlayer(String name)

Handle null, trim spaces, and check each player's full name without regard to capitalization. Return the actual Player reference on a match and null when no match exists.

This is sequential search. The list is not guaranteed to be sorted by name, so ordinary binary search would not be valid here.

Time: best O(1), average/worst O(n) comparisons. Extra space: O(1), treating short names as bounded strings.

## 5. Player.getAveragePoints()

Divide the four-week points total by the number of rows. Preserve decimal values.

With exactly four rows, this is O(1). If the number of weeks became a variable w, calculating the sum and average would be O(w).

## Stretch: Market.sortedByPrice(ArrayList<Player> players)

Base case: fewer than two items. Otherwise split into two halves, recursively sort each, merge with two indices, and copy leftovers. Choose the left item on equal prices to preserve stability.

Trace prices `[30, 10, 25, 10]` by hand. The result is `[10, 10, 25, 30]`, with the two equal-price players keeping their original order. Then explain why each recursion level handles n items and there are about log2(n) levels.

Time: O(n log n). Peak extra space: O(n). Stack depth: O(log n).

## Questions for the code review

Why must a receipt keep the old price when its Player reference gets a new price? Why does summing portfolio value alone not measure profit? Why does selling remove only one matching Player reference? Why do the stats getters copy each row? What happens when a buy fails? What is the difference between calling updatePrices and advancing the simulated week?
