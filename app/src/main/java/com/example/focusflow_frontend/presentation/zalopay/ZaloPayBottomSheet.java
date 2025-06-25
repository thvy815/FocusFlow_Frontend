package com.example.focusflow_frontend.presentation.zalopay;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.api.AppInfo;
import com.example.focusflow_frontend.data.api.CreateOrder;
import com.example.focusflow_frontend.data.model.ProUpgradeRequest;
import com.example.focusflow_frontend.utils.ApiClient;
import com.example.focusflow_frontend.utils.TokenManager;
import com.example.focusflow_frontend.utils.ZaloPayUtils.ProUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.zalopay.sdk.*;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class ZaloPayBottomSheet extends BottomSheetDialogFragment {
    private RadioGroup planGroup;
    private Button btnPay;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = d.findViewById(R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                bottomSheet.setLayoutParams(layoutParams);
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        FrameLayout bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setDraggable(false);
            behavior.setHideable(false);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            bottomSheet.setOnTouchListener((v, event) -> true);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_zalopay, container, false);
        Log.d("ZaloPayDebug", "onCreateView STARTED");
        planGroup = view.findViewById(R.id.planGroup);
        btnPay = view.findViewById(R.id.btnPay);

        ZaloPaySDK.init(AppInfo.APP_ID, Environment.SANDBOX);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        btnPay.setOnClickListener(v -> {
            int selectedId = planGroup.getCheckedRadioButtonId();
            final String amount, plan;

            if (selectedId == R.id.plan1) {
                amount = "29000"; plan = "1 tháng";
            } else if (selectedId == R.id.plan3) {
                amount = "79000"; plan = "3 tháng";
            } else if (selectedId == R.id.plan6) {
                amount = "149000"; plan = "6 tháng";
            } else if (selectedId == R.id.plan12) {
                amount = "279000"; plan = "12 tháng";
            } else {
                Toast.makeText(getContext(), "Vui lòng chọn gói nâng cấp!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                CreateOrder orderApi = new CreateOrder();
                JSONObject data = orderApi.createOrder(amount);
                Log.d("ZaloPay Response", data.toString());

                String code = data.getString("returncode");
                if (code.equals("1")) {
                    String zpToken = data.getString("zptranstoken");

                    ZaloPaySDK.getInstance().payOrder(getActivity(), zpToken, "demozpdk://app", new PayOrderListener() {
                        @Override
                        public void onPaymentSucceeded(String transactionId, String transToken, String appTransID) {
                            if (!isAdded() || getContext() == null) return;

                            Context context = getContext(); // Không dùng requireContext()

                            new AlertDialog.Builder(context)
                                    .setTitle("Nâng cấp thành công")
                                    .setMessage("Bạn đã nâng cấp thành công gói " + plan)
                                    .setCancelable(false)
                                    .setPositiveButton("OK", (dialog, which) -> dismiss()) // 💡 dismiss sau khi OK
                                    .show();

                            long expireTime = System.currentTimeMillis() + ProUtils.getDurationInMillis(plan);
                            ProUtils.saveProStatus(context, plan, expireTime);

                            SharedPreferences prefs = context.getSharedPreferences("user", Context.MODE_PRIVATE);
                            String jwtToken = TokenManager.getToken(context);

                            if (jwtToken != null) {
                                TokenManager.saveToken(context, jwtToken);
                            }

                            ProUpgradeRequest request = new ProUpgradeRequest(plan, expireTime);
                            ApiClient.getProController(context).upgradePro(request).enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        Log.d("ProUpgrade", "Lưu Pro thành công");
                                    } else {
                                        Log.e("ProUpgrade", "Lỗi API: " + response.code());
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Log.e("ProUpgrade", "API lỗi", t);
                                }
                            });
                        }
                        @Override
                        public void onPaymentCanceled(String zpTransToken, String appTransID) {
                            Toast.makeText(getContext(), "Bạn đã hủy thanh toán", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPaymentError(ZaloPayError zaloPayError, String zpTransToken, String appTransID) {
                            Toast.makeText(getContext(), "Lỗi thanh toán: " + zaloPayError.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Tạo đơn hàng thất bại!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Lỗi tạo đơn hàng!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
