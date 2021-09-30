package fr.Rgld_.Fraud.Helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class IPInfo {

    private List<String> desc = new ArrayList<>();
    private String ip;
    private String netname;
    private String country;
    private String from;

    public IPInfo(String ip, String netname, String country, String from, String... desc) {
        this.ip = ip;
        this.netname = netname;
        this.country = country;
        this.from = from;
        this.desc = new ArrayList<>(Arrays.asList(desc));
    }

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
        return desc;
    }
}
