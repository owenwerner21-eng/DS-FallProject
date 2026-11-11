from pathlib import Path
from html import escape

root = Path(__file__).resolve().parents[1]
parts = ['''<svg xmlns="http://www.w3.org/2000/svg" width="1250" height="1440" viewBox="0 0 1250 1440">
<defs>
<marker id="arrow" markerWidth="12" markerHeight="12" refX="10" refY="6" orient="auto"><path d="M1,1 L10,6 L1,11" fill="none" stroke="#46665b" stroke-width="1.5"/></marker>
<marker id="inherit" markerWidth="16" markerHeight="16" refX="14" refY="8" orient="auto"><path d="M1,1 L14,8 L1,15 Z" fill="white" stroke="#46665b" stroke-width="1.5"/></marker>
</defs><rect width="1250" height="1440" fill="#f4f5f0"/>
<style>text{font-family:Arial,sans-serif;fill:#192b2b} .title{font-size:21px;font-weight:bold} .body{font-size:15px} .edge{font-size:13px;fill:#46665b}</style>''']

def text(x,y,value,kind='body'):
    parts.append(f'<text x="{x}" y="{y}" class="{kind}">{escape(value)}</text>')

def box(x,y,w,title,fields,methods):
    h=44 + len(fields)*23 + 14 + len(methods)*23 + 12
    parts.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="3" fill="white" stroke="#46665b" stroke-width="1.5"/>')
    parts.append(f'<path d="M{x} {y+39} H{x+w}" stroke="#46665b"/>')
    text(x+14,y+27,title,'title')
    for i,line in enumerate(fields):text(x+14,y+63+i*23,line)
    split=y+49+len(fields)*23
    parts.append(f'<path d="M{x} {split} H{x+w}" stroke="#d9dfd7"/>')
    for i,line in enumerate(methods):text(x+14,split+25+i*23,line)
    return h

def edge(path,label='',x=0,y=0,kind='arrow',dash=False):
    attr=f' marker-end="url(#{kind})"' if kind else ''
    if dash:attr+=' stroke-dasharray="6 5"'
    parts.append(f'<path d="{path}" fill="none" stroke="#46665b" stroke-width="1.8"{attr}/>')
    if label:text(x,y,label,'edge')

def diamond(x,y,filled=True):
    parts.append(f'<path d="M{x},{y} l-6,9 l6,9 l6,-9 Z" fill="{"#46665b" if filled else "white"}" stroke="#46665b"/>')

# Relationships are drawn behind the class boxes.
edge('M440 108 H370', 'extends', 388,94,'inherit')
edge('M780 108 H850')
edge('M610 224 V270')
edge('M850 175 H812 V350 H780',kind='arrow',dash=True)
edge('M475 501 V530 H195 V570', '0..20 users', 245,519)
diamond(475,501)
edge('M610 501 V570','1',624,547);diamond(610,501)
edge('M750 501 V530 H1030 V570','1',867,519);diamond(750,501)
edge('M195 847 V905','1',210,881);diamond(195,847)
edge('M850 690 H780','updates',790,678)
edge('M650 778 V863 H1030 V905','listings 1..*',740,853);diamond(650,778,False)
edge('M370 986 H850','shares 0..100',512,975)
parts.append('<path d="M370,986 l9,-6 l9,6 l-9,6 Z" fill="white" stroke="#46665b"/>')
edge('M195 1159 V1260 H440','receipts 0..*',235,1248);diamond(195,1159)
edge('M780 1310 H1030 V1182','1 player',900,1299)

box(20,50,350,'GCanvas (ACM)',[],['+ add(GObject): void', '+ paint(Graphics): void'])
box(440,20,340,'FantasyExchangeGUI',['- exchange: FantasyExchange','- currentUser: User','- selected: Player'],['+ drawDashboard(): void','+ drawMarketplace(): void','+ drawPortfolio(): void','+ handleMouseInput(x,y): void'])
box(850,50,360,'SaveManager',[],['+ save(exchange, path): void','+ load(path): FantasyExchange'])
box(440,270,340,'FantasyExchange',['- users: ArrayList<User>','- market: Market','- dataManager: NFLDataManager'],['+ registerUser(user): void','+ login(name): User','+ updateMarket(): void','+ getLeaderboard(): ArrayList<User>'])
box(20,570,350,'User',['- username: String','- tokens: int (hundredths)','- portfolio: Portfolio'],['+ canAfford(player): boolean','+ buyPlayer(player): boolean','+ sellPlayer(player): boolean','+ addTokens(amount): void','+ getBalance(): double','+ getNetWorth(): double'])
box(440,570,340,'Market',['- availablePlayers: ArrayList<Player>'],['+ searchPlayer(name): Player','+ filterPlayers(...): ArrayList','+ sortByPrice(): void','+ sortedByPrice(players): ArrayList','+ updatePrices(): void'])
box(850,570,360,'NFLDataManager',['- market: Market','- week: int'],['+ fetchPlayerStats(): String','+ updatePlayerStats(): void','+ calculateFantasyPoints(): double','+ getWeek(): int'])
box(20,905,350,'Portfolio',['- ownedPlayers: ArrayList<Player>','- history: ArrayList<Transaction>'],['+ addPlayer(player): void','+ removePlayer(player): void','+ countShares(player): int','+ getPortfolioValue(): double','+ getTotalProfit(): double','+ getAverageCost(player): double'])
box(850,905,360,'Player',['- name, position, team: String','- marketPrice: double','- previousPrice: double','- weeklyStats: int[][]'],['+ calculateFantasyPoints(): double','+ getWeekPoints(week): double','+ getAveragePoints(): double','+ updateMarketPrice(): void','+ addWeek(stats): void'])
box(440,1215,340,'Transaction',['- player: Player','- type: String','- price: double','- date: String'],['+ getTransactionDetails(): String'])
text(24,1414,'Filled diamond: composition. Open diamond: shared references. Dashed arrow: dependency.','edge')
parts.append('</svg>')
(root/'docs/UML.svg').write_text('\n'.join(parts))
