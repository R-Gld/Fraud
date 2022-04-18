import com.google.gson.GsonBuilder;
import fr.Rgld_.Fraud.Spigot.Helpers.ExtAPI;
import fr.Rgld_.Fraud.Spigot.Helpers.Stats;

import java.util.ArrayList;
import java.util.List;

public class StatsTest {

    public static void main(String[] args) {
        ExtAPI extAPI = new ExtAPI(null);
        System.out.println(extAPI.sendFraudStats(new Stats.Data("1.8.5", 1, 8954621, "1.8", false, 25565)));
        System.out.println("extAPI.getOwnIP() = " + extAPI.getOwnIP());
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
