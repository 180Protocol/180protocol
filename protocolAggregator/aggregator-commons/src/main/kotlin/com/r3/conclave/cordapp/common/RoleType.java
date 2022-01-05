package com.r3.conclave.cordapp.common;

public enum RoleType {
    TYPE_HOST("HOST"),
    TYPE_PROVIDER("PROVIDER"),
    TYPE_CONSUMER("CONSUMER");

    public final String type;

    private RoleType(String type) {
        this.type = type;
    }
}
