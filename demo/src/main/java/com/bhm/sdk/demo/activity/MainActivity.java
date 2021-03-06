package com.bhm.sdk.demo.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bhm.sdk.bhmlibrary.views.TitleBar;
import com.bhm.sdk.demo.adapter.MainUIAdapter;
import com.bhm.sdk.demo.entity.DoGetEntity;
import com.bhm.sdk.demo.entity.UpLoadEntity;
import com.bhm.sdk.demo.http.HttpApi;
import com.bhm.sdk.demo.tools.Entity;
import com.bhm.sdk.demo.tools.Utils;
import com.bhm.sdk.onresult.ActivityResult;
import com.bhm.sdk.rxlibrary.demo.R;
import com.bhm.sdk.rxlibrary.rxbus.RxBus;
import com.bhm.sdk.rxlibrary.rxbus.Subscribe;
import com.bhm.sdk.rxlibrary.rxjava.RxBaseActivity;
import com.bhm.sdk.rxlibrary.rxjava.RxBuilder;
import com.bhm.sdk.rxlibrary.rxjava.callback.CallBack;
import com.bhm.sdk.rxlibrary.rxjava.callback.RxDownLoadCallBack;
import com.bhm.sdk.rxlibrary.rxjava.callback.RxUpLoadCallBack;
import com.bhm.sdk.rxlibrary.utils.RxLoadingDialog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

@SuppressLint("CheckResult")
public class MainActivity extends RxBaseActivity {

