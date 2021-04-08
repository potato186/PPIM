package com.ilesson.ppim.utils;

import org.xutils.common.util.IOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class MD5 {

    private MD5() {
    }

    private static final char hexDigits[] =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String toHexString(byte[] bytes) {
        if (bytes == null) return "";
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(hexDigits[(b >> 4) & 0x0F]);
            hex.append(hexDigits[b & 0x0F]);
        }
        return hex.toString();
    }

    public static String md5(File file) throws IOException {
        MessageDigest messagedigest = null;
        FileInputStream in = null;
        FileChannel ch = null;
        byte[] encodeBytes = null;
        try {
            messagedigest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            ch = in.getChannel();
            MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            messagedigest.update(byteBuffer);
            encodeBytes = messagedigest.digest();
        } catch (NoSuchAlgorithmException neverHappened) {
            throw new RuntimeException(neverHappened);
        } finally {
            IOUtil.closeQuietly(in);
            IOUtil.closeQuietly(ch);
        }

        return toHexString(encodeBytes);
    }

//    public static String md5(String string) {
//        byte[] encodeBytes = null;
//        try {
//            encodeBytes = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
//        } catch (NoSuchAlgorithmException neverHappened) {
//            throw new RuntimeException(neverHappened);
//        } catch (UnsupportedEncodingException neverHappened) {
//            throw new RuntimeException(neverHappened);
//        }
//
//        return toHexString(encodeBytes);
//    }
public static String md5(String str) {
    try {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(str.getBytes("utf-8"));
        byte b[] = md.digest();
        int i;
        StringBuffer buf = new StringBuffer("");
        for (int offset = 0; offset < b.length; offset++) {
            i = b[offset];
            if (i < 0)
                i += 256;
            if (i < 16)
                buf.append("0");
            buf.append(Integer.toHexString(i));
        }
        str = buf.toString();
    } catch (Exception e) {
        e.printStackTrace();

    }
    return str;
}
}
