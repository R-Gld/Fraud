package fr.Rgld_.Fraud.Spigot.Helpers;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public enum Links {

    BASE_RGLD_API("https://api.rgld.fr/", false),
    BASE_ANALYTICS("https://url.rgld.fr/", false),
    BASE_SPIGET_API("https://api.spiget.org/v2/", false),

    PERSONNAL_TWITTER(BASE_ANALYTICS.link + "twitter", false),
    ADD_DISCORD(BASE_ANALYTICS.link + "add-discord", false),
    GITHUB(BASE_ANALYTICS.link + "github", false),
    MAIL(BASE_ANALYTICS.link + "mail", false),


    FRAUD_SPIGOT(BASE_ANALYTICS.link + "fraud-spg", false),
    FRAUD_SOURCECODE(BASE_ANALYTICS.link + "fraud-sc", false),
    FRAUD_ISSUES(BASE_ANALYTICS.link + "fraud-issues", false),
    FRAUD_DOWNLOAD(BASE_ANALYTICS.link + "fraud-dl", false),
    FRAUD_DOWNLOAD_WITHOUT_ANALYTICS(BASE_RGLD_API.link + "fraud/download", false),
    FRAUD_DOWNLOAD_WITHOUT_ANALYTICS_VERSION(BASE_RGLD_API.link + "fraud/download?version={0}", true),

    RGLD_API_STATS(BASE_RGLD_API.link + "fraud/stats/", false),
    RGLD_API_OWN_IP(BASE_RGLD_API.link + "ip/own", false),
    RGLD_API_ASK_HELP(BASE_RGLD_API.link + "fraud/askHelp",  false),
    RGLD_API_REACH(BASE_RGLD_API.link + "reach",  false),

    SPIGET_RESOURCE(BASE_SPIGET_API.link + "resources/{0}", true),
    SPIGET_AUTHORS(BASE_SPIGET_API.link + "authors/{0}", true),
    SPIGET_VERSIONS(BASE_SPIGET_API.link + "resources/{0}/versions/{1}",  true),
    SPIGET_REVIEWS(BASE_SPIGET_API.link + "resources/{0}/reviews", true)
    ;

    private final String link;
    private final boolean needFormat;

    Links(String link, boolean needFormat) {
        this.link = link;
        this.needFormat = needFormat;
    }

    @Override
    public String toString() {
        return link;
    }

    public String getUrl() {
        return link;
    }

    public Collection<Links> getBasesLinks() {
        Collection<Links> output = new ArrayList<>();
        for (Links value : Links.values()) {
            if(value.name().contains("BASE")) output.add(value);
        }
        return output;
    }

    public String format(Object... args) {
        if(!needFormat) throw new UnsupportedOperationException("This link doesn't need to be formatted.");
        List<String> argsCol = new ArrayList<>();
        for (Object arg : args) {
            argsCol.add(String.valueOf(arg));
        }
        return MessageFormat.format(link, argsCol.toArray());
    }
}
