package com.ilesson.ppim.utils;

/**
 * Created by potato on 2020/3/10.
 */

public interface Constants {

    String BASE_URL="https://pp.fangnaokeji.com:9443/pp";
//    String BASE_URL="http://192.168.2.192:8080/pp";
//    String AI_URL="https://aiibt.net/pigai.php";
    String AI_URL="http://www.xn--gmq6f129l.net/api20.php";
    String FAV_URL="http://192.168.2.192:8080/favourite/";
    String OCR_BASE="http://bang.aiibt.net";
    String USER_URL="/user";
    String UPDATE_URL="/update";
    String OCR_URL = "/location/ocr";
    String RONG_URL="/rong";
    String ASSET_URL="/asset";
    String APP_URL="/app";
    String BUSER_URL="/buser";
    String SIGNIN="/signin_v3";
    String BUSER_ASSET="/basset";
    String CODE_URL="/code";
    String GROUP_URL="/group";
    String GROUP_TAG_URL="/tag";
    String MONEY_URL="/money";
    String FAV="/fav";
    String ORDER="/order";
    String TALK="/talk";
    String PRODUCE="/produce";
    String SCORE="/score";
    String EXPRESS="/express";
    String SHOPKEEPER="/shopkeeper";
    String ADDRESS="/address";
    String FAV_LIST="/fav_list";
    String SHOP="/shop";
    String SERVER="/server";
    String COMPOSITION_URL="/composition";
    String BITCAPS="/bitcaps";
    String EXTRA="/extra";
    String REGISTE="/mobile-signup";
    String SHARE_BASE="http://pp.aiibt.net/pp";
    String SHARE_LINK=SHARE_BASE+COMPOSITION_URL+".jsp?uuid=";
    int appCode = 103;
    String opCode = "fc14061dee275c12fc7b331f3b888347";
    public static final int REQ_QR_CODE = 11002; // // 打开扫描界面请求码
    public static final int REQ_PERM_CAMERA = 11003; // 打开摄像头
    public static final int REQ_PERM_EXTERNAL_STORAGE = 11004; // 读写文件
    public static final String INTENT_EXTRA_KEY_QR_SCAN = "qr_scan_result";
    int VISIBLE_THRESHOLD = 3;
}
