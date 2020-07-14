package com.bhm.sdk.demo.tools;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bhm.sdk.rxlibrary.demo.R;
import com.bhm.sdk.rxlibrary.rxjava.RxBuilder;
import com.bhm.sdk.rxlibrary.utils.RxLoadingFragment;

public class MyRxLoadingFragment extends RxLoadingFragment {

    MyRxLoadingFragment(RxBuilder builder){
        super(builder);
    }

    @Override
    public Dialog initDialog() {
        String title = null;
        boolean isCancelable = true;
        boolean isCanceledOnTouchOutside = true;
        if(getArguments() != null){
            title = getArguments().getString("title");
            isCancelable = getArguments().getBoolean("isCancelable");
            isCanceledOnTouchOutside = getArguments().getBoolean("isCanceledOnTouchOutside");
        }
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        @SuppressLint("InflateParams")
        View v = inflater.inflate(R.layout.layout_my_loading, null);// 得到加载view
        @SuppressWarnings("ConstantConditions")
        Dialog dialog = new Dialog(getActivity(), R.style.loading_dialog);// 创建自定义样式dialog
        dialog.setCancelable(isCancelable);// false不可以用“返回键”取消
        dialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside);
        dialog.setContentView(v, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));// 设置布局
        if(!TextUtils.isEmpty(title)){
            TextView textView = v.findViewById(R.id.dialog_text_loading);
            textView.setText(title);
        }
        return dialog;
    }
}
