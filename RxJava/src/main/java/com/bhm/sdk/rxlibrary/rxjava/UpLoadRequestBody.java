package com.bhm.sdk.rxlibrary.rxjava;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;

import com.bhm.sdk.rxlibrary.rxjava.callback.RxUpLoadCallBack;
import com.bhm.sdk.rxlibrary.utils.RxUtils;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Created by bhm on 2018/5/28.
 */

public class UpLoadRequestBody extends RequestBody {
    private RequestBody mRequestBody;
    private RxBuilder rxBuilder;

    public UpLoadRequestBody(RequestBody requestBody, RxBuilder builder) {
        this.mRequestBody = requestBody;
        this.rxBuilder = builder;
    }

    @Override
    public MediaType contentType() {
        return mRequestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        try {
            return mRequestBody.contentLength();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        BufferedSink bufferedSink;
        CountingSink mCountingSink = new CountingSink(sink);
        bufferedSink = Okio.buffer(mCountingSink);

        mRequestBody.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    class CountingSink extends ForwardingSink {

        private long bytesWritten = 0L;
        private long contentLength = 0L;

        CountingSink(Sink delegate) {
            super(delegate);
        }

        @SuppressLint("CheckResult")
        @Override
        public void write(@NonNull Buffer source, final long byteCount) throws IOException {
            super.write(source, byteCount);
            if (null != rxBuilder && null != rxBuilder.getListener() &&
                    rxBuilder.getListener() instanceof RxUpLoadCallBack) {
                if(contentLength == 0L){
                    contentLength = contentLength();
                }
                if (bytesWritten == 0) {
                    Observable.just(bytesWritten)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<Long>() {
                                @Override
                                public void accept(Long aLong) throws Exception {
                                    rxBuilder.getListener().onStart();
                                    RxUtils.Logger(rxBuilder, "upLoad-- > ", "begin upLoad");
                                }
                            });
                }
                bytesWritten += byteCount;
                final int progress = (int) (bytesWritten * 100 / contentLength);
                Observable.just(bytesWritten)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Long>() {
                            @Override
                            public void accept(Long aLong) throws Exception {
//                                rxBuilder.getListener().onProgress(progress, byteCount, contentLength);
                                rxBuilder.getListener().onProgress(progress > 100 ?
                                        100 : progress, byteCount, contentLength);
                            }
                        });
            }
        }
    }
}
