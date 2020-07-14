package com.bhm.sdk.rxlibrary.rxjava;

import android.app.Activity;
import android.widget.Toast;

import com.bhm.sdk.rxlibrary.rxjava.callback.CallBack;
import com.bhm.sdk.rxlibrary.rxjava.callback.RxStreamCallBackImp;
import com.bhm.sdk.rxlibrary.utils.ResultException;
import com.bhm.sdk.rxlibrary.utils.RxLoadingDialog;
import com.bhm.sdk.rxlibrary.utils.RxUtils;
import com.google.gson.JsonSyntaxException;
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;

import java.io.InputStream;
import java.util.concurrent.TimeoutException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

/**
 * Created by bhm on 2018/5/11.
 */

public class RxBuilder {

    private Builder builder;
    private CallBack callBack;
    private RxStreamCallBackImp listener;

    public RxBuilder(@NonNull Builder builder){
        this.builder = builder;
    }

    public RxAppCompatActivity getActivity() {
        return builder.activity;
    }

    public boolean isShowDialog() {
        return builder.isShowDialog;
    }

    public boolean isCancelable() {
        return builder.cancelable;
    }

    public CallBack getCallBack() {
        return callBack;
    }

    public boolean isLogOutPut() {
        return builder.isLogOutPut;
    }

    public boolean isCanceledOnTouchOutside() {
        return builder.isCanceledOnTouchOutside;
    }

    public RxLoadingDialog getDialog() {
        return builder.dialog;
    }

    public boolean isDefaultToast() {
        return builder.isDefaultToast;
    }

    public RxManager getRxManager() {
        return builder.rxManager;
    }

    public int getReadTimeOut() {
        return builder.readTimeOut;
    }

    public int getConnectTimeOut() {
        return builder.connectTimeOut;
    }

    public OkHttpClient getOkHttpClient() {
        return builder.okHttpClient;
    }

    public RxStreamCallBackImp getListener() {
        return listener;
    }

    public String getFilePath(){
        return builder.filePath;
    }

    public String getFileName(){
        return builder.fileName;
    }

    public long writtenLength(){
        return builder.writtenLength;
    }

    public boolean isAppendWrite(){
        return builder.appendWrite;
    }

    public String getLoadingTitle(){
        return builder.loadingTitle;
    }

    public <T> T createApi(Class<T> cla, String host){
        if(builder.isShowDialog && null != builder.dialog){
            builder.dialog.showLoading(this);
        }
        return new RetrofitCreateHelper(this)
                .createApi(cla, host);
    }

    /** 上传请求
     * @param cla
     * @param host 请求地址
     * @param listener
     * @return
     */
    public <T> T createApi(Class<T> cla, String host, RxStreamCallBackImp listener){
        if(null == listener){
            throw new NullPointerException("RxStreamCallBackImp(listener) can not be null!");
        }
        if(builder.isShowDialog && null != builder.dialog){
            builder.dialog.showLoading(this);
        }
        this.listener = listener;
        return new RetrofitCreateHelper(this)
                .createApi(cla, host);
    }

    public <T> Disposable setCallBack(Observable<T> observable, final CallBack<T> callBack){
        this.callBack = callBack;
        return observable.compose(builder.activity.bindToLifecycle())////管理生命周期
                .compose(RxManager.rxSchedulerHelper())//发布事件io线程
                .subscribe(getBaseConsumer(),
                        getThrowableConsumer(),
                        getDefaultAction(),
                        getConsumer());
    }

    private <T> Consumer<T> getBaseConsumer(){
        return new Consumer<T>(){
            @Override
            public void accept(T t) throws Exception {
                if(null != getCallBack()){
                    getCallBack().onSuccess(t);
                }
                if(isShowDialog() && null != getDialog()){
                    getDialog().dismissLoading(getActivity());
                }
            }
        };
    }

