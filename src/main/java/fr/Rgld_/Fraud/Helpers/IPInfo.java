package fr.Rgld_.Fraud.Helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Object that store the information of an ip
 */
public class IPInfo {

    private final List<String> desc = new ArrayList<>();
    private String ip;
    private String netname;
    private String country;
    private String from;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public IPInfo(){}

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getNetname() {
        return netname;
    }

    public void setNetname(String netname) {
        this.netname = netname;
    }

    @Override
    public String toString() {
        return "IPInfo{" +
                "ip='" + ip + '\'' +
                ", netname='" + netname + '\'' +
                ", country='" + country + '\'' +
                ", from='" + from + '\'' +
                '}';
    }

    public void addDesc(String substring) {
        desc.add(substring);
    }

    public Collection<String> getDesc() {
        return Collections.unmodifiableCollection(desc);
    }
}
