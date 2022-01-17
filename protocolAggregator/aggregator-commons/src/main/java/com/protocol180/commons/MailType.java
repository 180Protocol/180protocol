package com.protocol180.commons;

public enum MailType {

    TYPE_SCHEMA("schema"),
    TYPE_IDENTITIES("identities"),
    TYPE_CONSUMER("consumer"),
    TYPE_PROVIDER("provider"),
    TYPE_REWARDS("rewards");

    public final String type;

    private MailType(String type) {
        this.type = type;
    }


}
