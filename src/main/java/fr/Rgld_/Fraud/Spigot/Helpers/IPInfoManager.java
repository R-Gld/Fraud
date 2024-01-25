package fr.Rgld_.Fraud.Spigot.Helpers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.Rgld_.Fraud.Spigot.Fraud;

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

    /**
     * Getter of the ipInfoHashMap
     * @return the ipInfoHashMap
     */
    public HashMap<String, IPInfo> getIpInfoMap() {
        return ipInfoHashMap;
    }


    /**
     * Get the information of an ip.
     * @param ip the ip to get the information
     * @param geoip if the geoip information should be returned
     * @return the information of the ip
     */
    public IPInfo getIpInfo(String ip, boolean geoip) {
        IPInfo ipInfo = new IPInfo();
        ipInfo.setIP(ip);

        if(!Utils.isPublicIPV4Address(ip)) {
            ipInfo.addDesc(" Local IP, no information available.");
            return ipInfo;
        }

        String[] informationValue = getJsonGeoIPInfo(ip, geoip);

        JsonParser parser = new JsonParser();

        switch(Integer.parseInt(informationValue[1])){
            case 200:
                JsonObject gip = parser.parse(informationValue[0]).getAsJsonObject().getAsJsonObject("data");
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
                break;
            case 404:
                ipInfo.addDesc("The ip address was not found in the database (" + ip + ").", false);
                break;
            case 400:
                ipInfo.addDesc("The ip address is not valid (" + ip + ").", false);
                break;
            default:
                ipInfo.addDesc("An error occur when trying to get the geolocation this ip. Please contact the developer: /fd contact\nYou can also execute /fd reach-geoapi ", false);
                break;
        }
        return ipInfo;
    }

    /**
     * Get the information of an ip.
     * @param ip the ip to get the information
     * @return the information of the ip
     */
    public IPInfo getIPInfoConformConfig(String ip) {
        if(ipInfoHashMap.containsKey(ip)) return ipInfoHashMap.get(ip);
        boolean geoip = fraud.getConfiguration().isGeoIPAPIActivated();
        IPInfo ipInfo = getIpInfo(ip, geoip);

        if(!fraud.getConfiguration().isGeoIPAPIActivated()) {
            ipInfo.delGeoIp(); // This is by security but by default, there is no geo ip information returned by IPInfoManager#getIpInfo(String, boolean)
        }
        ipInfoHashMap.put(ip, ipInfo);
        return ipInfo;
    }

    private String[] getJsonGeoIPInfo(String ip, boolean geoip) {
        String base_url = extAPI.restAPIBaseUrl + "ip/{0}?geoip=" + geoip;
        String url = MessageFormat.format(base_url, ip.replace(":", "%3"));
        return Utils.getContent(url, extAPI.restAPIkey, extAPI.getServerUUID());
    }
}
