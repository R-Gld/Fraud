import com.google.gson.GsonBuilder;
import fr.Rgld_.Fraud.Spigot.Helpers.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatsTest {

    public static void main(String[] args) {
        System.out.println("Utils.getContent(\"51.210.249.108:11043/api/geoip\", \"edGfJSQqavVTWmzQ\") = " + Arrays.toString(Utils.getContent("http://51.210.249.108:11043/api/geoip", "edGfJSQqavVTWmzQ")));
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
