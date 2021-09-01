package fr.Rgld_.Fraud.api;

import java.util.List;

public class FraudPlayer {

    private final int id;
    private final String pseudo;
    private final String ip;
    private final long first;
    private final long last;

    public FraudPlayer(int id, String pseudo, String ip, long first, long last) {
        this.id = id;
        this.pseudo = pseudo;
        this.ip = ip;
        this.first = first;
        this.last = last;
    }

    public int getId() {
        return id;
    }
    public String getPseudo() {
        return pseudo;
    }
    public String getIp() {
        return ip;
    }
    public long getFirst() {
        return first;
    }
    public long getLast() {
        return last;
    }
    public List<String> getAlts() {
        return new Data().getAlts(pseudo);
    }
}
