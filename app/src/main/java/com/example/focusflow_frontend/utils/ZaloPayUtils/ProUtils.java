package com.example.focusflow_frontend.utils.ZaloPayUtils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.focusflow_frontend.data.api.ProController;
import com.example.focusflow_frontend.data.model.ProStatusResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

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
    // Kiểm tra trạng thái Pro thông minh: ưu tiên local, fallback server nếu cần

    public static boolean isProValidLocal(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean isPro = prefs.getBoolean(KEY_IS_PRO, false);
        long expireTime = prefs.getLong(KEY_EXPIRE_TIME, 0);
        return isPro && System.currentTimeMillis() < expireTime;
    }
    public static void isProValid(Context context, Retrofit retrofit, ProStatusCallback callback) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean isPro = prefs.getBoolean(KEY_IS_PRO, false);
        long expireTime = prefs.getLong(KEY_EXPIRE_TIME, 0);
        long now = System.currentTimeMillis();

        if (isPro && now < expireTime) {
            // Local còn hiệu lực → dùng luôn
            callback.onResult(true);
        } else {
            // Local hết hạn hoặc chưa có → kiểm tra server
            checkProFromServer(context, retrofit, callback);
        }
    }
    public static void checkProFromServer(Context context, Retrofit retrofit, ProStatusCallback callback) {
        ProController service = retrofit.create(ProController.class);

        service.getProStatus().enqueue(new Callback<ProStatusResponse>() {
            @Override
            public void onResponse(Call<ProStatusResponse> call, Response<ProStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProStatusResponse data = response.body();

                    SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                    prefs.edit()
                            .putBoolean(KEY_IS_PRO, data.isPro())
                            .putString(KEY_PLAN_NAME, data.getPlanName())
                            .putLong(KEY_EXPIRE_TIME, data.getExpireTime())
                            .apply();

                    callback.onResult(data.isPro());
                } else {
                    callback.onError("Server trả về lỗi");
                }
            }

            @Override
            public void onFailure(Call<ProStatusResponse> call, Throwable t) {
                callback.onError("Lỗi mạng: " + t.getMessage());
            }
        });
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
