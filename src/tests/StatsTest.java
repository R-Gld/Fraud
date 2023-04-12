import com.google.gson.GsonBuilder;
import fr.Rgld_.Fraud.Spigot.Helpers.Links;
import fr.Rgld_.Fraud.Spigot.Helpers.Utils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StatsTest {

    public static void main(String[] args) throws IOException {
        System.out.println(getDownloadLink());
    }


    public static String getDownloadLink() throws IOException {
        String url = Links.BASE_SPIGET_API + "resources/69872";
        String[] infos = Utils.getContent(url);
        if (Integer.parseInt(infos[1]) != 200) {
            throw new IOException("An error occur during getting the download link of Fraud.");
        }

        String content = infos[0];
        JSONObject obj;
        try {
            obj = (JSONObject) new JSONParser().parse(content);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        JSONObject file = (JSONObject) obj.get("file");
        if(file.get("type").equals("external"))
            return (String) file.get("externalUrl");
        else return "error";
    }

    public static class CommonClassForTests {
        final String name;
        final int code;
        final List<Integer> list;

        public CommonClassForTests(String name, int code) {
            this.name = name;
            this.code = code;
            this.list = new ArrayList<>();
            for (int i = 0; i <15; i++) {
                list.add(i);
            }
        }

        @Override
        public String toString() {
            return new GsonBuilder().create().toJson(this);
        }
    }

}
