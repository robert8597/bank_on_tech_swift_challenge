package com.db.fms_sds.botchallenge.utils;

import org.apache.commons.codec.binary.Base64;

public abstract class Utils {

    public static String base64Encode(String value) {
        return Base64.encodeBase64String(value.getBytes());
    }

    public static String base64Decode(String value) {
        return new String(Base64.decodeBase64(value));
    }


}
