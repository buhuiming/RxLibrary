package com.bhm.sdk.demo.activity;

import android.os.Bundle;
import android.view.View;

import com.bhm.sdk.bhmlibrary.views.TitleBar;
import com.bhm.sdk.demo.entity.DoGetEntity;
import com.bhm.sdk.demo.http.HttpApi;
import com.bhm.sdk.demo.tools.Entity;
import com.bhm.sdk.rxlibrary.demo.R;
import com.bhm.sdk.rxlibrary.rxjava.RxBaseActivity;
import com.bhm.sdk.rxlibrary.rxjava.RxBuilder;
import com.bhm.sdk.rxlibrary.rxjava.callback.CallBack;
import com.bhm.sdk.rxlibrary.utils.RxLoadingDialog;

import androidx.annotation.Nullable;
import io.reactivex.Observable;

/**
 * Created by bhm on 2018/5/15.
 */

public class RxBusActivity extends RxBaseActivity {

    private TitleBar titleBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xml_title_bar);
        titleBar = (TitleBar) findViewById(R.id.titleBar);
        titleBar.setLeftOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        titleBar.setRightOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RxBuilder builder1 = RxBuilder.newBuilder(RxBusActivity.this)
                        .setLoadingDialog(RxLoadingDialog.getDefaultDialog())
                        .setLoadingTitle("dsadasd")
                        .setRxManager(rxManager)
                        .bindRx();

                Observable<DoGetEntity> observable1 = builder1
                        .createApi(HttpApi.class, "http://news-at.zhihu.com")
                        .getData("Bearer aedfc1246d0b4c3f046be2d50b34d6ff", "1");
                builder1.setCallBack(observable1, new CallBack<DoGetEntity>() {
                    @Override
                    public void onFail(Throwable e) {
                        super.onFail(e);
                        Entity entity = new Entity();
                        entity.setMsg("测试RxBus");
//                        RxBus.get().send(1111, entity);
                        finish();
                    }
                });
            }
        });
    }
}
