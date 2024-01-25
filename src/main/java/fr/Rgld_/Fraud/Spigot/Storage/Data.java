package fr.Rgld_.Fraud.Spigot.Storage;

import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.Rgld_.Fraud.Spigot.Fraud;
import fr.Rgld_.Fraud.Spigot.Helpers.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class used to store datas in the database sqlite of the server.
 */
public class Data {

    private HikariDataSource dataSource;
    public HikariDataSource getDataSource() {
        return dataSource;
    }

    private enum TABLE_NAME {
        ips("ips"),
        connections("connections"),
        reports("reports");

        private final String name;

        TABLE_NAME(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }


    private final Fraud fraud;
    private final Configuration.DatabaseSection.Type type;

    private final long cacheRefreshInterval = 60000; // Intervalle de rafraîchissement du cache en millisecondes (par exemple, 1 minute)
    private long lastCacheRefreshTime = System.currentTimeMillis();
    private final Map<String, List<String>> cache = new ConcurrentHashMap<>();

    private File file = null;

    public Data(Fraud fraud) {
        String[] createTableQuery;
        this.fraud = fraud;
        Configuration.DatabaseSection db = fraud.getConfiguration().getDatabase();
        this.type = db.getType();
        switch(type) {
            case MYSQL:
                HikariConfig mysqlConfig = new HikariConfig();
                mysqlConfig.setJdbcUrl(db.generateURL());
                mysqlConfig.setUsername(db.getUser());
                mysqlConfig.setPassword(db.getPassword());
                mysqlConfig.setMaximumPoolSize(10);

                dataSource = new HikariDataSource(mysqlConfig);

                createTableQuery = new String[] {

                    // IP Table
                    "CREATE TABLE IF NOT EXISTS " + TABLE_NAME.ips.getName() +
                            "(id INT AUTO_INCREMENT PRIMARY KEY," +
                            "pseudo text NOT NULL," +
                            "ip text NOT NULL," +
                            "INDEX (ip)," +
                            "INDEX (pseudo));",

                    // Connection Table
                    "CREATE TABLE IF NOT EXISTS " + TABLE_NAME.connections.getName() +
                            "(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                            "pseudo text NOT NULL, " +
                            "first bigint, " +
                            "last bigint," +
                            "INDEX(pseudo));"
                };
                break;
            case SQLITE:
                file = new File(fraud.getDataFolder(), "data.sqlite");
                if(!file.canRead() || !file.canWrite()) {
                    System.err.println("The file " + file.getAbsolutePath() + " can't be read or write. Please adjust the permissions or modify the configuration for a MySQL connection.");
                    fraud.getPluginLoader().disablePlugin(fraud);
                    return;
                }
                HikariConfig sqliteConfig = new HikariConfig();
                sqliteConfig.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath().replace("\\", File.separator));
                sqliteConfig.setMaximumPoolSize(10); // Nombre maximum de connexions dans le pool (ajustez selon vos besoins)
                dataSource = new HikariDataSource(sqliteConfig);

                createTableQuery = new String[] {
                    // IP Table
                    "CREATE TABLE IF NOT EXISTS " + TABLE_NAME.ips.getName() +
                        "(id INTEGER PRIMARY KEY," +
                        "pseudo VARCHAR(64) NOT NULL," +
                        "ip VARCHAR(16) NOT NULL);" +
                    "CREATE INDEX IF NOT EXISTS idx_ip ON " + TABLE_NAME.ips.getName() + " (ip);",
                    "CREATE INDEX IF NOT EXISTS idx_pseudo ON " + TABLE_NAME.ips.getName() + " (pseudo);",

                    // Connection Table
                    "CREATE TABLE IF NOT EXISTS " + TABLE_NAME.connections.getName() +
                        "(id INTEGER PRIMARY KEY, " +
                        "pseudo VARCHAR(64) NOT NULL, " +
                        "first INTEGER, " +
                        "last INTEGER);",
                    "CREATE INDEX IF NOT EXISTS idx_pseudo ON " + TABLE_NAME.connections.getName() + " (pseudo);"
                };
                break;
            case UNKNOWN:
            default:
                throw new IllegalStateException("The type of storage is unknown. Please fix the config.");
        }
        createTables(createTableQuery);
        initializeOnlinePlayers();
    }

