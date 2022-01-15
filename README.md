[![](https://badges.spiget.org/resources/version/Version-green-69872.svg)](https://api.spiget.org/v2/resources/69872/versions/latest/download)
[![](https://badges.spiget.org/resources/rating/Rating-blue-69872.svg)](https://www.spigotmc.org/resources/fraud.69872/)
[![](https://jitci.com/gh/R-Gld/Fraud/svg)](https://jitci.com/gh/R-Gld/Fraud)

<img align="right" src="https://i.imgur.com/WjvQClG.png" height="200" width="200">

# Fraud
**Fraud** is a French java project of a [Minecraft](https://www.minecraft.net) spigot plugin that allows a server owner / administrator to keep an eye on his players, see if any of them have a double-account.

1. [Installation](#Installation)
1. [Contact](#Contact)

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

import java.util.List;

public class Example {

    public void printAltsOf(String pseudo) {
        Data data = new Data();
        List<String> altsOfRgld_ = data.getAlts(pseudo);
        System.out.println("Alts of " + pseudo + ":");
        for(String alt : altsOfRgld_) {
            System.out.println("\t- " + alt);
        }
    }

    public void getLatitudeAndLongitudeOfPlayer(String pseudo) {
        Data data = new Data();
        IPInfo ipInfoOfRgld_ = data.getIPInfo(Utils.getAddress(Bukkit.getPlayer(pseudo).getAddress()));
        System.out.println(pseudo + " is located at these coordinates(lat/lon): " +
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

### Discord
- *`Romain | Rgld_#5344`*
