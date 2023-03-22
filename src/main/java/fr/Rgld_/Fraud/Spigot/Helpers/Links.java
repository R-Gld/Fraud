package fr.Rgld_.Fraud.Spigot.Helpers;

public enum Links {

    PERSONNAL_TWITTER("https://url.rgld.fr/twitter"),
    ADD_DISCORD("https://url.rgld.fr/add-discord"),
    GITHUB("https://url.rgld.fr/github"),
    MAIL("https://url.rgld.fr/mail"),

    FRAUD_SPIGOT("https://url.rgld.fr/fraud-spg"),
    FRAUD_SOURCECODE("https://url.rgld.fr/fraud-sc"),
    FRAUD_DOWNLOAD("https://url.rgld.fr/fraud-dl"),


    SPIGET_API_V2_BASE("https://api.spiget.org/v2/"),
    ;

    private final String link;
    Links(String link) {
        this.link = link;
    }

    @Override
    public String toString() {
        return link;
    }

    public String getLink() {
        return link;
    }
}
