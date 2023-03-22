package fr.Rgld_.Fraud.Spigot.Storage.Data;

import com.google.common.collect.Lists;
import fr.Rgld_.Fraud.Spigot.Fraud;
import fr.Rgld_.Fraud.Spigot.Helpers.Utils;
import fr.Rgld_.Fraud.Spigot.Storage.Configuration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.*;
import java.text.MessageFormat;
import java.util.List;

/**
 * Class used to store datas in the database sqlite of the server.
 */
public class Data {

    private final String TABLE_NAME_ips = "ips";
    private final String TABLE_NAME_connection = "connections";

    private final Fraud fraud;
    private final Configuration.DatabaseSection.Type type;
    private File file = null;

    public Data(Fraud fraud) {
        this.fraud = fraud;
        Configuration.DatabaseSection db = fraud.getConfiguration().getDatabase();
        this.type = db.getType();
        switch(type) {
            case MYSQL:
                try {
                    connect();
                } catch(SQLException e) {
                    System.err.println("There is a problem with the mysql connection." +
                                       "\nYou should check the parameters of the connection with the database." +
                                       "\nHere is the url used to connect to the database: " + db.generateURL() +
                                       "\nHere is the stacktrace:" + e.getMessage());
                    e.printStackTrace();
                } catch(ClassNotFoundException e) {
                    System.out.println("An error occur with the driver:");
                    e.printStackTrace();
                }
                break;
            case SQLITE:
                file = new File(fraud.getDataFolder(), "data.sqlite");
                try {
                    connect();
                } catch(SQLException e) {
                    System.err.println("There is a problem with the sqlite connection." +
                                       "\nPlease send the following stacktrace to the developer of this plugin." +
                                       "\nHere is the stacktrace: " + e.getMessage());
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    System.out.println("An error occur with the driver:");
                    e.printStackTrace();
                }
                break;
            case UNKNOWN:
            default:
                throw new IllegalStateException("The type of storage is unknown. Please fix the config.");
        }

        createIpsTable();
        createConnectionTable();
        createHistoryTable();
    }

    /**
     * Connect to the database.
     *
     * @throws SQLException if an error occur with the connection.
     * @throws ClassNotFoundException if the driver is not found.
     */
    public Connection connect() throws SQLException, ClassNotFoundException {
        switch(type) {
            case MYSQL:
                Configuration.DatabaseSection db = fraud.getConfiguration().getDatabase();
                Class.forName("com.mysql.cj.jdbc.Driver");
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
        }
    }

