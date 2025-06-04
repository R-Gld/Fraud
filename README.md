# Fraud

[![Latest Version](https://badges.spiget.org/resources/version/Version-green-69872.svg)](https://api.spiget.org/v2/resources/69872/versions/latest/download)
[![Rating](https://badges.spiget.org/resources/rating/Rating-blue-69872.svg)](https://www.spigotmc.org/resources/fraud-alts-finder.69872/)
[![Build](https://jitci.com/gh/R-Gld/Fraud/svg)](https://jitci.com/gh/R-Gld/Fraud)
[![API Status](https://status.devgld.fr/api/badge/12/status?style=plastic)](https://status.devgld.fr/status/rapi)

**Fraud** is a Spigot plugin that helps server staff detect alternate accounts by storing players' IP addresses. It can warn when players connect from unusual countries and offers optional geolocation features.

## Features

- Automatic detection of alt accounts using stored IP addresses
- Configurable alerts when a player connects with too many alts or from a specific country
- Optional automatic kick when the alt limit is exceeded
- Detailed information about an IP address (GeoIP) with a built‑in GUI
- Automatic update checking and optional downloading
- Data storage via SQLite or MySQL
- Fully customizable messages through `messages.json`

## Quick Installation

1. Download the latest release from the Spigot page
2. Place `Fraud.jar` in your server's `plugins` folder
3. Start the server to generate configuration files
4. Edit `config.yml` as needed and restart or reload the plugin

## Configuration

The generated `config.yml` (based on `sp-config.yml`) contains the core settings:

```yml
onJoin alert: true        # Show alerts when players join
alts limit: 2             # Number of alts allowed (-1 to disable)
ask for review: false     # Ask players to review the plugin on Spigot
geoip-enable: true        # Enable GeoIP lookups

data store:
  type: sqlite            # sqlite or mysql/mariadb
  #parameters:
  #  ip: "localhost"
  #  port: 3306
  #  user: "fraud"
  #  password: "password"
  #  database: "Fraud"

update:
  check for update: true
  auto download: false

kick:
  enabled: false
  kick reason: "§f§lThe server has detected that you have too many accounts on your ip §7§l({0} accounts)§f§l.\n§f§lYou can not log in with as much of an account as that!\n§f§lContact an administrator if you think this is an error."

countries alert:
  - "FR"
  - "BE"
  - "CH"
  - "CA"
  - "MR"
  - "MC"
  - "DZ"
  - "MA"
```

## Main Commands

Every action is performed through `/fraud` (aliases `/fd` or `/alts`).

| Subcommand                 | Description                                   | Permission                 |
|----------------------------|-----------------------------------------------|----------------------------|
| `/fd check <player|ip>`    | Check a player's alts or an IP                | `fraud.check.player.one`   |
| `/fd all`                  | List all online players who have alts         | `fraud.check.player.all`   |
| `/fd info <player>`        | Show a player's history and location          | `fraud.info`               |
| `/fd geoip <ip>`           | Geolocate an IP address                       | `fraud.geoip`              |
| `/fd alert`                | Toggle receiving alerts                       | `fraud.alert.switch`       |
| `/fd gui`                  | Open the graphical interface                  | `fraud.gui`                |
| `/fd reload`               | Reload configuration and data files           | `fraud.reload`             |
| `/fd download`             | Download the latest plugin version            | `fraud.download`           |
| `/fd version`              | Display the installed version and updates     | `fraud.update`             |

Permissions are described in [`plugin.yml`](src/main/resources/plugin.yml).

## API Usage

A small API is available to retrieve a player's alts or IP information.

```java
import fr.Rgld_.Fraud.Spigot.api.Data;
import fr.Rgld_.Fraud.Spigot.Helpers.IPInfo;

Data data = new Data();
List<String> alts = data.getAlts("PlayerName");
IPInfo ipInfo = data.getIPInfo("1.2.3.4");
```

## Building

This project uses Maven. Run:

```bash
mvn package
```

to generate `Fraud.jar` in the `target/` folder.

## Support

- Discord: [Romain | Rgld_#5344](https://discord.com/users/273162457256558603)
- Email: [spigot@rgld.fr](mailto:spigot@rgld.fr)
- Twitter: [@RGld_](https://twitter.com/RGld_)

---
© Rgld_ – [Spigot page](https://www.spigotmc.org/resources/fraud-alts-finder.69872/)
