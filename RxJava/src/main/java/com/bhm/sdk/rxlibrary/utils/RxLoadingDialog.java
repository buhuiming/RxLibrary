package com.bhm.sdk.rxlibrary.utils;

import android.app.Activity;

import com.bhm.sdk.rxlibrary.rxjava.RxBuilder;

/**
 * Created by bhm on 2018/5/11.
 */

public class RxLoadingDialog {

    private RxLoadingFragment rxLoadingFragment;
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
        if (builder.getActivity() != null && !builder.getActivity().isFinishing() && builder.isShowDialog()) {
            if (rxLoadingFragment == null) {
                rxLoadingFragment = initDialog(builder);
            }
            rxLoadingFragment.show(builder.getActivity().getSupportFragmentManager(), "default");
        }
    }

    public RxLoadingFragment initDialog(RxBuilder builder){
        return new RxLoadingFragment(builder);
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
