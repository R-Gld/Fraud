package fr.Rgld_.Fraud.Helpers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;

public class IPInfoManager {

    private final HashMap<String, IPInfo> ipInfoHashMap;

    public IPInfoManager() {
        this.ipInfoHashMap = new HashMap<>();
    }

    public HashMap<String, IPInfo> getIpInfoMap() {
        return ipInfoHashMap;
    }

    public IPInfo getIpInfo(String ip) {
        if(ipInfoHashMap.containsKey(ip)) return ipInfoHashMap.get(ip);
        String whois = getJsonIpInfo(ip);
        JsonParser parser = new JsonParser();
        JsonArray jsonArray = parser.parse(whois).getAsJsonObject().getAsJsonObject("data").getAsJsonArray("records").get(0).getAsJsonArray();
        JsonArray irr_records = parser.parse(whois).getAsJsonObject().getAsJsonObject("data").getAsJsonArray("irr_records").get(1).getAsJsonArray();
        IPInfo ipInfo = new IPInfo();
        ipInfo.setIp(ip);
        for (JsonElement elem : jsonArray) {
            JsonObject obj = elem.getAsJsonObject();
            String key = obj.get("key").toString();
            String value = obj.get("value").toString();
            if(key.equals("remarks")) continue;
            if (key.contains("netname")) ipInfo.setNetname(value.substring(1, value.length()-1));
            if (key.contains("country")) ipInfo.setCountry(value.substring(1, value.length()-1));
            if (key.contains("descr")) ipInfo.addDesc(value.substring(1, value.length()-1));
        }
        for (JsonElement elem : irr_records) {
            JsonObject obj = elem.getAsJsonObject();
            String key = obj.get("key").toString();
            String value = obj.get("value").toString();
            if(key.equals("remarks")) continue;
            if (key.contains("descr")) ipInfo.setFrom(value.substring(1, value.length()-1));
        }
        ipInfoHashMap.put(ip, ipInfo);
        return ipInfo;
    }

    private String getJsonIpInfo(String ip) {
        try {
            String base_url = "https://stat.ripe.net/data/whois/data.json?resource={0}";
            HttpURLConnection con = (HttpURLConnection) new URL(MessageFormat.format(base_url, ip.replace(":", "%3"))).openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "FraudClient");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } catch(IOException e) {
            e.printStackTrace();
            return "ERROR: " + Arrays.toString(e.getStackTrace());
        }
    }
}
