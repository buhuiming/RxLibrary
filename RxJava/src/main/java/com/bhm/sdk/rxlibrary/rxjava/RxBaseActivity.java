package com.bhm.sdk.rxlibrary.rxjava;

import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;

/**
 * Created by bhm on 2018/5/11.
 */

public class RxBaseActivity extends RxAppCompatActivity {

    protected RxManager rxManager = new RxManager();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rxManager.unSubscribe();
    }
}
