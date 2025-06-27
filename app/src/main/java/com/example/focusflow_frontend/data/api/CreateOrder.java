package com.example.focusflow_frontend.data.api;

import com.example.focusflow_frontend.utils.ZaloPayUtils.Helper.Helpers;

import org.json.JSONObject;

import java.util.Date;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class CreateOrder {

    private class CreateOrderData {
        String AppId;
        String AppUser;
        String AppTime;
        String Amount;
        String AppTransId;
        String EmbedData;
        String Items;
        String BankCode;
        String Description;
        String Mac;

        private CreateOrderData(String amount) throws Exception {
            long appTime = new Date().getTime();

            AppId = String.valueOf(AppInfo.APP_ID);
            AppUser = "Android_Demo";
            AppTime = String.valueOf(appTime);
            Amount = amount;
            AppTransId = Helpers.getAppTransId();
            EmbedData = "{}";
            Items = "[]";
            BankCode = "zalopayapp";
            Description = "Thanh toán nâng cấp gói tại Android app #" + AppTransId;

            String inputHMac = String.format("%s|%s|%s|%s|%s|%s|%s",
                    AppId, AppTransId, AppUser, Amount, AppTime, EmbedData, Items);

            // ✅ Log để kiểm tra nếu cần
            android.util.Log.d("ZALO_INPUT_MAC", inputHMac);

            Mac = Helpers.getMac(AppInfo.MAC_KEY, inputHMac);

            // ✅ Log MAC
            android.util.Log.d("ZALO_MAC", Mac);
        }
    }

    public JSONObject createOrder(String amount) throws Exception {
        CreateOrderData input = new CreateOrderData(amount);

        RequestBody formBody = new FormBody.Builder()
                .add("appid", input.AppId)
                .add("appuser", input.AppUser)
                .add("apptime", input.AppTime)
                .add("amount", input.Amount)
                .add("apptransid", input.AppTransId)
                .add("embeddata", input.EmbedData)
                .add("item", input.Items)
                .add("bankcode", input.BankCode)
                .add("description", input.Description)
                .add("mac", input.Mac)
                .build();

        return HttpProvider.sendPost(AppInfo.URL_CREATE_ORDER, formBody);
    }
}
