package fr.Rgld_.Fraud;

import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;

public class Console {

    public Console(){}

    public void sendMessages(String... messages){
        Bukkit.getConsoleSender().sendMessage(messages);
    }

    public void sm(String message){
        sm(message);
    }

    public void sendMessage(String message){
        sendMessages(message);
    }

    public void sendMessage(){
        sm("");
    }


    public void sendObject(Object obj){
        sm(new GsonBuilder().create().toJson(obj));
    }

}
