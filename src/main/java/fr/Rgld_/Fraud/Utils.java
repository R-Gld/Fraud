package fr.Rgld_.Fraud;

public class Utils {

    public static Thread scheduleAsyncRunnable(Runnable runnable){
        Thread thread = new Thread(runnable);
        thread.start();
        return thread;
    }

}
