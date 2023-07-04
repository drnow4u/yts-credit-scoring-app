package com.yolt.creditscoring.controller.admin.users;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.Base64;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Based64 {

    public static final Base64.Encoder ENCODER = Base64.getEncoder();
    public static final Base64.Decoder DECODER = Base64.getDecoder();

    private final byte[] data;

    @Override
    public String toString() {
        return toEncoded();
    }

    public static Based64 of(byte[] data) {
        return new Based64(data);
    }

    public static Based64 fromEncoded(String based64) {
        return new Based64(DECODER.decode(based64));
    }

    public String toEncoded() {
        return ENCODER.encodeToString(data);
    }

    public byte[] toBytes() {
        return data;
    }
}
