package fr.Rgld_.Fraud.Spigot.Storage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fr.Rgld_.Fraud.Spigot.Fraud;
import fr.Rgld_.Fraud.Spigot.Helpers.Messages;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;


public class MessageManager {

    private final Fraud fraud;
    private final File file;

    public MessageManager(Fraud fraud) throws IOException {
        this.fraud = fraud;
        this.file = new File(fraud.getDataFolder(), "messages.json");
        loadFile();
    }

    public void loadFile() throws IOException {
        fraud.saveResource("messages.json", false);
        // Store the content of the file messages.json in a String

        String content = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(file.getPath())));
        loadMessages(content);
    }

    public void loadMessages(String json) {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> jsonMap = gson.fromJson(json, type);

        processMap(jsonMap, "");
    }

    private void processMap(Map<String, Object> map, String prefix) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map<?, ?>) {
                // C'est un sous-objet, on traite récursivement
                processMap((Map<String, Object>) value, prefix + key + ".");
            } else if (value instanceof String) {
                // C'est une chaîne, on peut l'associer à une énumération
                setEnumMessage(prefix + key, (String) value);
            }
        }
    }

    private void setEnumMessage(String key, String value) {
        try {
            String enumKey = key.toUpperCase().replace(" ", "_").replace("-", "_").replace(".", "_");
            Messages messageEnum = Messages.valueOf(enumKey);
            messageEnum.setMessage(value);
        } catch (IllegalArgumentException e) {
            System.out.println("No corresponding enum found for key: " + key);
        }
    }

}