    protected RecyclerView main_recycle_view;
    private MainUIAdapter adapter;
    private TitleBar titleBar;
    private ProgressBar progressBarHorizontal;
    private RxPermissions rxPermissions;
    private Disposable down_Disposable;
    private Disposable up_Disposable;
    private long downLoadLength;//已下载的长度

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RxBus.get().register(this);
        rxPermissions = new RxPermissions(this);//权限申请
        initView();
        initListener();
    }

    private void initView() {
        main_recycle_view = (RecyclerView) findViewById(R.id.main_recycle_view);
        progressBarHorizontal = (ProgressBar) findViewById(R.id.progressBarHorizontal);
        titleBar = (TitleBar) findViewById(R.id.titleBar);
        LinearLayoutManager ms = new LinearLayoutManager(this);
        ms.setOrientation(LinearLayoutManager.VERTICAL);
        main_recycle_view.setLayoutManager(ms);
        main_recycle_view.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        main_recycle_view.setHasFixedSize(false);
        adapter = new MainUIAdapter(getItems());
        main_recycle_view.setAdapter(adapter);
    }

    private List<String> getItems() {
        List<String> list = new ArrayList<>();
        list.add("RxJava2+Retrofit2,Get请求");
        list.add("RxJava2+Retrofit2,post请求");
        list.add("RxJava2+Retrofit2,文件上传（带进度）");
        list.add("取消上传");
        list.add("RxJava2+Retrofit2,文件下载（带进度）");
        list.add("暂停/取消下载");
        list.add("继续下载");
        list.add("");
        list.add("RxBus");
        return list;
    }

    private void initListener() {
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                openUI(position);
            }
        });
    }

    private void openUI(int position) {
        switch (position) {
            case 0:
                doGet();
                break;
            case 1:
                doPost();
                break;
            case 2:
                upLoad();
                break;
            case 3:
                rxManager.removeObserver(up_Disposable);
                break;
            case 4:
                downLoadLength = 0;
                downLoad();
                break;
            case 5:
                if(!rxManager.isExitObserver(down_Disposable)){
                    return;
                }
                rxManager.removeObserver(down_Disposable);
                break;
            case 6:
                downLoad();
                break;
            case 8:
//                startActivity(new Intent(this, RxBusActivity.class));
                new ActivityResult(this).startForResult(new Intent(this, RxBusActivity.class)
                        , new ActivityResult.Callback() {
                            @Override
                            public void onActivityResult(int i, Intent intent) {
//                                RxLoadingDialog.getDefaultDialog().showLoading(RxBuilder.
//                                        newBuilder(MainActivity.this).bindRx());
                                doGet();
                            }
                        });
                break;
            default:
                return;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.get().unRegister(this);
    }

    @Subscribe(code = 1111)
    public void rxBusEvent(Entity entity){
        if(null != entity){
            Toast.makeText(this, "RxBus改变了MainActivity的标题", Toast.LENGTH_SHORT).show();
            titleBar.setTitleText(entity.getMsg());
            doGet();
        }
    }

    private void upLoad(){
        rxPermissions
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if(!aBoolean){
                            Toast.makeText(MainActivity.this, "无法获取权限，请在设置中授权",
                                    Toast.LENGTH_SHORT).show();
                        }else{
                            upLoadFile();//上传文件
                        }
                    }
                });
    }

    private void downLoad(){
        rxPermissions
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if(!aBoolean){
                            Toast.makeText(MainActivity.this, "无法获取权限，请在设置中授权",
                                    Toast.LENGTH_SHORT).show();
                        }else{
                            downLoadFile();//下载文件
                        }
                    }
                });
    }

    private void doGet() {
        /*单独使用配置*/
        /*RxBuilder builder = RxBuilder.newBuilder(this)
                .setLoadingDialog(RxLoadingDialog.getDefaultDialog())
//                .setLoadingDialog(new MyLoadingDialog())
                .setDialogAttribute(true, false, false)
                .setHttpTimeOut(20000, 20000)
                .setIsLogOutPut(true)//默认是false
                .setIsDefaultToast(true, rxManager)
                .bindRx();*/

        /*默认使用Application的配置*/
        RxBuilder builder = RxBuilder.newBuilder(this)
                .setLoadingDialog(RxLoadingDialog.getDefaultDialog())
                .setRxManager(rxManager)
                .bindRx();

        Observable<DoGetEntity> observable = builder
                .createApi(HttpApi.class, "http://news-at.zhihu.com")
                .getData("Bearer aedfc1246d0b4c3f046be2d50b34d6ff", "1");
        builder.setCallBack(observable, new CallBack<DoGetEntity>() {
            @Override
            public void onSuccess(DoGetEntity response) {
                Log.i("MainActivity--> ", response.getDate());
                Toast.makeText(MainActivity.this, response.getDate(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFail(Throwable e) {
                super.onFail(e);
                RxBuilder builder1 = RxBuilder.newBuilder(MainActivity.this)
                        .setLoadingDialog(RxLoadingDialog.getDefaultDialog())
                        .setLoadingTitle("dsadasd")
                        .setRxManager(rxManager)
                        .bindRx();

                Observable<DoGetEntity> observable1 = builder1
                        .createApi(HttpApi.class, "http://news-at.zhihu.com")
                        .getData("Bearer aedfc1246d0b4c3f046be2d50b34d6ff", "1");
                builder1.setCallBack(observable1, new CallBack<DoGetEntity>() {
                    @Override
                    public void onSuccess(DoGetEntity response) {
                        Log.i("MainActivity--> ", response.getDate());
                        Toast.makeText(MainActivity.this, response.getDate(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void doPost() {
        RxBuilder builder = RxBuilder.newBuilder(this)
                .setLoadingDialog(RxLoadingDialog.getDefaultDialog())
//                .setLoadingDialog(new MyLoadingDialog())
                .setDialogAttribute(true, false, false)
                //.setHttpTimeOut()
                .setIsLogOutPut(false)
                .setIsDefaultToast(false, rxManager)
                .bindRx();
        Observable<DoGetEntity> observable = builder
                .createApi(HttpApi.class, "Https://api.douban.com/")
                .getDataPost(true);
        builder.setCallBack(observable, new CallBack<DoGetEntity>() {
            @Override
            public void onSuccess(DoGetEntity response) {
                Log.i("MainActivity--> ", response.toString());
                Toast.makeText(MainActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFail(Throwable e) {
                super.onFail(e);
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(e.getMessage())
                        .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });
    }

    private void upLoadFile() {
        File file = Utils.getFile();
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg; charset=UTF-8"), file);
        MultipartBody.Part part= MultipartBody.Part.createFormData("file", file.getName(), requestBody);//key(file)与服务器一致

        RxBuilder builder = RxBuilder.newBuilder(this)
                .setLoadingDialog(RxLoadingDialog.getDefaultDialog())
                .setDialogAttribute(false, false, false)
                .setIsLogOutPut(true)//默认是false
                .setIsDefaultToast(true, rxManager)
                .bindRx();
        Observable<UpLoadEntity> observable = builder
                .createApi(HttpApi.class, "http://cloudapi.dev-chexiu.cn/", rxUpLoadListener)//rxUpLoadListener不能为空
                .upload("Bearer 56b3fe3eb59fc880ab1aa690dd822b47",
                        RequestBody.create(MediaType.parse("text/plain"), "9"),
                        part);
        up_Disposable = builder.setCallBack(observable, new CallBack<UpLoadEntity>() {
            @Override
            public void onStart(Disposable disposable) {
                rxUpLoadListener.onStart();
            }

            @Override
            public void onSuccess(UpLoadEntity response) {
                Log.i("MainActivity--> ", response.getMsg());
                Toast.makeText(MainActivity.this, response.getMsg(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFail(Throwable e) {
                rxUpLoadListener.onFail(e.getMessage());
            }

            @Override
            public void onComplete() {
                rxUpLoadListener.onFinish();
            }
        });
//        rxManager.subscribe(up_Disposable);setCallBack方法有rxManager.subscribe(disposable),无需重复，重复也没关系。
    }

    /**
     * 多文件一起上传
     */
    private void upLoadFiles() {
        List<File> files = Utils.getFiles();
        final MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();
        bodyBuilder.setType(MultipartBody.FORM);
        bodyBuilder.addFormDataPart("id", String.valueOf(100));
        bodyBuilder.addFormDataPart("type", String.valueOf(1));

        //"file"为服务器定义的key
        for(File file : files) {
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg; charset=UTF-8"), file);
            bodyBuilder.addFormDataPart("file", file.getName(), requestBody);
        }
        RxBuilder builder = RxBuilder.newBuilder(this)
                .setLoadingDialog(RxLoadingDialog.getDefaultDialog())
                .setDialogAttribute(false, false, false)
                .setIsLogOutPut(true)//默认是false
                .setIsDefaultToast(true, rxManager)
                .bindRx();
        Observable<UpLoadEntity> observable = builder
                .createApi(HttpApi.class, "http://cloudapi.dev-chexiu.cn/", rxUpLoadListener)//rxUpLoadListener不能为空
                .upload("Bearer 56b3fe3eb59fc880ab1aa690dd822b47",
                        bodyBuilder.build());
        up_Disposable = builder.setCallBack(observable, new CallBack<UpLoadEntity>() {
            @Override
            public void onStart(Disposable disposable) {
                rxUpLoadListener.onStart();
            }

            @Override
            public void onSuccess(UpLoadEntity response) {
                Log.i("MainActivity--> ", response.getMsg());
                Toast.makeText(MainActivity.this, response.getMsg(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFail(Throwable e) {
                rxUpLoadListener.onFail(e.getMessage());
            }

            @Override
            public void onComplete() {
                rxUpLoadListener.onFinish();
            }
        });
//        rxManager.subscribe(up_Disposable);setCallBack方法有rxManager.subscribe(disposable),无需重复，重复也没关系。
    }

    /**
     * setDialogAttribute参数：1.filePath：文件下载路径， 2.fileName：文件名
     *              3.mAppendWrite：是否支持暂停下载。true,支持，同时需要记录writtenLength
     *              false，每次都重新开始下载，并且会删除原文件。（注：文件下载完后，再下载都会删除原文件重新下载，与此参数无关）
     *              4.writtenLength：当mAppendWrite=true,需要记录已下载的部分，当mAppendWrite=false,writtenLength需
     *              赋值0，否则，新文件会从writtenLength开始下载导致文件不完整。
     *
     * 注：调用的函数downLoad,第一个参数为@Header("RANGE") String range，传递参数格式为："bytes=" + writtenLength + "-"
     *     rxDownLoadListener不能为空
     */
    private void downLoadFile(){
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "RxLibrary/";
        String fileName = "demo.apk";
        RxBuilder builder = RxBuilder.newBuilder(this)
                .setLoadingDialog(RxLoadingDialog.getDefaultDialog())
                .setDialogAttribute(false, false, false)
                .setDownLoadFileAtr(filePath, fileName, true, downLoadLength)
                .setIsLogOutPut(true)
                .setIsDefaultToast(true, rxManager)
                .bindRx();
        Observable<ResponseBody> observable = builder
                //域名随便填写,但必须以“/”为结尾
                .createApi(HttpApi.class, "http://dldir1.qq.com/weixin/", rxDownLoadListener)
                .downLoad("bytes=" + downLoadLength + "-", "http://dldir1.qq.com/weixin/android/weixin666android1300.apk");
        down_Disposable = builder.beginDownLoad(observable);
        rxManager.subscribe(down_Disposable);
    }

    private RxUpLoadCallBack rxUpLoadListener = new RxUpLoadCallBack() {
        @Override
        public void onStart() {
            progressBarHorizontal.setProgress(0);
        }

        @Override
        public void onProgress(int progress, long bytesWritten, long contentLength) {
            progressBarHorizontal.setProgress(progress);
            Log.e("upLoad---- > ","progress : " + progress + "，bytesWritten : "
                    + bytesWritten + "，contentLength : " + contentLength);
        }

        @Override
        public void onFinish() {
            Toast.makeText(MainActivity.this, "onFinishUpload", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFail(String errorInfo) {
            Toast.makeText(MainActivity.this, errorInfo, Toast.LENGTH_SHORT).show();
        }
    };

    private RxDownLoadCallBack rxDownLoadListener = new RxDownLoadCallBack() {
        @Override
        public void onStart() {
            progressBarHorizontal.setProgress(0);
            titleBar.setTitleText("改变标题");
        }

        @Override
        public void onProgress(int progress, long bytesWritten, long contentLength) {
            progressBarHorizontal.setProgress(progress);
            downLoadLength += bytesWritten;
        }

        @Override
        public void onFinish() {
            Toast.makeText(MainActivity.this, "onFinishDownload", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFail(String errorInfo) {
            Toast.makeText(MainActivity.this, errorInfo, Toast.LENGTH_SHORT).show();
        }
    };
}