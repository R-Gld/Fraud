package fr.Rgld_.Fraud.Global;

import com.google.gson.GsonBuilder;
import fr.Rgld_.Fraud.Spigot.Helpers.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;

public class Plugin {

    private final boolean external;
    private final File file;
    private final String description;
    private final long likes;
    private final String sourceCodeLink;
    private final String supportedLanguages;
    private final String[] testedVersions;
    private final Version[] versions;
    private final Long[] updates;
    private final Review[] reviews;
    private final String[] links;
    private final String name;
    private final String tag;
    private final Version version;
    private final long author;
    private final long category;
    private final Rating rating;
    private final Icon icon;
    private final long releaseDate;
    private final long updateDate;
    private final long downloads;
    private final boolean premium;
    private final double price;
    private final long existenceStatus;
    private final long id;

    private Plugin(boolean external, File file, String description, long likes, String sourceCodeLink, String supportedLanguages, String[] testedVersions, Version[] versions, Long[] updates, Review[] reviews, String[] links, String name, String tag, Version version, long author, long category, Rating rating, Icon icon, long releaseDate, long updateDate, long downloads, boolean premium, double price, long existenceStatus, long id) {
        this.external = external;
        this.file = file;
        this.description = description;
        this.likes = likes;
        this.sourceCodeLink = sourceCodeLink;
        this.supportedLanguages = supportedLanguages;
        this.testedVersions = testedVersions;
        this.versions = versions;
        this.updates = updates;
        this.reviews = reviews;
        this.links = links;
        this.name = name;
        this.tag = tag;
        this.version = version;
        this.author = author;
        this.category = category;
        this.rating = rating;
        this.icon = icon;
        this.releaseDate = releaseDate;
        this.updateDate = updateDate;
        this.downloads = downloads;
        this.premium = premium;
        this.price = price;
        this.existenceStatus = existenceStatus;
        this.id = id;
    }

    public boolean isExternal() {
        return external;
    }
    public File getFile() {
        return file;
    }
    public String getDescription() {
        return description;
    }
    public long getLikes() {
        return likes;
    }
    public String getSourceCodeLink() {
        return sourceCodeLink;
    }
    public String getSupportedLanguages() {
        return supportedLanguages;
    }
    public String[] getTestedVersions() {
        return testedVersions;
    }
    public Version[] getVersions() {
        return versions;
    }
    public Long[] getUpdates() {
        return updates;
    }
    public Review[] getReviews() {
        return reviews;
    }
    public String[] getLinks() {
        return links;
    }
    public String getName() {
        return name;
    }
    public String getTag() {
        return tag;
    }
    public Version getVersion() {
        return version;
    }
    public long getAuthor() {
        return author;
    }
    public long getCategory() {
        return category;
    }
    public Rating getRating() {
        return rating;
    }
    public Icon getIcon() {
        return icon;
    }
    public long getReleaseDate() {
        return releaseDate;
    }
    public long getUpdateDate() {
        return updateDate;
    }
    public long getDownloads() {
        return downloads;
    }
    public boolean isPremium() {
        return premium;
    }
    public double getPrice() {
        return price;
    }
    public long getExistenceStatus() {
        return existenceStatus;
    }
    public long getId() {
        return id;
    }

    public static class File {
        private final String type;
        private final long size;
        private final String sizeUnit;
        private final String url;
        private final String externalUrl;

        private File(String type, long size, String sizeUnit, String url, String externalUrl) {
            this.type = type;
            this.size = size;
            this.sizeUnit = sizeUnit;
            if (!url.startsWith("https://") && !url.startsWith("http://")) {
                url = "https://www.spigotmc.org/" + url;
            }
            this.url = url;
            this.externalUrl = externalUrl;
        }

        public String getType() {
            return type;
        }
        public long getSize() {
            return size;
        }
        public String getSizeUnit() {
            return sizeUnit;
        }
        public String getUrl() {
            return url;
        }
        public String getExternalUrl() {
            return externalUrl;
        }

        public static File parseFile(JSONObject obj) {
            return new File((String) obj.get("type"), (long) obj.get("size"), (String) obj.get("sizeUnit"), (String) obj.get("url"), (String) obj.get("externalUrl"));
        }

        @Override
        public String toString() {
            return new GsonBuilder().setPrettyPrinting().create().toJson(this);
        }

    }

    public static class Version {
        private final long pluginId;
        private final long id;
        private final String uuid;

        private Version(long pluginId, long id, String uuid) {
            this.pluginId = pluginId;
            this.id = id;
            this.uuid = uuid;
        }

        public long getId() {
            return id;
        }
        public String getUuid() {
            return uuid;
        }

