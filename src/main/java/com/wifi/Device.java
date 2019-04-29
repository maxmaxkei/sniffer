package com.wifi;

import org.bson.Document;

public class Device {

    private final String mac;
    private Long startSession;
    private Long endSession;

    public Device(String mac, Long ts) {
        this.mac = mac;
        this.startSession = ts;
        this.endSession = ts;
    }

    public Device(String mac, Long start, Long end) {
        this.mac = mac;
        this.startSession = start;
        this.endSession = end;
    }

    public int showDuration () {
        return (int) (endSession - startSession);
    }

    public Long getEndSession() {
        return endSession;
    }

    public void setEndSession(Long ts) {
        this.endSession = ts;
    }

    public Document createDocument () {
        return new Document("name", mac)
                .append("startSession", startSession)
                .append("endSession", endSession)
                .append("sessionDuration", (int)(endSession - startSession));
    }

}