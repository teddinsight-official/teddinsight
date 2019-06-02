package ng.com.teddinsight.teddinsight_app.models;

import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class TicketMessages {

    public String message;
    public String messageId;
    public long time;
    public String from;
    public boolean isText;
    public String senderName;

    public TicketMessages() {
    }


    public TicketMessages(String message, String from, boolean isText, String senderName, String messageId) {
        this.message = message;
        this.from = from;
        this.isText = isText;
        this.senderName = senderName;
        this.messageId = messageId;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> res = new HashMap<>();
        res.put("message", message);
        res.put("from", from);
        res.put("time", ServerValue.TIMESTAMP);
        res.put("isText", isText);
        res.put("senderName", senderName);
        res.put("messageId", messageId);
        return res;
    }
}
