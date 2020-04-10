package fr.Rgld_.Fraud.Storage;

import com.google.common.collect.Lists;
import fr.Rgld_.Fraud.Fraud;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.*;
import java.text.MessageFormat;
import java.util.List;

@SuppressWarnings("ALL")
public class Datas {

    private final Fraud fraud;
    private final File file;
    private final String TABLE_NAME_ips = "ips";
    private final String TABLE_NAME_connection = "connections";

    public Datas(Fraud fraud) throws Throwable {
        this.fraud = fraud;
        file = new File(fraud.getDataFolder(), "data.sqlite");
        createIpsTable();
        createConnectionTable();
    }

    private Connection connect() throws SQLException, ClassNotFoundException {
        File dataFolder = fraud.getDataFolder();
        if(!file.exists()) try {
            file.createNewFile();
        } catch(IOException e) {
            e.printStackTrace();
        }

        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath().replace("\\", File.separator));
    }


    private void createIpsTable() {
        try(Connection connection = connect()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_ips + "(id integer PRIMARY KEY,pseudo text NOT NULL,ip text NOT NULL);";
            connection.createStatement().execute(sql);
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private void createConnectionTable() {
        try(Connection connection = connect()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_connection + "(id integer PRIMARY KEY NOT NULL,pseudo text NOT NULL,first bigint,last bigint);";
            connection.createStatement().execute(sql);
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public File getFile() {
        return file;
    }

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
                psst.setString(1, getAddress(address));
                psst.setString(2, name);
                psst.executeUpdate();
            } else {
                String sql = MessageFormat.format("INSERT INTO {0}(pseudo,ip) VALUES(?,?);", TABLE_NAME_ips);
                PreparedStatement psst = connection.prepareStatement(sql);
                psst.setString(1, name);
                psst.setString(2, getAddress(address));
                psst.executeUpdate();
            }
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void forgotPlayer(Player p) {
        forgotPlayer(p.getName());
    }

    public void forgotPlayer(String name) {
        try(Connection connection = connect()) {
            String sql = MessageFormat.format("DELETE FROM `{0}` WHERE pseudo = ?", TABLE_NAME_ips);
            PreparedStatement psst = connection.prepareStatement(sql);
            psst.setString(1, name);
            psst.executeUpdate();
            String sql2 = MessageFormat.format("DELETE FROM `{0}` WHERE pseudo = ?", TABLE_NAME_connection);
            PreparedStatement psst2 = connection.prepareStatement(sql2);
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

    public boolean isFullRegistered(Player p) {
        return isFullRegistered(p.getName());
    }

    public boolean isFullRegistered(String name) {
        return isRegisteredInIps(name) && isRegisteredInConnection(name);
    }

    public boolean isRegisteredInConnection(String name) {
        try(Connection connection = connect()) {
            String sql = MessageFormat.format("SELECT pseudo FROM `{0}` WHERE pseudo = \"{1}\"", TABLE_NAME_connection, name);
            PreparedStatement psst = connection.prepareStatement(sql);
            ResultSet rs = psst.executeQuery();
            if(!rs.next()) return false;
            return rs.getString("pseudo").equals(name);
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isRegisteredInConnection(Player p) {
        return isRegisteredInConnection(p.getName());
    }

    public boolean isRegisteredInIps(String name) {
        try(Connection connection = connect()) {
            String sql = MessageFormat.format("SELECT pseudo FROM `{0}` WHERE pseudo = \"{1}\"", TABLE_NAME_ips, name);
            PreparedStatement psst = connection.prepareStatement(sql);
            ResultSet rs = psst.executeQuery();
            if(!rs.next()) return false;
            return rs.getString("pseudo").equals(name);
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isRegisteredInIps(Player p) {
        return isRegisteredInIps(p.getName());
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

    public List<String> getList(Player player) {
        return getList(player.getAddress());
    }

    public List<String> getList(InetSocketAddress address) {
        return getList(getAddress(address));
    }

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

    private String getAddress(InetSocketAddress address) {
        return address.toString().split(":")[0].substring(1);
    }
}
