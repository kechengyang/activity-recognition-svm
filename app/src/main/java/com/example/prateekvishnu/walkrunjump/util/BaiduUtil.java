package com.example.prateekvishnu.walkrunjump.util;

/**
 * Created by css on 2018/7/26.
 */

public class BaiduUtil {
    static int[] zoomLevel = {20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3};
    static String[] zoomLevelStr = {"10", "20", "50", "100", "200", "500", "1000",
            "2000", "5000", "10000", "20000", "25000", "50000", "100000",
            "200000", "500000", "1000000", "2000000"
    };

    /**
     * 获取缩放级别
     *
     * @param distance 2点之间距离
     * @return int zoomLevel
     */
    public static int getZoomLevel(double distance) {
        int mid = (int) (distance * 100);
        for (int i = 0; i < zoomLevelStr.length; i++) {
            if (i < zoomLevelStr.length - 1) {
                int left = Integer.valueOf(zoomLevelStr[i]);
                int right = Integer.valueOf(zoomLevelStr[i + 1]);
                if (mid < left) {
                    return zoomLevel[i];
                } else if (mid > left && mid < right) {
                    return zoomLevel[i + 1];
                }
            } else {
                return 3;
            }
        }
        return 18;
    }
}