    private void createTables(final String[] createTableQuery) {
        if(createTableQuery == null) throw new UnsupportedOperationException("The type of storage is unknown. Please fix the config.");
        try (Connection connection = connect()) {
            for(String query : createTableQuery) {
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.executeUpdate();
                }
            }
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Connect to the database.
     *
     * @throws SQLException if an error occurs with the connection.
     * @throws ClassNotFoundException if the driver is not found.
     */
    public Connection connect() throws SQLException, ClassNotFoundException {
        /*

        //Old system without pool connection

        switch(type) {
            case MYSQL:
                Configuration.DatabaseSection db = fraud.getConfiguration().getDatabase();
                Class.forName("com.mysql.jdbc.Driver");
                return DriverManager.getConnection(db.generateURL(), db.getUser(), db.getPassword());
            case SQLITE:
                if(!file.exists()) try {
                    file.createNewFile();
                } catch(IOException e) {
                    e.printStackTrace();
                }
                Class.forName("org.sqlite.JDBC");
                return DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath().replace("\\", File.separator));
            case UNKNOWN:
            default:
                throw new IllegalStateException("The type of storage is unknown. Please fix the config.");
        }*/
        return dataSource.getConnection();
    }
    

    public void initializeOnlinePlayers () {
        try (Connection connection = connect()) {
            Bukkit.getOnlinePlayers().forEach(p -> {
                try {
                    putPlayer(p, connection);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void putPlayer(Player p, Connection connection) throws SQLException {
        InetSocketAddress address = p.getAddress();
        String name = p.getName();

        if (hasPrivateIPv4(p)) {
            fraud.getConsole().sm("The player " + name + " was not added to the database because their IP (" + Utils.getAddress(address) + ") is private.");
            return;
        }

        if (isRegisteredInConnection(name)) {
                String sql = MessageFormat.format("UPDATE {0} SET last = ? WHERE pseudo = ?", TABLE_NAME.connections.getName());
                try (PreparedStatement psst = connection.prepareStatement(sql)) {
                    psst.setLong(1, System.currentTimeMillis());
                    psst.setString(2, name);
                    psst.executeUpdate();
                }
            } else {
                String sql = MessageFormat.format("INSERT INTO {0}(pseudo,first,last) VALUES(?,?,?);", TABLE_NAME.connections.getName());
                try (PreparedStatement psst = connection.prepareStatement(sql)) {
                    psst.setString(1, name);
                    long firstP = p.getFirstPlayed();
                    psst.setLong(2, firstP == 0 ? System.currentTimeMillis() : firstP);
                    psst.setLong(3, System.currentTimeMillis());
                    psst.executeUpdate();
                }
            }

            if (p.hasPermission("fraud.bypass.ip")) return;

            if (isRegisteredInIps(name)) {
                String sql = MessageFormat.format("UPDATE {0} SET ip = ? WHERE pseudo = ?", TABLE_NAME.ips.getName());
                try (PreparedStatement psst = connection.prepareStatement(sql)) {
                    psst.setString(1, Utils.getAddress(address));
                    psst.setString(2, name);
                    psst.executeUpdate();
                }
            } else {
                String sql = MessageFormat.format("INSERT INTO {0}(pseudo,ip) VALUES(?,?);", TABLE_NAME.ips.getName());
                try (PreparedStatement psst = connection.prepareStatement(sql)) {
                    psst.setString(1, name);
                    psst.setString(2, Utils.getAddress(address));
                    psst.executeUpdate();
                }
            }
    }


    /**
     * Insert or update a player in the database.
     *
     * @param p the player who has the ip.
     */
    public void putPlayer(Player p) {
        try (Connection connection = connect()) {
            putPlayer(p, connection);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }



    public void forgotPlayer(String name) {
        String ip = getIP(name);
        cache.get(ip).remove(name);
        String sql = MessageFormat.format("DELETE FROM `{0}` WHERE pseudo = ?", TABLE_NAME.ips.getName());
        try(Connection connection = connect();
            PreparedStatement psst = connection.prepareStatement(sql)) {
            psst.setString(1, name);
            psst.executeUpdate();
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public long getFirstJoin(String pseudo) {
        String sql = MessageFormat.format("SELECT first FROM `{0}` WHERE pseudo = ?", TABLE_NAME.connections.getName());
        try(Connection connection = connect();
            PreparedStatement psst = connection.prepareStatement(sql)) {
            psst.setString(1, pseudo);
            ResultSet rs = psst.executeQuery();
            if(rs.next()) {
                return rs.getLong("first");
            }
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public long getLastJoin(String pseudo) {
        String sql = MessageFormat.format("SELECT last FROM `{0}` WHERE pseudo = ?", TABLE_NAME.connections.getName());
        try(Connection connection = connect();
            PreparedStatement psst = connection.prepareStatement(sql)) {
            psst.setString(1, pseudo);
            ResultSet rs = psst.executeQuery();
            if(rs.next()) {
                return rs.getLong("last");
            }
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Check if a player is registered in the database.
     *
     * @param name the name of the player.
     * @return true if the player is registered, false otherwise.
     */
    public boolean isFullRegistered(String name) {
        return isRegisteredInIps(name) && isRegisteredInConnection(name);
    }

    /**
     * Check if a player is registered in the database.
     *
     * @param name the name of the player.
     * @return true if the player is registered, false otherwise.
     */
    public boolean isRegisteredInConnection(String name) {
        return isRegistered(name, TABLE_NAME.connections.getName());
    }

    /**
     * Check if a player is registered in the database.
     *
     * @param name the name of the player.
     * @return true if the player is registered, false otherwise.
     */
    public boolean isRegisteredInIps(String name) {
        return isRegistered(name, TABLE_NAME.ips.getName());
    }

    /**
     * Check if a player is registered in the database.
     *
     * @param name the name of the player.
     * @return true if the player is registered, false otherwise.
     */
    private boolean isRegistered(String name, String table_name) {
        String sql = MessageFormat.format("SELECT pseudo FROM `{0}` WHERE pseudo = \"{1}\"", table_name, name);
        try(Connection connection = connect();
            PreparedStatement psst = connection.prepareStatement(sql)) {
            ResultSet rs = psst.executeQuery();
            if(!rs.next()) return false;
            return rs.getString("pseudo").equals(name);
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get the ip of a player.
     *
     * @param name the name of the player.
     * @return the ip of the player.
     */
    public String getIP(String name) {
        String sql = "SELECT ip FROM `" + TABLE_NAME.ips.getName() + "` WHERE pseudo = ?";
        try(Connection connection = connect();
            PreparedStatement psst = connection.prepareStatement(sql)) {
            psst.setString(1, name);
            ResultSet rs = psst.executeQuery();
            if(rs.next()) {
                return rs.getString("ip");
            }
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.err.println("Error while getting the ip of " + name + " in the database.");
        return null;
    }

    public List<String> getListByPseudo(String pseudo) {
        String ipFromPseudo = null;

        String sql_getIp = "SELECT ip FROM `" + TABLE_NAME.ips.getName() + "` WHERE pseudo = ?";
        try(Connection connection = connect();
            PreparedStatement psst_getIp = connection.prepareStatement(sql_getIp)) {
            psst_getIp.setString(1, pseudo);
            ResultSet rs_getIp = psst_getIp.executeQuery();
            while(rs_getIp.next()) {
                ipFromPseudo = rs_getIp.getString("ip");
            }
            if(ipFromPseudo == null) {
                return Lists.newArrayList();
            }
            return getList(ipFromPseudo);
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Lists.newArrayList();
    }

    /**
     * Get the list of players from an ip.
     *
     * @param player the player.
     * @return the list of players.
     */
    public List<String> getList(Player player) {
        return getList(player.getAddress());
    }

    /**
     * Get the list of players from an ip.
     *
     * @param address the ip of the players.
     * @return the list of players.
     */
    public List<String> getList(InetSocketAddress address) {
        return getList(Utils.getAddress(address));
    }

    /**
     * Get the list of players from an ip.
     *
     * @param address the ip of the players.
     * @return the list of players.
     */
    public List<String> getList(String address) {
        // Vérifiez d'abord si les données sont dans le cache
        if (cache.containsKey(address))
            if (isCacheStale()) {
                cache.clear();
                lastCacheRefreshTime = System.currentTimeMillis();
            } else return cache.get(address);

        String sql = MessageFormat.format("SELECT pseudo FROM `{0}` WHERE ip = ?", TABLE_NAME.ips.getName());
        // Si non, interrogez la base de données
        try (Connection connection = connect();
            PreparedStatement psst = connection.prepareStatement(sql)) {
            psst.setString(1, address);
            ResultSet rs = psst.executeQuery();
            List<String> pseudos = new ArrayList<>();
            while (rs.next()) {
                pseudos.add(rs.getString("pseudo"));
            }

            // Mettez les données dans le cache avant de retourner
            cache.put(address, pseudos);
            return pseudos;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private boolean isCacheStale() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastCacheRefreshTime) >= cacheRefreshInterval;
    }


    public long ping() {
        if(type.equals(Configuration.DatabaseSection.Type.SQLITE))
            return (file.canRead() && file.canWrite()) ? 0 : -1;

        long start = System.currentTimeMillis();
        try(Connection connect = connect()) {
            return System.currentTimeMillis()-start;
        } catch(SQLException | ClassNotFoundException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Get all players with the same ip.
     * @return all players with the same ip.
     */
    public ConcurrentHashMap<String, List<String>> getAllPlayersWDA_String() {
        ConcurrentHashMap<String, List<String>> output = new ConcurrentHashMap<>();
        for(Player p: Bukkit.getOnlinePlayers()) {
            if(p.hasPermission("fraud.bypass.ip")) continue;
            List<String> alts = getList(p); // la liste de tout les pseudos.
            if(alts.size() >= 2) {

                if(!output.containsKey(Utils.getAddress(p.getAddress()))) {
                    output.put(Utils.getAddress(p.getAddress()), Lists.newArrayList());
                }
                output.get(Utils.getAddress(p.getAddress())).addAll(alts);
            }
        }
        return output;
    }

    /**
     * Get all players with the same ip.
     * @return all players with the same ip.
     */
    public ConcurrentHashMap<String, List<OfflinePlayer>> getAllPlayersWDA_OfflinePlayer() {
        ConcurrentHashMap<String, List<OfflinePlayer>> output = new ConcurrentHashMap<>();
        for(Player p: Bukkit.getOnlinePlayers()) {
            if(p.hasPermission("fraud.bypass.ip")) continue;
            List<String> alts = getList(p); // la liste de tout les pseudos.
            if(alts.size() >= 2/* && Utils.cantGetAnAlt(alts)*/) {
                if(!output.containsKey(Utils.getAddress(p.getAddress()))) {
                    output.put(Utils.getAddress(p.getAddress()), Lists.newArrayList());
                }
                for(String alt: alts) {
                    output.get(Utils.getAddress(p.getAddress())).add(Bukkit.getOfflinePlayer(alt));
                }
            }
        }
        return output;
    }

    public File getFile() {
        return file;
    }

    /**
     * Return true if the player ip is public, false otherwise.
     * @param p player to check.
     * @return true if the player ip is public, false otherwise.
     */
    public boolean hasPrivateIPv4(final Player p) {
        return !Utils.isPublicIPV4Address(Utils.getAddress(p.getAddress()));
    }
}
