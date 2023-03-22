package fr.Rgld_.Fraud.Global;

import org.jetbrains.annotations.NotNull;

import java.util.*;


public final class IPInfo {

    private final List<String> desc = new ArrayList<>();
    private String ip = null;
    private String netname = null;
    private String continent = null;
    private String countryName = null;
    private String countryCode = null;
    private String subDivision = null;
    private String city = null;
    private String postalCode = null;
    private String latitude = null;
    private String longitude = null;

    public String getIP() {
        return ip;
    }
    public void setIP(String ip) {
        this.ip = ip;
    }

    public String getNetname() {
        return netname;
    }
    public void setNetname(String netname) {
        this.netname = format(netname);
    }

    public Collection<String> getDesc() {
        return Collections.unmodifiableCollection(desc);
    }

    public void addDesc(String substring) {
        addDesc(substring, true);
    }

    public void addDesc(String substring, boolean format) {
        if(format) {
            desc.add(format(substring));
        } else {
            desc.add(substring);
        }
    }

    public String getCountryName() {
        return countryName;
    }
    public void setCountryName(String countryName) {
        this.countryName = format(countryName);
    }

    public String getCountryCode() {
        return countryCode;
    }
    public void setCountryCode(String countryCode) {
        this.countryCode = format(countryCode);
    }

    public String getLatitude() {
        return latitude;
    }
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getSubDivision() {
        return subDivision;
    }
    public void setSubDivision(String subDivision) {
        this.subDivision = format(subDivision);
    }

    public String getPostalCode() {
        return postalCode;
    }
    public void setPostalCode(String postalCode) {
        this.postalCode = format(postalCode);
    }

    public String getContinent() {
        return continent;
    }
    public void setContinent(String continent) {
        this.continent = format(continent);
    }

    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = format(city);
    }

    private @NotNull String format(String text) {
        return !Objects.equals(text, "null") ? text.substring(1, text.length()-1) : text;
    }

    public IPInfo delContinent() {
        this.continent = null;
        return this;
    }

    public IPInfo delSubDivision() {
        this.subDivision = null;
        return this;
    }

    public IPInfo delCity() {
        this.city = null;
        return this;
    }

    public IPInfo delPostalCode() {
        this.postalCode = null;
        return this;
    }

    public IPInfo delLatitude() {
        this.latitude = null;
        return this;
    }

    public IPInfo delLongitude() {
        this.longitude = null;
        return this;
    }

    public void delGeoIp() {
        this.continent = null;
        this.subDivision = null;
        this.city = null;
        this.postalCode = null;
        this.latitude = null;
        this.longitude = null;
    }
}
