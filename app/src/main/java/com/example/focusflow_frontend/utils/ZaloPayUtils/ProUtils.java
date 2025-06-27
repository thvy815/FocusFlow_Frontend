package com.example.focusflow_frontend.utils.ZaloPayUtils;

import android.content.Context;
import android.content.SharedPreferences;

public class ProUtils {
    private static final String PREF_NAME = "user";
    private static final String KEY_IS_PRO = "isPro";
    private static final String KEY_PLAN_NAME = "planName";
    private static final String KEY_EXPIRE_TIME = "expireTime";

    // Trả về thời gian hết hạn tương ứng theo gói
    public static long getDurationInMillis(String plan) {
        switch (plan) {
            case "1 tháng": return 30L * 24 * 60 * 60 * 1000;
            case "3 tháng": return 90L * 24 * 60 * 60 * 1000;
            case "6 tháng": return 180L * 24 * 60 * 60 * 1000;
            case "12 tháng": return 365L * 24 * 60 * 60 * 1000;
            default: return 0;
        }
    }

    // Lưu trạng thái Pro vào SharedPreferences
    public static void saveProStatus(Context context, String plan, long expireTime) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(KEY_IS_PRO, true)
                .putString(KEY_PLAN_NAME, plan)
                .putLong(KEY_EXPIRE_TIME, expireTime)
                .apply();
    }

    // Kiểm tra người dùng còn hạn Pro hay không
    public static boolean isProValid(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean isPro = prefs.getBoolean(KEY_IS_PRO, false);
        long expireTime = prefs.getLong(KEY_EXPIRE_TIME, 0);
        return isPro && System.currentTimeMillis() < expireTime;
    }

    // Trả về tên gói hiện tại
    public static String getPlanName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_PLAN_NAME, "Chưa nâng cấp");
    }

    // Trả về thời gian hết hạn (để hiển thị hoặc kiểm tra)
    public static long getExpireTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_EXPIRE_TIME, 0);
    }

    // Dùng khi logout / hủy gói Pro
    public static void clearProStatus(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(KEY_IS_PRO)
                .remove(KEY_PLAN_NAME)
                .remove(KEY_EXPIRE_TIME)
                .apply();
    }
}