    /**
     * Create the table ips if it doesn't exist.
     */
    public void createIpsTable() {
        try(Connection connection = connect()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_ips + "(id integer PRIMARY KEY,pseudo text NOT NULL,ip text NOT NULL);";
            connection.createStatement().execute(sql);
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the table history if it doesn't exist.
     */
    public void createHistoryTable() {
        try(Connection connection = connect()) {
            String TABLE_NAME_history = "history";
            String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_history + "(id integer PRIMARY KEY,pseudo text NOT NULL,ip text NOT NULL);";
            connection.createStatement().execute(sql);
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /** Create the table connection if it doesn't exist. */
    public void createConnectionTable() {
        try(Connection connection = connect()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_connection + "(id integer PRIMARY KEY NOT NULL,pseudo text NOT NULL,first bigint,last bigint);";
            connection.createStatement().execute(sql);
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Insert or update a player in the database.
     *
     * @param p the player who has the ip.
     */
    public void putPlayer(Player p) {
        String name = p.getName();
        InetSocketAddress address = p.getAddress();
        try(Connection connection = connect()) {
            if(isRegisteredInConnection(name)) {
                String sql = MessageFormat.format("UPDATE {0} SET last = ? WHERE pseudo = ?", TABLE_NAME_connection);
                PreparedStatement psst = connection.prepareStatement(sql);
                psst.setLong(1, System.currentTimeMillis());
                psst.setString(2, name);
                psst.executeUpdate();
            } else {
                String sql = MessageFormat.format("INSERT INTO {0}(pseudo,first,last) VALUES(?,?,?);", TABLE_NAME_connection);
                PreparedStatement psst = connection.prepareStatement(sql);
                psst.setString(1, name);
                long firstP = p.getFirstPlayed();
                if(firstP == 0) {
                    psst.setLong(2, System.currentTimeMillis());
                } else {
                    psst.setLong(2, firstP);
                }
                psst.setLong(3, System.currentTimeMillis());
                psst.executeUpdate();
            }
            if(p.hasPermission("fraud.bypass.ip")) return;
            if(isRegisteredInIps(name)) {
                String sql = MessageFormat.format("UPDATE {0} SET ip = ? WHERE pseudo = ?", TABLE_NAME_ips);
                PreparedStatement psst = connection.prepareStatement(sql);
                psst.setString(1, Utils.getAddress(address));
                psst.setString(2, name);
                psst.executeUpdate();
            } else {
                String sql = MessageFormat.format("INSERT INTO {0}(pseudo,ip) VALUES(?,?);", TABLE_NAME_ips);
                PreparedStatement psst = connection.prepareStatement(sql);
                psst.setString(1, name);
                psst.setString(2, Utils.getAddress(address));
                psst.executeUpdate();
            }
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void forgotPlayer(String name) {
        try(Connection connection = connect()) {
            String sql = MessageFormat.format("DELETE FROM `{0}` WHERE pseudo = ?", TABLE_NAME_ips);
            PreparedStatement psst = connection.prepareStatement(sql);
            psst.setString(1, name);
            psst.executeUpdate();
            psst.setString(1, name);
            psst.executeUpdate();
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public long getFirstJoin(String pseudo) {
        try(Connection connection = connect()) {
            String sql = MessageFormat.format("SELECT first FROM `{0}` WHERE pseudo = ?", TABLE_NAME_connection);
            PreparedStatement psst = connection.prepareStatement(sql);
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
        try(Connection connection = connect()) {
            String sql = MessageFormat.format("SELECT last FROM `{0}` WHERE pseudo = ?", TABLE_NAME_connection);
            PreparedStatement psst = connection.prepareStatement(sql);
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
        return isRegistered(name, TABLE_NAME_connection);
    }

    /**
     * Check if a player is registered in the database.
     *
     * @param name the name of the player.
     * @return true if the player is registered, false otherwise.
     */
    public boolean isRegisteredInIps(String name) {
        return isRegistered(name, TABLE_NAME_ips);
    }

    /**
     * Check if a player is registered in the database.
     *
     * @param name the name of the player.
     * @return true if the player is registered, false otherwise.
     */
    private boolean isRegistered(String name, String table_name) {
        try(Connection connection = connect()) {
            String sql = MessageFormat.format("SELECT pseudo FROM `{0}` WHERE pseudo = \"{1}\"", table_name, name);
            PreparedStatement psst = connection.prepareStatement(sql);
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
        try(Connection connection = connect()) {
            String sql = MessageFormat.format("SELECT ip FROM `{0}` WHERE pseudo = \"{1}\"", TABLE_NAME_ips, name);
            PreparedStatement psst = connection.prepareStatement(sql);
            ResultSet rs = psst.executeQuery();
            return rs.getString("ip");
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }



    public List<String> getListByPseudo(String pseudo) {
        String ipFromPseudo = null;

        try(Connection connection = connect()) {
            String sql_getIp = "SELECT ip FROM `" + TABLE_NAME_ips + "` WHERE pseudo = ?";
            PreparedStatement psst_getIp = connection.prepareStatement(sql_getIp);
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
        try(Connection connection = connect()) {
            String sql = MessageFormat.format("SELECT pseudo FROM `{0}` WHERE ip = ?", TABLE_NAME_ips);
            PreparedStatement psst = connection.prepareStatement(sql);
            psst.setString(1, address);
            ResultSet rs = psst.executeQuery();
            List<String> pseudos = Lists.newArrayList();
            while(rs.next()) {
                pseudos.add(rs.getString("pseudo"));
            }
            return pseudos;
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Lists.newArrayList();
    }
}
