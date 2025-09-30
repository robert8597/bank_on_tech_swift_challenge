package com.db.fms_sds.botchallenge.constants;

public abstract class BotAppConstants {
    public static final String JWT_AUDIENCE = "sandbox.swift.com/oauth2/v1";
    public static final String JWT_SUBJECT = "CN=demo-swift-sandbox-consumer, O=Demo, L=London, S=London, C=GB";

    // TODO Task 0: Set up the host constant
    public static final String HOST = "?";


    public static final String SENDER_REFERENCE = "BankOnTech";
    public static final String SERVICE_CODE = "swift.finplus!pc";
    public static final String MESSAGE_TYPE = "pacs.008.001.13";
    public static final String REQUESTOR = "ou=xxx,o=deutdeff,o=swift";
    public static final String RESPONDER = "ou=xxx,o=bktrus33,o=swift";

}