        public Data getData() throws IOException {
            String[] datas = Utils.getContent("https://api.spiget.org/v2/resources/" + pluginId + "/versions/" + id);
            checkHTMLCode(Integer.parseInt(datas[1]), pluginId);
            String content = datas[0];

            JSONObject obj;
            try {
                obj = (JSONObject) new JSONParser().parse(content);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return new Data((Integer) obj.get("downloads"), Rating.parseRating((JSONObject) obj.get("rating")), (String) obj.get("name"), (Long) obj.get("releaseDate"));
        }
        
        public static Version parseVersion(JSONObject obj, int pluginId) {
            return new Version(pluginId, (long) obj.get("id"), (String) obj.get("uuid"));
        }

        public static class Data {

            private final long downloads;
            private final Rating rating;
            private final String name;
            private final long releaseDate;

            public Data(long downloads, Rating rating, String name, long releaseDate) {
                this.downloads = downloads;
                this.rating = rating;
                this.name = name;
                this.releaseDate = releaseDate;
            }

            public long getDownloads() {
                return downloads;
            }
            public Rating getRating() {
                return rating;
            }
            public String getName() {
                return name;
            }
            public long getReleaseDate() {
                return releaseDate;
            }


            @Override
            public String toString() {
                return new GsonBuilder().setPrettyPrinting().create().toJson(this);
            }

        }


        @Override
        public String toString() {
            return new GsonBuilder().setPrettyPrinting().create().toJson(this);
        }

    }

    public static class Rating {
        private final long count;
        private final double average;

        private Rating(long count, double average) {
            this.count = count;
            this.average = average;
        }

        public long getCount() {
            return count;
        }
        public double getAverage() {
            return average;
        }

        public static Rating parseRating(JSONObject obj) {
            return new Rating((Long) obj.get("count"), Double.parseDouble("" + obj.get("average")));
        }


        @Override
        public String toString() {
            return new GsonBuilder().setPrettyPrinting().create().toJson(this);
        }

    }

    public static class Icon {
        private final String url;
        private final String data;
        private transient final BufferedImage dataImage;
        private final String info;
        private final String hash;

        private Icon(String url, String data, String info, String hash) {
            this.url = "https://www.spigotmc.org/" + url;
            this.data = data;

            BufferedImage dataImg = null;
            byte[] imageByte;
            try {
                BASE64Decoder decoder = new BASE64Decoder();
                imageByte = decoder.decodeBuffer(data);
                ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
                dataImg = ImageIO.read(bis);
                bis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            this.dataImage = dataImg;
            this.info = info;
            this.hash = hash;
        }

        public String getUrl() {
            return url;
        }
        public String getData() {
            return data;
        }
        public BufferedImage getDataImage() {
            return dataImage;
        }
        public String getInfo() {
            return info;
        }
        public String getHash() {
            return hash;
        }

        public static Icon parseIcon(JSONObject obj) {
            return new Icon((String) obj.get("url"), (String) obj.get("data"), (String) obj.get("info"), (String) obj.get("hash"));
        }


        @Override
        public String toString() {
            return new GsonBuilder().setPrettyPrinting().create().toJson(this);
        }

    }

    public static class Author {

        private final long id;
        private final String name;
        private final Icon icon;

        public Author(long id, String name, Icon icon) {
            this.id = id;
            this.name = name;
            this.icon = icon;
        }

        public long getId() {
            return id;
        }
        public String getName() {
            return name;
        }
        public Icon getIcon() {
            return icon;
        }

        private static Author parseAuthor(JSONObject obj) {
            return new Author((Long) obj.get("id"), (String) obj.get("name"), Icon.parseIcon((JSONObject) obj.get("icon")));
        }

        public static Author getAuthor(long id) {
            String[] datas = Utils.getContent("https://api.spiget.org/v2/authors/" + id);
            Plugin.checkHTMLCode(Integer.parseInt(datas[1]));
            String content = datas[0];
            JSONObject obj;
            try {
                obj = (JSONObject) new JSONParser().parse(content);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return parseAuthor(obj);
        }


    }


    public static class Review {

        private final long author;
        private final Rating rating;
        private final String message;
        private final String version;
        private final long date;
        private final long resource;
        private final String responseMessage;

        public Review(long author, Rating rating, String message, String version, long date, long resource, String responseMessage) {
            this.author = author;
            this.rating = rating;
            this.message = new String(Base64.getDecoder().decode(message.getBytes()));
            this.version = version;
            this.date = date;
            this.resource = resource;
            this.responseMessage = responseMessage == null ? null : new String(Base64.getDecoder().decode(responseMessage.getBytes()));
        }

