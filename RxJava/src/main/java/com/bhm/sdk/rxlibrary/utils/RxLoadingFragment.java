package com.bhm.sdk.rxlibrary.utils;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bhm.sdk.rxlibrary.R;
import com.bhm.sdk.rxlibrary.rxjava.RxBuilder;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class RxLoadingFragment extends DialogFragment {

    private RxBuilder builder;
    private static long onBackPressed = 0L;
    private TextView textView;

    public RxLoadingFragment(RxBuilder builder){
        this.builder = builder;
    }

    @Override
    public void show(@NotNull FragmentManager manager, String tag) {
        try {
            //在每个add事务前增加一个remove事务，防止连续的add
            manager.beginTransaction().remove(this).commitAllowingStateLoss();
            showAllowingLoss(manager, tag);
        } catch (Exception e) {
            //同一实例使用不同的tag会异常,这里捕获一下
            e.printStackTrace();
        }
    }

    /**
     * 解决 Can not perform this action after onSaveInstanceState问题
     *
     * @param manager FragmentManager
     * @param tag     tag
     */
    public void showAllowingLoss(FragmentManager manager, String tag) {
        try {
            Class cls = DialogFragment.class;
            Field mDismissed = cls.getDeclaredField("mDismissed");
            mDismissed.setAccessible(true);
            mDismissed.set(this, false);
            Field mShownByMe = cls.getDeclaredField("mShownByMe");
            mShownByMe.setAccessible(true);
            mShownByMe.set(this, true);
        } catch (Exception e) {
            //调系统的show()方法
            show(manager, tag);
            return;
        }
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        ft.commitAllowingStateLoss();
    }

    @Override
    public void dismiss() {
        //防止横竖屏切换时 getFragmentManager置空引起的问题：
        //Attempt to invoke virtual method 'android.app.FragmentTransaction
        //android.app.FragmentManager.beginTransaction()' on a null object reference
        if (getFragmentManager() == null) return;
        super.dismissAllowingStateLoss();
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = initDialog();
        if(dialog != null && getActivity() != null) {
            dialog.setOwnerActivity(getActivity());
            dialog.setCanceledOnTouchOutside(false);//这个值最好设置成false，点击其他区域关闭loading，体验效果不佳
            dialog.setCancelable(builder.isCancelable());
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                    if (i == KeyEvent.KEYCODE_BACK && dialog.isShowing()
                            && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                        if(builder.isCancelable()){
                            if(builder.isDialogDismissInterruptRequest() && null != builder.getRxManager()){
                                builder.getRxManager().removeObserver();
                            }
                            dismiss();
                            return true;
                        }
                        if ((System.currentTimeMillis() - onBackPressed) > 1000) {
                            onBackPressed = System.currentTimeMillis();
                        }else{
                            if(null != builder.getRxManager()) {
                                builder.getRxManager().removeObserver();
                            }
                            dismiss();
                        }
                    }
                    return true;
                }
            });
        }
        return Objects.requireNonNull(dialog);
    }

    public Dialog initDialog(){
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        @SuppressLint("InflateParams")
        View v = inflater.inflate(R.layout.layout_dialog_app_loading, null);// 得到加载view
        @SuppressWarnings("ConstantConditions")
        Dialog dialog = new Dialog(getActivity(), R.style.loading_dialog);// 创建自定义样式dialog
        dialog.setContentView(v, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));// 设置布局
        textView = v.findViewById(R.id.dialog_text_loading);
        if(!TextUtils.isEmpty(builder.getLoadingTitle())){
            textView.setText(builder.getLoadingTitle());
        }
        return dialog;
    }

    /** 改变Dialog的显示内容
     * @param rxBuilder
     */
    public void changDialogContent(RxBuilder rxBuilder){
        if(textView != null && !TextUtils.isEmpty(rxBuilder.getLoadingTitle())){
            textView.setText(rxBuilder.getLoadingTitle());
        }
    }
}
