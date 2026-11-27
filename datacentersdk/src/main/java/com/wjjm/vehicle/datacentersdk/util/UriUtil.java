package com.wjjm.vehicle.datacentersdk.util;

import android.net.Uri;

import static com.wjjm.vehicle.datacentersdk.Constant.AUTHORITY;

public class UriUtil {

    public static Uri parseUri(String suffix) {
        return Uri.parse("content://" + AUTHORITY + "/datacenter/" + suffix);
    }
}
