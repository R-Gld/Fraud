package fr.Rgld_.Fraud.Storage;

import com.google.common.collect.Lists;
import fr.Rgld_.Fraud.Fraud;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.*;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("ALL")
public class Datas {

    private final Fraud fraud;
    private final File file;
    private final String TABLE_NAME_ips = "ips";

    public Datas(Fraud fraud) throws Throwable {
        this.fraud = fraud;
        file = new File(fraud.getDataFolder(), "data.sqlite");
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

    private void createConnectionTable() {
        try(Connection connection = connect()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_ips + "(id integer PRIMARY KEY,pseudo text NOT NULL,ip text NOT NULL);";
            connection.createStatement().execute(sql);
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public File getFile() {
        return file;
    }

    public void putPlayer(Player p) {
        if(p.hasPermission("fraud.bypass.ip")) return;
        putPlayer(p.getName(), p.getAddress());
    }

    public void putPlayer(String name, InetSocketAddress address) {
        try(Connection connection = connect()) {
            if(isEverRegistered(name)) {
                String sql = MessageFormat.format("UPDATE {0} SET ip = ? WHERE pseudo = ?", TABLE_NAME_ips);
                PreparedStatement psst = connection.prepareStatement(sql);
                psst.setString(1, getAddress(address));
                psst.setString(2, name);
                psst.executeUpdate();
            } else {
                String sql = MessageFormat.format("INSERT INTO {0}(pseudo,ip) VALUES(?,?);", TABLE_NAME_ips);
                PreparedStatement psst = connection.prepareStatement(sql);
                psst.setString(1, getAddress(address));
                psst.setString(2, name);
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
        } catch(SQLException e) {
            e.printStackTrace();
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean isEverRegistered(Player p) {
        try(Connection connection = connect()) {
            String sql = MessageFormat.format("SELECT ip FROM `{0}` WHERE pseudo = ?", TABLE_NAME_ips);
            PreparedStatement psst = connection.prepareStatement(sql);
            psst.setString(1, p.getName());
            ResultSet rs = psst.executeQuery();
            while(rs.next()) {
                if(rs.getString("ip") != getAddress(p.getAddress())) return true;
            }
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isEverRegistered(String name) {
        try(Connection connection = connect()) {
            String sql = MessageFormat.format("SELECT pseudo FROM `{0}` WHERE pseudo = ?", TABLE_NAME_ips);
            PreparedStatement psst = connection.prepareStatement(sql);
            psst.setString(1, name);
            ResultSet rs = psst.executeQuery();
            while(rs.next()) {
                if(rs.getString("pseudo") != name) return true;
            }
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public HashMap<String, String> getAllDatas() {
        String sql = MessageFormat.format("SELECT * FROM `{0}`", TABLE_NAME_ips);
        HashMap<String, String> map = new HashMap<>();
        try(Connection connection = connect()) {
            ResultSet rs = connection.prepareStatement(sql).executeQuery();
            try {
                while(rs.next()) {
                    map.put(rs.getString("pseudo"), rs.getString("ip"));
                }
            } catch(SQLException ignored) {
            }
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return map;
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
            String sql_getPseudos = "SELECT pseudo FROM `" + TABLE_NAME_ips + "` WHERE ip = ?";
            PreparedStatement psst_getPseudos = connection.prepareStatement(sql_getPseudos);
            psst_getPseudos.setString(1, ipFromPseudo);
            ResultSet rs = psst_getPseudos.executeQuery();
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

    public List<String> getList(Player player) {
        return getList(player.getAddress());
    }

    public List<String> getList(InetSocketAddress address) {
        return getList(getAddress(address));
    }

    public List<String> getList(String address) {
        try(Connection connection = connect()) {
            String sql = "SELECT pseudo FROM `" + TABLE_NAME_ips + "` WHERE ip = ?";
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
