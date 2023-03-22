package fr.Rgld_.Fraud.Global;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.Rgld_.Fraud.Spigot.Fraud;
import fr.Rgld_.Fraud.Spigot.Helpers.ExtAPI;
import fr.Rgld_.Fraud.Spigot.Helpers.Utils;

import java.text.MessageFormat;
import java.util.HashMap;

/**
 * Used by the /fd info to get some information about an ip
 */
public class IPInfoManager {

    private final HashMap<String, IPInfo> ipInfoHashMap;
    private final Fraud fraud;
    private final ExtAPI extAPI;

    public IPInfoManager(Fraud fraud, ExtAPI extAPI) {
        this.fraud = fraud;
        this.extAPI = extAPI;
        this.ipInfoHashMap = new HashMap<>();
    }

    public HashMap<String, IPInfo> getIpInfoMap() {
        return ipInfoHashMap;
    }


    public IPInfo getIpInfo(String ip, boolean geoip) {
        IPInfo ipInfo = new IPInfo();
        ipInfo.setIP(ip);

        String[] informationValue = getJsonGeoIPInfo(ip, geoip);
        if(informationValue[1].equals("200")) {
            JsonObject gip = JsonParser.parseString(informationValue[0]).getAsJsonObject().getAsJsonObject("data");
            if(geoip) {
                ipInfo.setContinent(gip.get("continent").toString());
                ipInfo.setCountryName(gip.get("country").toString());
                ipInfo.setCountryCode(gip.get("country-code").toString());
                ipInfo.setSubDivision(gip.get("sub-division").toString());
                ipInfo.setPostalCode(gip.get("postal-code").toString());
                ipInfo.setCity(gip.get("city").toString());
                ipInfo.setLatitude(gip.get("latitude").toString());
                ipInfo.setLongitude(gip.get("longitude").toString());
            }
            ipInfo.addDesc(gip.get("description").toString());
            ipInfo.setNetname(gip.get("netname").toString());
        } else {
            ipInfo.addDesc("An error occur when trying to get the geolocation this ip. Please contact the developer: /fd contact\nYou can also execute /fd reach-geoapi ", false);
        }
        return ipInfo;
    }

    public IPInfo getIPInfoConformConfig(String ip) {
        if(ipInfoHashMap.containsKey(ip)) return ipInfoHashMap.get(ip);
        boolean geoip = fraud.getConfiguration().isGeoIPAPIActivated();
        IPInfo ipi = getIpInfo(ip, geoip);

        if(!fraud.getConfiguration().isGeoIPAPIActivated()) {
            ipi.delGeoIp(); // This is by security but by default, there is no geo ip information returned by IPInfoManager#getIpInfo(String, boolean)
        }
        ipInfoHashMap.put(ip, ipi);
        return ipi;
    }

    private String[] getJsonGeoIPInfo(String ip, boolean geoip) {
        String base_url = extAPI.restAPIBaseUrl + "ip/{0}?geoip=" + geoip;
        String url = MessageFormat.format(base_url, ip.replace(":", "%3"));
        return Utils.getContent(url, extAPI.restAPIkey, extAPI.getServerUUID());
    }
}
