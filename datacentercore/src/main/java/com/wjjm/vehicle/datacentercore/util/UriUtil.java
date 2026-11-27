package com.wjjm.vehicle.datacentercore.util;

import android.net.Uri;

import static com.wjjm.vehicle.datacentercore.Constant.AUTHORITY;


public class UriUtil {

    public static Uri parseUri(String suffix) {
        return Uri.parse("content://" + AUTHORITY + "/datacenter/" + suffix);
    }
}
