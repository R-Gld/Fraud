package fr.Rgld_.Fraud.Spigot.Storage;

import fr.Rgld_.Fraud.Spigot.Fraud;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ErrorCatcher {

    private final File folder;

    public ErrorCatcher(Fraud fraud) {
        this.folder = new File(fraud.getDataFolder(), "errors");
        if(!folder.exists() || folder.isFile()) {
            folder.mkdirs();
        }
    }

    public boolean storeError(Throwable err) {
        File errorFile = generateErrorFile();
        try {
            FileWriter writer = new FileWriter(errorFile);
            writer.write(err.getMessage());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        System.out.println("Error write in file: " + errorFile.getPath());
        return true;
    }

    private File generateErrorFile() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH:mm");
        String date = dtf.format(LocalDateTime.now());
        int i = 0;
        for(File file : folder.listFiles()) {
            if(!file.getName().contains(date)) continue;
            i++;
        }

        String filename = MessageFormat.format("error-{0}-{1}.err", date, i);
        File errFile = new File(folder, filename);
        try {
            errFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return errFile;
    }


}
