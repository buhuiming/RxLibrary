package com.bhm.sdk.rxlibrary.rxjava;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by bhm on 2018/5/10.
 * 用于管理Rxjava 注册订阅和取消订阅
 */

public class RxManager {

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();//管理订阅者者
    private List<Disposable> list = new ArrayList<>();

    public void subscribe(Disposable d) {
        if(null == list){
            list = new ArrayList<>();
        }
        if(!list.contains(d)) {
            list.add(d);
            mCompositeDisposable.add(d);//注册订阅
        }
    }

    /**
     * 清空监听，再次调用需new CompositeDisposable()
     */
    public void unSubscribe() {
        mCompositeDisposable.dispose();//取消订阅  activity销毁时调用
        list = null;
    }

    /**
     * 取消一个请求
     */
    public void removeObserver(){//中断监听 取消请求
        if(null != list && list.size() > 0) {
            mCompositeDisposable.remove(list.get(list.size() - 1));
            list.remove(list.get(list.size()-1));
        }
    }

    /**
     * 取消一个请求
     */
    public void removeObserver(Disposable disposable){//中断监听 取消请求
        if(null != disposable) {
            mCompositeDisposable.remove(disposable);
            if(list != null){
                list.remove(disposable);
            }
        }
    }

    public boolean isExitObserver(){
        if(null != list && list.size() > 0) {
            return isExitObserver(list.get(list.size()-1));
        }
        return false;
    }

    public boolean isExitObserver(Disposable disposable){
        return null != list && list.contains(disposable);
    }

    /**
     * 统一线程处理
     * <p>
     * 发布事件io线程，接收事件主线程
     */
    public static <T> ObservableTransformer<T, T> rxSchedulerHelper() {//compose处理线程
        return new ObservableTransformer<T, T>() {

            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                return upstream.subscribeOn(Schedulers.io())//读写文件、读写数据库、网络信息交互等
                        .observeOn(AndroidSchedulers.mainThread());//指定的是它之后的操作所在的线程。
            }
        };
    }
}
