# R-Balance Plugin for Timeland

R-Balance is a comprehensive economy system designed specifically for the Timeland Minecraft server. It provides a multi-currency economy with full trade, protection, and logging capabilities.

## Features

### Multi-Currency Economy
- 5 different currency types: Iron (I), Gold (G), Diamond (D), Netherite (N), Emerald (E)
- Support for fractional values (0.1 increments)
- Automatic commission system
- Daily deposit limits
- Server balance tracking

### Commands
- `/bal` - Complete balance management
- `/bal deposit` - Deposit resources from inventory
- `/bal withdraw` - Withdraw resources to inventory
- `/bal top` - View top players by currency
- `/bal pay` - Transfer funds to other players
- `/bal history` - View transaction history

### AFK Trading System
- Create trade signs for automatic transactions
- Format: `[Trade] <Name> <Resource>x<Amount> S:<Price>B /<Price>P`
- Chunk-based protection
- Automatic balance calculations

### Protection & Limits
- Daily deposit limits per currency
- Minimum transaction amounts
- Anti-abuse monitoring
- Operation throttling
- Full transaction logging

## Installation

### Requirements
- Java 17 or higher
- Spigot/Paper 1.20.1 or higher
- Maven 3.6+ (for building)

### Building from Source
```bash
cd rbalance
mvn clean package
```

The compiled JAR will be in `target/RBalance-1.0.0.jar`

### Installation on Server
1. Copy the JAR file to your server's `plugins/` directory
2. Restart or reload your server
3. The plugin will automatically create configuration files
4. Edit `plugins/RBalance/config.yml` as needed
5. Reload with `/rbalance reload` or restart the server

## Configuration

### Database Options
By default, RBalance uses YAML files for data storage. MySQL support will be added in future versions.

### Economy Settings (`config.yml`)
```yaml
economy:
  currencies:
    iron:
      enabled: true
      deposit-commission: 1.0    # Percentage
      withdraw-commission: 3.0   # Percentage
      daily-limit: 1000         # Maximum deposits per day
      min-operation: 0.1        # Minimum transaction amount
```

### Trade Settings
```yaml
trade:
  enabled: true
  chunk-owner-required: true   # Only allow signs in owned chunks
  max-signs-per-player: 10
  min-trade-amount: 0.1
```

### Protection Settings
```yaml
protection:
  max-operations-per-minute: 10
  cooldown-seconds: 5
  log-suspicious-activity: true
```

## Commands & Permissions

### Balance Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/bal` | Show your balance | `rbalance.user` | true |
| `/bal deposit <res> <amt>` | Deposit resources | `rbalance.user` | true |
| `/bal withdraw <res> <amt>` | Withdraw resources | `rbalance.user` | true |
| `/bal top [res]` | Show top players | `rbalance.user` | true |
| `/bal pay <player> <res> <amt>` | Pay another player | `rbalance.user` | true |
| `/bal history [other] <player>` | View history | `rbalance.user` | true |

### Trade Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/tradecmd help` | Trade help | `rbalance.trade` | true |

### Permission Nodes
- `rbalance.*` - Full access to all features
- `rbalance.admin` - Admin permissions
- `rbalance.user` - Basic user access
- `rbalance.trade` - Trade functionality
- `rbalance.trade.create` - Create trade signs
- `rbalance.history.other` - View other players' history

## Creating Trade Signs

1. Place a sign
2. Write on it:
   ```
   [Trade]
   YourName
   Iron x10
   S:4I / B:2I
   ```
3. Right-click to trade!

### Format Details
- Line 1: `[Trade]` (case-insensitive)
- Line 2: Your username (auto-corrected)
- Line 3: Resource and amount (e.g., `Iron x10`)
- Line 4: Prices (e.g., `S:4I / B:2I`)

### Price Format
- `S:4I` - Selling price: 4 Iron
- `B:2I` - Buying price: 2 Iron
- Supports all currencies: `I`, `G`, `D`, `N`, `E`

## API for Developers

### Maven Dependency
```xml
<dependency>
    <groupId>com.timeland</groupId>
    <artifactId>RBalance</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
```

### Getting Player Balance
```java
RBalancePlugin plugin = RBalancePlugin.getInstance();
BalanceManager manager = plugin.getBalanceManager();
double balance = manager.getBalance(player, CurrencyType.IRON);
```

### Depositing/Withdrawing
```java
// Deposit
manager.deposit(player, CurrencyType.GOLD, 10.0);

// Withdraw
manager.withdraw(player, CurrencyType.DIAMOND, 5.0);

// Transfer
manager.transfer(fromPlayer, toPlayer, CurrencyType.EMERALD, 3.0);
```

### Checking Top Players
```java
List<Map.Entry<UUID, Double>> top = manager.getTopBalances(CurrencyType.IRON, 10);
```

## Troubleshooting

### Common Issues

**Plugin not loading:**
- Check Java version (requires Java 17+)
- Verify Spigot version (1.20.1+)
- Check server logs for errors

**Commands not working:**
- Ensure permissions are set correctly
- Check if plugin is enabled with `/plugins`
- Look for errors in console

**Data not saving:**
- Verify write permissions on `plugins/RBalance/` directory
- Check available disk space
- Look for errors during server shutdown

**Trade signs not working:**
- Ensure you have `rbalance.trade.create` permission
- Check sign format carefully
- Verify chunk ownership if `chunk-owner-required` is true

### Performance Tips
- Reduce `max-history-entries` if experiencing lag
- Use MySQL for large servers (when available)
- Adjust `max-operations-per-minute` to prevent spam
- Monitor server balance using `/bal top` for all currencies

## Support

For support, please:
1. Check this README and configuration comments
2. Review server logs for errors
3. Open an issue on our GitHub repository
4. Contact server administrators on Timeland Discord

## Contributing

We welcome contributions! Please:
1. Fork the repository
2. Create a feature branch
3. Follow existing code style
4. Submit a pull request with clear description

## License

This plugin is proprietary software for exclusive use on Timeland Minecraft server. All rights reserved.

---

**Timeland Team** - Crafting the ultimate Minecraft experience