package fr.Rgld_.Fraud.Storage;

import fr.Rgld_.Fraud.Fraud;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Read the file countries.json
 */
public class Countries {

    private JSONObject obj = null;
    private final Fraud fraud;

    public Countries(Fraud fraud) {
        this.fraud = fraud;
        loadFile();
    }

    public String getCountriesName(String code) {
        if(obj == null) return null;
        return (String) obj.get(code);
    }


    public void loadFile() {
        JSONParser jsonParser = new JSONParser();
        File countryFile = new File(fraud.getDataFolder(), "countries.json");
        if(!countryFile.exists()){
            try {
                countryFile.createNewFile();
                fraud.saveResource(countryFile.getName(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try (FileReader reader = new FileReader("config.yml")) {
                this.obj = (JSONObject) jsonParser.parse(reader);
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }
    }

}
