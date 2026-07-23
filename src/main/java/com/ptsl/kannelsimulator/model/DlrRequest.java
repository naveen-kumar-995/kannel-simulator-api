package com.ptsl.kannelsimulator.model;

import java.io.Serializable;


public class DlrRequest implements Serializable {

    private static final long serialVersionUID = 1L;

        public DlrRequest(String user, String password, String smsc, String smscId, String cliId, String mobileNumber, String senderId, String message, String dlrUrl, String metaData, long receivedTime) {
            this.user = user;
            this.password = password;
            this.smsc = smsc;
            this.smscId = smscId;
            this.cliId = cliId;
            this.mobileNumber = mobileNumber;
            this.senderId = senderId;
            this.message = message;
            this.dlrUrl = dlrUrl;
            this.metaData = metaData;
            this.receivedTime = receivedTime;
        }

        private final String user;

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getSmsc() {
        return smsc;
    }

    public String getSmscId() {
        return smscId;
    }

    public String getCliId() {
        return cliId;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getMessage() {
        return message;
    }

    public String getDlrUrl() {
        return dlrUrl;
    }

    public String getMetaData() {
        return metaData;
    }

    public long getReceivedTime() {
        return receivedTime;
    }

    private final String password;
    private final String smsc;
    private final String smscId;
    private final String cliId;
    private final String mobileNumber;
    private final String senderId;
    private final String message;
    private final String dlrUrl;
    private final String metaData;
    private final long   receivedTime;




}