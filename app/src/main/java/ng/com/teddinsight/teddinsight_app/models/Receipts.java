package ng.com.teddinsight.teddinsight_app.models;

import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class Receipts {

    public String trxRef;
    public long dateIssued;
    public double amount;
    public String name;
    public String service;
    public long due;
    public String customService;
    public int progress;
    private String serviceId;
    private int status;

    public Receipts() {

    }

    public static String getTableName() {
        return "receipts";
    }

    public Receipts(String trxRef, String name, String service, double amount) {
        this.progress = 0;
        this.amount = amount;
        this.trxRef = trxRef;
        this.name = name;
        this.service = service;
    }

    public Receipts(long dateIssued, long due, int progress, String serviceId, String name, int status) {
        this.dateIssued = dateIssued;
        this.status = status;
        this.due = due;
        this.name = name;
        this.progress = progress;
        this.serviceId = serviceId;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("dateIssued", ServerValue.TIMESTAMP);
        data.put("amount", amount);
        data.put("service", service);
        data.put("trxRef", trxRef);
        data.put("customService", customService);
        data.put("progress", progress);
        return data;
    }

    public String getCustomService() {
        return customService;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
