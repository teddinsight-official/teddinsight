package ng.com.teddinsight.teddinsight_app.models;

public class Notifications {

    public int count;
    public String message;
    public String type;
    public boolean newTaskReceived;

    public static String getTableName() {
        return "notifications";
    }

    public Notifications() {
    }

}
