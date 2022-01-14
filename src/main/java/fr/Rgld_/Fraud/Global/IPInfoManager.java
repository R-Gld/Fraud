package fr.Rgld_.Fraud.Global;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.Rgld_.Fraud.Spigot.Fraud;
import fr.Rgld_.Fraud.Spigot.Helpers.Utils;

import java.text.MessageFormat;
import java.util.HashMap;

/**
 * Used by the /fd info to get some information about an ip
 */
public class IPInfoManager {

    private final HashMap<String, IPInfo> ipInfoHashMap;
    private final Fraud fraud;

    public IPInfoManager(Fraud fraud) {
        this.fraud = fraud;
        this.ipInfoHashMap = new HashMap<>();
    }

    public HashMap<String, IPInfo> getIpInfoMap() {
        return ipInfoHashMap;
    }


    public static IPInfo getIpInfo(String ip) {
        IPInfo ipInfo = new IPInfo();
        ipInfo.setIP(ip);

        String[] geolocation = getJsonGeoIPInfo(ip);
        if(geolocation[1].equals("200")) {
            JsonObject gip = JsonParser.parseString(geolocation[0]).getAsJsonObject().getAsJsonObject("data");
            ipInfo.setContinent(gip.get("continent").toString());
            ipInfo.setSubDivision(gip.get("sub-division").toString());
            ipInfo.setCity(gip.get("city").toString());
            ipInfo.setPostalCode(gip.get("postal-code").toString());
            ipInfo.setLatitude(gip.get("latitude").toString());
            ipInfo.setLongitude(gip.get("longitude").toString());
            ipInfo.setCountryName(gip.get("country").toString());
            ipInfo.setCountryCode(gip.get("country-code").toString());

            ipInfo.addDesc(gip.get("description").toString());
            ipInfo.setNetname(gip.get("netname").toString());
        } else {
            ipInfo.addDesc("An error occur when trying to geolocation this ip. Please contact the developer: /fd contact");
        }
        return ipInfo;
    }

    public IPInfo getIPInfoConformConfig(String ip) {
        if(ipInfoHashMap.containsKey(ip)) return ipInfoHashMap.get(ip);
        IPInfo ipi = IPInfoManager.getIpInfo(ip);

        if(!fraud.getConfiguration().isGeoIPAPIActivated()) {
            ipi.delContinent().delSubDivision().delCity().delPostalCode().delLatitude().delLongitude();
        }
        ipInfoHashMap.put(ip, ipi);
        return ipi;
    }

    private static String[] getJsonGeoIPInfo(String ip) {
        String base_url = Fraud.restAPIBaseUrl + "/api/geoip/{0}";
        String url = MessageFormat.format(base_url, ip.replace(":", "%3"));
        return Utils.getContent(url, "edGfJSQqavVTWmzQ");
    }
}
