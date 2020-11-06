package com.mei.guide;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.mei.guide.util.AppUtils;
import com.mei.guide.util.ParseJsonUtils;
import com.mei.guide.util.Result;
import com.zhouyou.http.EasyHttp;
import com.zhouyou.http.callback.SimpleCallBack;
import com.zhouyou.http.exception.ApiException;

public class ReserveDialog extends Dialog {

    private ImageView ivCommit;
    private EditText etCommit;
    private OnItemListener listener;

    interface OnItemListener {
        void onCommit(Dialog dialog);
    }

    protected ReserveDialog(Context context, final OnItemListener listener) {
        super(context);
        setContentView(R.layout.dialog_reserve);
        this.listener = listener;

        ivCommit = findViewById(R.id.iv_commit);
        etCommit = findViewById(R.id.et_phone);

        ivCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = etCommit.getText().toString().trim();
                if (TextUtils.isEmpty(phone)) {
                    Toast.makeText(getContext(), "请输入手机号", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!AppUtils.isMobile(phone)) {
                    Toast.makeText(getContext(), "请输入正确手机号", Toast.LENGTH_LONG).show();
                    return;
                }
                EasyHttp.post("gameBook/book")
                        .params("phone", phone)
                        .execute(new SimpleCallBack<String>() {
                            @Override
                            public void onError(ApiException e) {
                            }

                            @Override
                            public void onSuccess(String s) {
                                Result<String> result = ParseJsonUtils.parseDataToResult(s, String.class);
                                if (result.isOk()) {
                                    listener.onCommit(ReserveDialog.this);
                                } else {
                                    Toast.makeText(getContext(), result.msg, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

    }

    @Override
    public void show() {
        super.show();
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        window.setBackgroundDrawableResource(R.color.translation);
        params.width = WindowManager.LayoutParams.MATCH_PARENT;//如果不设置,可能部分机型出现左右有空隙,也就是产生margin的感觉
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        int screenWidth = ((Activity)getContext()).getWindowManager().getDefaultDisplay().getWidth(); // 屏幕宽
//        int screenHeight = getOwnerActivity().getWindowManager().getDefaultDisplay().getHeight(); // 屏幕高
//        params.width = (int)(screenWidth * 0.8); // 宽度
//        params.height = (int)(screenHeight * 0.8); // 高度

        params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;//显示dialog的时候,就显示软键盘
        params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;//就是这个属性导致不能获取焦点,默认的是FLAG_NOT_FOCUSABLE,故名思义不能获取输入焦点,
        params.dimAmount = 0.5f;//设置对话框的透明程度背景(非布局的透明度)
        window.setAttributes(params);
    }

}
