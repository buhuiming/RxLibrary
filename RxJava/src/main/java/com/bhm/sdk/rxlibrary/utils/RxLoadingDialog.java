package com.bhm.sdk.rxlibrary.utils;

import android.app.Activity;

import com.bhm.sdk.rxlibrary.rxjava.RxBuilder;

import androidx.fragment.app.FragmentManager;

/**
 * Created by bhm on 2018/5/11.
 */

public class RxLoadingDialog {

    private RxLoadingFragment rxLoadingFragment;
    private static RxLoadingDialog RxDialog;
    private boolean showAgain = false;

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
                showAgain = false;
            }else{
                rxLoadingFragment.changDialogContent(builder);
                showAgain = true;
            }
            FragmentManager fm = builder.getActivity().getSupportFragmentManager();
            if (!rxLoadingFragment.isAdded() && null == fm.findFragmentByTag("default")) {
                rxLoadingFragment.show(fm, "default");
            }
        }
    }

    public RxLoadingFragment initDialog(RxBuilder builder){
        return new RxLoadingFragment(builder);
    }

    public void dismissLoading(Activity activity){
        cancelLoading(activity);
    }

    public void cancelLoading(Activity activity){
        if(null != rxLoadingFragment && !showAgain){
            if(null != activity && !activity.isFinishing() && null != rxLoadingFragment.getDialog()
                    && activity.equals(rxLoadingFragment.getDialog().getOwnerActivity())){
                rxLoadingFragment.dismiss();
            }
            rxLoadingFragment = null;
        }
        showAgain = false;
    }
}
