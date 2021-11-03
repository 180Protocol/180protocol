package com.protocol180.commons;

public enum SchemaType {

    TYPE_SCHEMA1("schema1"),
    TYPE_SCHEMA2("schema2"),
    TYPE_SCHEMA3("schema3");

    public final String type;

    private SchemaType(String type) {
        this.type = type;
    }

}