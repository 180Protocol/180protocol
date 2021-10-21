package com.protocol180.commons;

public enum ClientType {
    TYPE_CONSUMER("consumer"),
    TYPE_PROVIDER("provider"),
    TYPE_PROVENANCE("provenance");

    public final String type;

    private ClientType(String type) {
        this.type = type;
    }

}
