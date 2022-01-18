[![](https://badges.spiget.org/resources/version/Version-green-69872.svg)](https://api.spiget.org/v2/resources/69872/versions/latest/download)
[![](https://badges.spiget.org/resources/rating/Rating-blue-69872.svg)](https://www.spigotmc.org/resources/fraud.69872/)
[![](https://jitci.com/gh/R-Gld/Fraud/svg)](https://jitci.com/gh/R-Gld/Fraud)

<img align="right" src="https://i.imgur.com/WjvQClG.png" height="200" width="200">

# Fraud
**Fraud** is a French java project of a [Minecraft](https://www.minecraft.net) spigot plugin that allows a server owner / administrator to keep an eye on his players, see if any of them have a double-account.

1. [Installation](#Installation)
2. [API](#API)
3. [Contact](#Contact)
4. [Other Projects](#other-projects)

## Installation

- Put the jar on the plugin directory of the server.
- Restart the server or if you have the [PlugMan](https://www.spigotmc.org/resources/plugmanx.88135/) ([Sources](https://github.com/TheBlackEntity/PlugMan/)) plugin on it, load the plugin with the command `/plugman load Fraud`.
- Configure the plugin in the file `config.yml`. You can translate or just change the messages in this file.
- Restart the server or just the plugin if you can.

## API

- Examples ([file](https://github.com/R-Gld/Fraud/blob/master/src/tests/Example.java)):

```java
import fr.Rgld_.Fraud.Global.IPInfo;
import fr.Rgld_.Fraud.Spigot.Helpers.Utils;
import fr.Rgld_.Fraud.Spigot.api.Data;
import org.bukkit.Bukkit;

import java.net.InetSocketAddress;
import java.util.List;

public class Example {

    /**
     * Sends in the console the complete list of alts of the player given in parameters.
     *
     * @param pseudo The name in game of the player.
     */
    public void printAltsOf(String pseudo) {
        Data data = new Data();
        List<String> altsOfRgld_ = data.getAlts(pseudo);
        System.out.println("Alts of " + pseudo + ":");
        for (String alt : altsOfRgld_) {
            System.out.println("\t- " + alt);
        }
    }

    /**
     * Print in the console the geolocation of a player into the minecraft (spigot) server.
     * @param pseudo (a {@link String}) the name in game of the player that we want to obtain the geolocation.
     * @see Example#getLatitudeAndLongitudeOfAnIp(String)
     * @see Utils#getAddress(InetSocketAddress) to format the {@link InetSocketAddress} object to a string that is conforming to the database.
     */
    public void getLatitudeAndLongitudeOfAPlayer(String pseudo) {
        getLatitudeAndLongitudeOfAnIp(Utils.getAddress(Bukkit.getPlayer(pseudo).getAddress()));
    }

    /**
     * Print in the console the geolocation of an ip.
     * @param ip (a {@link String}) the ip that we want to obtain the geolocation.
     * @see Example#getLatitudeAndLongitudeOfAnIp(String)
     */
    public void getLatitudeAndLongitudeOfAnIp(String ip) {
        Data data = new Data();
        IPInfo ipInfoOfRgld_ = data.getIPInfo(ip);
        System.out.println(ip + " is located at these coordinates(lat/lon): " +
                ipInfoOfRgld_.getLatitude() + "/" + ipInfoOfRgld_.getLongitude()
        );
    }
}
```

## Contact

### Discord
- [*`Romain | Rgld_#5344`*](https://discord.com/users/273162457256558603)

### Emails
- [*`spigot@rgld.fr`*](mailto:spigot@rgld.fr)

### Twitter
- [*`RGld_`*](https://twitter.com/RGld_)

## Other Projects

### WeatherBesac
#### Description
- It's a Twitter bot that tweets at certain times of the day the current weather in the city of Besançon in France in the Doubs (a French subdivision). This tweet is accompanied by a photo taken at the time of the tweet from a known place in Besançon to be able to look at the state of the sky, for example.
#### Links
- [Twitter account](https://twitter.com/BesanconMeteo)
- [Source-Code](https://github.com/R-Gld/weather_besac_bot_twitter)