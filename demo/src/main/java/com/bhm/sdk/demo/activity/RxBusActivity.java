package com.bhm.sdk.demo.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.bhm.sdk.bhmlibrary.views.TitleBar;
import com.bhm.sdk.demo.tools.Entity;
import com.bhm.sdk.rxlibrary.demo.R;
import com.bhm.sdk.rxlibrary.rxbus.RxBus;

/**
 * Created by bhm on 2018/5/15.
 */

public class RxBusActivity extends AppCompatActivity{

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
                Entity entity = new Entity();
                entity.setMsg("测试RxBus");
                RxBus.get().send(1111, entity);
            }
        });
    }
}