    private Consumer<Throwable> getThrowableConsumer(){
        return new Consumer<Throwable>() {
            @Override
            public void accept(Throwable e) throws Exception {
                if(null == e){
                    return;
                }
                RxUtils.Logger(RxBuilder.this, "ThrowableConsumer-> ", e.getMessage());//抛异常
                if(null != getCallBack()){
                    getCallBack().onFail(e);
                }
                if(isShowDialog() && null != getDialog()){
                    getDialog().dismissLoading(getActivity());
                }
                if(isDefaultToast()) {
                    if (e instanceof HttpException) {
                        if (((HttpException) e).code() == 404) {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        } else if (((HttpException) e).code() == 504) {
                            Toast.makeText(getActivity(), "请检查网络连接！", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "请检查网络连接！", Toast.LENGTH_SHORT).show();
                        }
                    } else if (e instanceof IndexOutOfBoundsException
                            || e instanceof NullPointerException
                            || e instanceof JsonSyntaxException
                            || e instanceof IllegalStateException
                            || e instanceof ResultException) {
                        Toast.makeText(getActivity(), "数据异常，解析失败！", Toast.LENGTH_SHORT).show();
                    } else if (e instanceof TimeoutException) {
                        Toast.makeText(getActivity(), "连接超时，请重试！", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "请求失败，请稍后再试！", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
    }

    /** 最终结果的处理
     * @return 和getThrowableConsumer互斥
     */
    private Action getDefaultAction(){
        return new Action() {
            @Override
            public void run() throws Exception {
                if(null != getCallBack()){
                    getCallBack().onComplete();
                }
                if(isShowDialog() && null != getDialog()){
                    getDialog().dismissLoading(getActivity());
                }
            }
        };
    }

    /** 做准备工作
     * @return
     */
    private Consumer<Disposable> getConsumer(){
        return new Consumer<Disposable>() {
            @Override
            public void accept(Disposable disposable) throws Exception {
                //做准备工作
                if(null != getCallBack()){
                    getCallBack().onStart(disposable);
                }
                if(builder.rxManager != null){
                    builder.rxManager.subscribe(disposable);
                }
            }
        };
    }

    public Disposable beginDownLoad(@androidx.annotation.NonNull Observable<ResponseBody> observable){
        return observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .map(new Function<ResponseBody, InputStream>() {
                    @Override
                    public InputStream apply(@NonNull ResponseBody responseBody) throws Exception {
                        return responseBody.byteStream();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<InputStream>() {
                    @Override
                    public void accept(InputStream inputStream) throws Exception {

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        if(null != listener) {
                            listener.onFail(throwable.getMessage());
                            if(null != builder.dialog && builder.isShowDialog) {
                                builder.dialog.dismissLoading(builder.activity);
                            }
                        }
                        if(builder.rxManager != null) {
                            builder.rxManager.removeObserver();
                        }
                    }
                });
    }

    public static Builder newBuilder(RxAppCompatActivity activity) {
        return new Builder(activity);
    }

    public static final class Builder {

        private RxAppCompatActivity activity;
        private RxManager rxManager;
        private boolean isShowDialog = RxConfig.isShowDialog();
        private boolean cancelable = RxConfig.cancelable();
        private boolean isCanceledOnTouchOutside = RxConfig.isCanceledOnTouchOutside();
        private RxLoadingDialog dialog = RxConfig.getRxLoadingDialog();
        private boolean isDefaultToast = RxConfig.isDefaultToast();
        private int readTimeOut = RxConfig.getReadTimeOut();
        private int connectTimeOut = RxConfig.getConnectTimeOut();
        private OkHttpClient okHttpClient = RxConfig.getOkHttpClient();
        private boolean isLogOutPut = RxConfig.isLogOutPut();
        private String filePath = RxConfig.getFilePath();
        private String fileName = RxConfig.getFileName();
        private long writtenLength = RxConfig.writtenLength();
        private boolean appendWrite = RxConfig.isAppendWrite();
        private String loadingTitle = RxConfig.getLoadingTitle();

        public Builder(RxAppCompatActivity activity) {
            this.activity = activity;
        }

        public Builder setLoadingDialog(RxLoadingDialog dialog){
            this.dialog = dialog;
            return this;
        }

        public Builder setDialogAttribute(boolean isShowDialog, boolean cancelable, boolean isCanceledOnTouchOutside){
            this.isShowDialog = isShowDialog;
            this.cancelable = cancelable;
            this.isCanceledOnTouchOutside = isCanceledOnTouchOutside;
            return this;
        }

        public Builder setIsDefaultToast(boolean isDefaultToast, RxManager rxManager){
            this.isDefaultToast = isDefaultToast;
            this.rxManager = rxManager;
            return this;
        }

        public Builder setRxManager(RxManager rxManager){
            this.rxManager = rxManager;
            return this;
        }

        public Builder setHttpTimeOut(int readTimeOut, int connectTimeOut){
            this.readTimeOut = readTimeOut;
            this.connectTimeOut = connectTimeOut;
            return this;
        }
        /** 不推荐使用，使用此方法，将取消默认的设置，包括但不限于日志，缓存，下载，上传，网络，SSL。
         * @param okHttpClient
         * @return
         */
        @Deprecated
        public Builder setOkHttpClient(OkHttpClient okHttpClient){
            this.okHttpClient = okHttpClient;
            return this;
        }

        public Builder setIsLogOutPut(boolean isLogOutPut){
            this.isLogOutPut = isLogOutPut;
            return this;
        }

        public Builder setLoadingTitle(String loadingTitle){
            this.loadingTitle = loadingTitle;
            return this;
        }

        public Builder setDownLoadFileAtr(String mFilePath, String mFileName, boolean mAppendWrite, long mWrittenLength){
            filePath = mFilePath;
            fileName = mFileName;
            appendWrite = mAppendWrite;
            writtenLength = mWrittenLength;
            return this;
    }

        public RxBuilder bindRx(){
            return new RxBuilder(this);
        }
    }
}