        public long getAuthor() {
            return author;
        }
        public Rating getRating() {
            return rating;
        }
        public String getMessage() {
            return message;
        }
        public String getVersion() {
            return version;
        }
        public long getDate() {
            return date;
        }
        public long getResource() {
            return resource;
        }
        public String getResponseMessage() {
            return responseMessage;
        }

        public static Review parseReview(JSONObject obj) {
            return new Review((long) ((JSONObject) obj.get("author")).get("id"),
                    Rating.parseRating((JSONObject) obj.get("rating")),
                    (String) obj.get("message"),
                    (String) obj.get("version"),
                    (long) obj.get("date"),
                    (long) obj.get("resource"),
                    (String) obj.get("responseMessage"));
        }


        @Override
        public String toString() {
            return new GsonBuilder().setPrettyPrinting().create().toJson(this);
        }

    }


    private static final int FRAUD_SPIGOT_PLUGIN_ID = 69872;

    public static Plugin getFraud() { return getPlugin(FRAUD_SPIGOT_PLUGIN_ID); }

    public static Plugin getPlugin(int pluginId) {
        String[] datas = Utils.getContent("https://api.spiget.org/v2/resources/" + pluginId);
        checkHTMLCode(Integer.parseInt(datas[1]), pluginId);
        String content = datas[0];

        JSONObject obj;
        try {
            obj = (JSONObject) new JSONParser().parse(content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        boolean external = (boolean) obj.get("external");
        File file = File.parseFile((JSONObject) obj.get("file"));
        String description = (String) obj.get("description");
        long likes = (long) obj.get("likes");
        String sourceCodeLink = (String) obj.get("sourceCodeLink");
        String supportedLanguages = (String) obj.get("supportedLanguages");

        String[] testedVersion = (String[]) ((JSONArray) obj.get("testedVersions")).toArray(new String[0]);

        ArrayList<Version> versionsAL = new ArrayList<>();
        for (Object o : (JSONArray) obj.get("versions")) versionsAL.add(Version.parseVersion((JSONObject) o, pluginId));
        Version[] versions = versionsAL.toArray(new Version[0]);

        ArrayList<Long> updatesAL = new ArrayList<>();
        for (Object o : (JSONArray) obj.get("updates")) updatesAL.add((long) ((JSONObject) o).get("id"));
        Long[] updates = updatesAL.toArray(new Long[0]);

        ArrayList<String> links = new ArrayList<>();
        for (Object o : ((JSONObject) obj.get("links")).entrySet()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) o;
            String url = entry.getValue();
            if (!url.startsWith("https://") && !url.startsWith("http://")) {
                url = "https://www.spigotmc.org/" + url;
            }
            links.add(url);
        }
        String name = (String) obj.get("name");
        String tag = (String) obj.get("tag");
        Version version = Version.parseVersion((JSONObject) obj.get("version"), pluginId);
        long author = (long) ((JSONObject) obj.get("author")).get("id");
        long category = (long) ((JSONObject) obj.get("category")).get("id");
        Rating rating = Rating.parseRating((JSONObject) obj.get("rating"));
        Icon icon = Icon.parseIcon((JSONObject) obj.get("icon"));
        long releaseDate = (long) obj.get("releaseDate");
        long updateDate = (long) obj.get("updateDate");
        long downloads = (long) obj.get("downloads");
        boolean premium = (boolean) obj.get("premium");
        double price = Double.parseDouble("" + obj.get("price"));
        long existenceStatus = (long) obj.get("existenceStatus");


        /* Reviews part */
        String[] reviewsDatas = Utils.getContent("https://api.spiget.org/v2/resources/" + pluginId + "/reviews");
        checkHTMLCode(Integer.parseInt(reviewsDatas[1]), pluginId);
        String reviewsContent = reviewsDatas[0];

        JSONArray arr;
        try {
            arr = (JSONArray) new JSONParser().parse(reviewsContent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Review[] reviews = new Review[arr.size()];

        for (int i = 0; i < arr.size(); i++) {
            Object o = arr.get(i);
            Review review = Review.parseReview((JSONObject) o);
            reviews[i] = review;
        }

        return new Plugin(external, file, description, likes, sourceCodeLink, supportedLanguages, testedVersion, versions, updates, reviews, links.toArray(new String[0]), name, tag, version, author, category, rating, icon, releaseDate, updateDate, downloads, premium, price, existenceStatus, pluginId);
    }

    public static void checkHTMLCode(int htmlCode) {
        if(htmlCode != 200) {
            throw new RuntimeException("Error occuring getting information about a plugin.");
        }
    }

    private static void checkHTMLCode(int htmlCode, long pluginId) {
        if(htmlCode != 200) {
            throw new RuntimeException("Error occuring during getting informations about the plugin with id: " + pluginId);
        }
    }

    @Override
    public String toString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }
}
