package com.bhm.sdk.rxlibrary.rxjava;

import android.content.Context;

import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;
import com.trello.rxlifecycle3.components.support.RxFragment;

import androidx.annotation.NonNull;

/**
 * Created by bhm on 2018/5/11.
 */

public class RxBaseFragment extends RxFragment{

    protected RxAppCompatActivity activity;
    protected RxManager rxManager = new RxManager();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (RxAppCompatActivity) context;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        rxManager.unSubscribe();
    }
}
