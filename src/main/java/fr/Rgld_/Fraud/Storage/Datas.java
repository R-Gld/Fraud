package fr.Rgld_.Fraud.Storage;

import fr.Rgld_.Fraud.Fraud;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("ALL")
public class Datas {

    private final Fraud fraud;
    private File file = new File(Fraud.getInstance().getDataFolder(), "data.json");

    private JSONObject ips = new JSONObject();
    private JSONObject preference = new JSONObject();

    private HashMap<String, List<String>> allDatas;

    public Datas(Fraud fraud) throws Throwable {
        this.fraud = fraud;
        this.allDatas = new HashMap<>();
        loadConfig();
    }

    public File getFile() {
        return file;
    }

    private void loadConfig() throws Throwable {
        if (!file.exists()) {
            file.createNewFile();
        } else {
            try (FileReader reader = new FileReader(file)) {
                JSONParser parser = new JSONParser();
                Object obj = parser.parse(reader);
                JSONArray jsonArray = (JSONArray) obj;
                ips = (JSONObject) jsonArray.get(0);
                preference = (JSONObject) jsonArray.get(1);
            }
        }
    }

    private void saveConfig() {
        JSONArray array = new JSONArray();
        array.add(ips);
        array.add(preference);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(array.toJSONString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void putPlayer(Player p) {
        if (p.hasPermission("fraud.bypass.ip")) return;
        String address = getAddress(p.getAddress());
        List<String> players = getListByAddress(address);
        if (null == players)
            players = new ArrayList<>();
        String name = p.getName();
        if (players.contains(name)) return;
        JSONObject obj = new JSONObject();
        obj.put("ips", players);
        players.add(name);
        putListByIp(address, players);
    }

    public List<String> getListByPseudo(String pseudo) {
        for (Object obj : ips.values()) {
            List<String> list = (List<String>) obj;
            List<String> lcList = new ArrayList<>();
            list.forEach(str -> {
                lcList.add(str.toLowerCase());
            });
            if (lcList.contains(pseudo.toLowerCase())) {
                return list;
            }
        }
        return null;
    }

    public List<String> getListByPlayer(Player player) {
        return getListByAddress(getAddress(player.getAddress()));
    }

    public List<String> getListByAddress(String address) {
        List<String> list = new ArrayList<>();
        if(ips.get(address) != null){
            list = (List<String>) ips.get(address);
        }
        return list;
    }

    private void putListByIp(String address, List<String> list) {
        if (null == ips) ips = new JSONObject();
        ips.remove(address);
        for (int i = 0; i < list.size() - 1; i++) {
            if(list.get(i).contains("§"))
                list.remove(i);
        }
        ips.put(address, list);
        saveConfig();
    }

    private String getAddress(InetSocketAddress address) {
        return address.toString().split(":")[0].substring(1);
    }

}
