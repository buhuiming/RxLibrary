package com.bhm.sdk.rxlibrary.rxjava;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;

import com.bhm.sdk.rxlibrary.rxjava.callback.RxDownLoadCallBack;
import com.bhm.sdk.rxlibrary.utils.RxUtils;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/** 下载请求体
 * Created by bhm on 2018/5/11.
 */

public class DownLoadResponseBody extends ResponseBody {

    private ResponseBody responseBody;
    private RxBuilder rxBuilder;
    // BufferedSource 是okio库中的输入流，这里就当作inputStream来使用。
    private BufferedSource bufferedSource;

    public DownLoadResponseBody(ResponseBody responseBody, RxBuilder builder) {
        this.responseBody = responseBody;
        this.rxBuilder = builder;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            long totalBytesRead = rxBuilder == null ? 0L : rxBuilder.writtenLength();
            long totalBytes = rxBuilder == null ? responseBody.contentLength() :
                    rxBuilder.writtenLength() + responseBody.contentLength();

            @SuppressLint("CheckResult")
            @Override
            public long read(@NonNull Buffer sink, long byteCount) throws IOException {
                final long bytesRead = super.read(sink, byteCount);
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                if (null != rxBuilder && null != rxBuilder.getListener() &&
                        rxBuilder.getListener() instanceof RxDownLoadCallBack) {
                    if(totalBytesRead == 0 && bytesRead != -1) {
                        RxUtils.deleteFile(rxBuilder, totalBytes);
                        Observable.just(bytesRead)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<Long>() {
                                    @Override
                                    public void accept(Long aLong) throws Exception {
                                        rxBuilder.getListener().onStart();
                                        RxUtils.Logger(rxBuilder, "DownLoad-- > ", "begin downLoad");
                                    }
                                });
                    }
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    totalBytesRead + = 10;
                    if (bytesRead != -1) {
                        final int progress = (int) (totalBytesRead * 100 / totalBytes);
                        Observable.just(bytesRead)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<Long>() {
                                    @Override
                                    public void accept(Long aLong) throws Exception {
                                        rxBuilder.getListener().onProgress(progress > 100 ?
                                                100 : progress, bytesRead, totalBytes);
                                    }
                                });
                        if(totalBytesRead == totalBytes){
                            Observable.just(bytesRead)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Consumer<Long>() {
                                        @Override
                                        public void accept(Long aLong) throws Exception {
//                                             rxBuilder.getListener().onProgress(100, bytesRead, totalBytes);
//                                             rxBuilder.getListener().onFinish();
//                                             RxUtils.Logger(rxBuilder, "DownLoad-- > ", "finish downLoad");
                                            if(null != rxBuilder.getDialog() && rxBuilder.isShowDialog()) {
                                                rxBuilder.getDialog().dismissLoading(rxBuilder.getActivity());
                                            }
                                        }
                                    });
                        }
                    }
                    RxUtils.writeFile(sink.inputStream(), rxBuilder);
                }
                return bytesRead;
            }
        };
    }
}
