package com.bhm.sdk.rxlibrary.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;

import com.bhm.sdk.rxlibrary.rxjava.RxBuilder;

/**
 * Created by bhm on 2018/5/11.
 */

public class RxLoadingDialog {

    private RxLoadingFragment rxLoadingFragment;
    private static long onBackPressed = 0L;
    private static RxLoadingDialog RxDialog;

    public static RxLoadingDialog getDefaultDialog(){
        if(null == RxDialog){
            RxDialog = new RxLoadingDialog();
        }
        return RxDialog;
    }

    /**
     * rxManager 用户按返回关闭，请求取消
     * isCancelable true,单击返回键，dialog关闭；false,1s内双击返回键，dialog关闭，否则dialog不关闭
     */
    public void showLoading(final RxBuilder builder){
        if (rxLoadingFragment == null) {
            if (builder.getActivity() != null && !builder.getActivity().isFinishing()) {
                rxLoadingFragment = initDialog(builder);
                if(rxLoadingFragment.getDialog() != null) {
                    rxLoadingFragment.getDialog().setOwnerActivity(builder.getActivity());
                    rxLoadingFragment.getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                            if (i == KeyEvent.KEYCODE_BACK && rxLoadingFragment.getDialog().isShowing()
                                    && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                                if(builder.isCancelable()){
                                    if(null != builder.getRxManager()) {
                                        builder.getRxManager().removeObserver();
                                    }
                                    dismissLoading(builder.getActivity());
                                    return false;
                                }
                                if ((System.currentTimeMillis() - onBackPressed) > 1000) {
                                    onBackPressed = System.currentTimeMillis();
                                }else{
                                    if(null != builder.getRxManager()) {
                                        builder.getRxManager().removeObserver();
                                    }
                                    dismissLoading(builder.getActivity());
                                }
                            }
                            return false;
                        }
                    });
                }
            }
        }
        rxLoadingFragment.show(builder.getActivity().getSupportFragmentManager(), "default");
    }

    public RxLoadingFragment initDialog(RxBuilder builder){
        RxLoadingFragment fragment = new RxLoadingFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", builder.getLoadingTitle());
        bundle.putBoolean("isCancelable", builder.isCancelable());
        bundle.putBoolean("isCanceledOnTouchOutside", builder.isCanceledOnTouchOutside());
        fragment.setArguments(bundle);
        return fragment;
    }

    public void dismissLoading(Activity activity){
        if(null != activity && !activity.isFinishing()
                && null != rxLoadingFragment) {
            rxLoadingFragment.dismiss();
        }
    }

    public void cancelLoading(Activity activity){
        if(null != activity && null != rxLoadingFragment && null != rxLoadingFragment.getDialog()
                && activity.equals(rxLoadingFragment.getDialog().getOwnerActivity())) {
            rxLoadingFragment.dismiss();
        }
    }
}
