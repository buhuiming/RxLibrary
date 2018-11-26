package com.bhm.sdk.rxlibrary.rxjava;

import com.bhm.sdk.rxlibrary.utils.RxLoadingDialog;

import okhttp3.OkHttpClient;

/**
 * Created by bhm on 2018/11/13.
 */

public class RxConfig {

    private static RxLoadingDialog dialog;
    private static boolean isShowDialog;
    private static boolean cancelable;
    private static boolean isCanceledOnTouchOutside;
    private static boolean isDefaultToast;
    private static int readTimeOut;
    private static int connectTimeOut;
    private static OkHttpClient okHttpClient;
    private static boolean isLogOutPut = false;
    private static String filePath;
    private static String fileName;
    private static boolean isDeleteOldFile;

    public RxConfig(Builder builder){
        dialog = builder.dialog;
        isShowDialog = builder.isShowDialog;
        cancelable = builder.cancelable;
        isCanceledOnTouchOutside = builder.isCanceledOnTouchOutside;
        isDefaultToast = builder.isDefaultToast;
        readTimeOut = builder.readTimeOut;
        connectTimeOut = builder.connectTimeOut;
        okHttpClient = builder.okHttpClient;
        isLogOutPut = builder.isLogOutPut;
        filePath = builder.filePath;
        fileName = builder.fileName;
    }

    public static RxConfig.Builder newBuilder() {
        return new RxConfig.Builder();
    }

    public static final class Builder {
        private RxLoadingDialog dialog;
        private boolean isShowDialog;
        private boolean cancelable;
        private boolean isCanceledOnTouchOutside;
        private boolean isDefaultToast;
        private int readTimeOut;
        private int connectTimeOut;
        private OkHttpClient okHttpClient;
        private boolean isLogOutPut = false;
        private String filePath;
        private String fileName;

        public Builder setRxLoadingDialog(RxLoadingDialog setDialog){
            dialog = setDialog;
            return this;
        }

        public Builder setOkHttpClient(OkHttpClient setOkHttpClient){
            okHttpClient = setOkHttpClient;
            return this;
        }

        public Builder setConnectTimeOut(int setConnectTimeOut){
            connectTimeOut = setConnectTimeOut;
            return this;
        }

        public Builder setReadTimeOut(int setReadTimeOut){
            readTimeOut = setReadTimeOut;
            return this;
        }

        public Builder setDialogAttribute(boolean isShowDialog, boolean cancelable, boolean isCanceledOnTouchOutside){
            this.isShowDialog = isShowDialog;
            this.cancelable = cancelable;
            this.isCanceledOnTouchOutside = isCanceledOnTouchOutside;
            return this;
        }

        public Builder isDefaultToast(boolean defaultToast){
            isDefaultToast = defaultToast;
            return this;
        }

        public Builder isLogOutPut(boolean logOutPut){
            isLogOutPut = logOutPut;
            return this;
        }

        public Builder setDownLoadFileAtr(String mFilePath, String mFileName, boolean mIsDeleteOldFile){
            filePath = mFilePath;
            fileName = mFileName;
            isDeleteOldFile = mIsDeleteOldFile;
            return this;
        }

        public RxConfig build(){
            return new RxConfig(this);
        }
    }

    public static RxLoadingDialog getRxLoadingDialog(){
        return dialog;
    }

    public static OkHttpClient getOkHttpClient(){
        return okHttpClient;
    }

    public static int getConnectTimeOut(){
        return connectTimeOut;
    }

    public static int getReadTimeOut(){
        return readTimeOut;
    }

    public static boolean isShowDialog(){
        return isShowDialog;
    }

    public static boolean cancelable(){
        return cancelable;
    }

    public static boolean isCanceledOnTouchOutside(){
        return isCanceledOnTouchOutside;
    }

    public static boolean isDefaultToast(){
        return isDefaultToast;
    }

    public static boolean isLogOutPut(){
        return isLogOutPut;
    }

    public static String getFilePath(){
        return filePath;
    }

    public static String getFileName(){
        return fileName;
    }

    public static boolean isDeleteOldFile(){
        return isDeleteOldFile;
    }
}