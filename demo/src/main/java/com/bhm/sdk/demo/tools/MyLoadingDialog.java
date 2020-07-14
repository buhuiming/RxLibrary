package com.bhm.sdk.demo.tools;

import com.bhm.sdk.rxlibrary.rxjava.RxBuilder;
import com.bhm.sdk.rxlibrary.utils.RxLoadingDialog;
import com.bhm.sdk.rxlibrary.utils.RxLoadingFragment;

/**
 * Created by bhm on 2018/5/14.
 */

public class MyLoadingDialog extends RxLoadingDialog{

    @Override
    public RxLoadingFragment initDialog(RxBuilder builder) {

        return new MyRxLoadingFragment();
    }
}
