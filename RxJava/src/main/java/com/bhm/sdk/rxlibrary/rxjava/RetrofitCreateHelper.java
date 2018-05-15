package com.bhm.sdk.rxlibrary.rxjava;


import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.URLDecoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.bhm.sdk.rxlibrary.rxjava.HttpCache.getUserAgent;


/**
 * Created by bhm on 2018/5/11.
 */

public class RetrofitCreateHelper {

    private int TIMEOUT_READ = 30;//读取超时
    private int TIMEOUT_CONNECTION = 20;//连接超时
    private Activity activity;
    private OkHttpClient okHttpClient;
    private boolean isLogOutPut = true;
    private RxDownLoadListener downLoadListener;
    private StringBuilder mMessage = new StringBuilder();

    /**
     * @param activity
     */
    public RetrofitCreateHelper(Activity activity){
        this.activity = activity;
    }

    /**
     * @param readTimeOut
     * @param connectTimeOut
     * @return
     */
    public RetrofitCreateHelper setHttpTimeOut(int readTimeOut, int connectTimeOut){
        if(readTimeOut > 0){
            TIMEOUT_READ = readTimeOut;
        }
        if(connectTimeOut > 0){
            TIMEOUT_CONNECTION = connectTimeOut;
        }
        return this;
    }

    public RetrofitCreateHelper setOkHttpClient(OkHttpClient client){
        this.okHttpClient = client;
        return this;
    }

    public RetrofitCreateHelper setIsLogOutPut(boolean isLogOutPut){
        this.isLogOutPut = isLogOutPut;
        return this;
    }

    private OkHttpClient getOkHttpClient(){
        if(null != okHttpClient){
            return okHttpClient;
        }
        return new OkHttpClient.Builder()
                .sslSocketFactory(getUnsafeOkHttpClient(), new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                })//SSL证书
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return false;
                    }
                })
                .addInterceptor(interceptor)//打印日志
                .addInterceptor(cacheInterceptor)
                .addInterceptor(downInterceptor)
                .addNetworkInterceptor(cacheInterceptor)//设置Cache拦截器
                .cache(HttpCache.getCache(activity))
                .connectTimeout(TIMEOUT_CONNECTION, TimeUnit.SECONDS)//time out
                .readTimeout(TIMEOUT_READ, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_READ, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)//失败重连
                .build();
    }

    /**
     * @param clazz
     * @param url
     * @param <T>
     * @return
     */
    public  <T> T createApi(Class<T> clazz, String url) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .client(getOkHttpClient())
                //把retrofit的工作放到Observable的工作流里
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                //GsonConverterFactory是告诉retrofit我们怎么去解析数据，有可能是xml，有可能是json。
                .addConverterFactory(GsonConverterFactory.create(getGsonBuilder()))
                .build();
        return retrofit.create(clazz);
    }


    /**
     *  http拦截器，打印数据
     */
    private final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(

            new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    if(!isLogOutPut){
                        return;
                    }
                    // 请求或者响应开始
                    if (message.startsWith("--> POST") || message.startsWith("--> GET")) {
                        mMessage.delete(0,mMessage.length());
                        mMessage.setLength(0);
                    }

                    if(message.contains("&")){
                        Log.i("RetrofitCreateHelper-> ", stringToKeyValue(message));
                    }
                    // 以{}或者[]形式的说明是响应结果的json数据，需要进行格式化
                    if ((message.startsWith("{") && message.endsWith("}"))
                            || (message.startsWith("[") && message.endsWith("]"))) {
                        Log.i("RetrofitCreateHelper-> ", replaceBlank(message.toString()) + "\n");
                    }
                    mMessage.append(message.concat("\n"));
                    // 响应结束，打印整条日志
                    if (message.startsWith("<-- END HTTP")) {
                        Log.i("RetrofitCreateHelper-> ", mMessage.toString());
                    }
                }
            }).setLevel(HttpLoggingInterceptor.Level.BODY);

    /**
     * 拦截器 设置相关参数
     */
    private Interceptor cacheInterceptor = new Interceptor(){
        @Override
        public Response intercept(Chain chain) throws IOException {
//            //拿到cookies 根据业务进行处理
//            Response response = chain.proceed(chain.request());
//            ResponseBody body = response.body();
//            BufferedSource source = body.source();
//            source.request(Long.MAX_VALUE); // Buffer the entire body.
//            Buffer buffer = source.buffer();
//            Charset charset = Charset.defaultCharset();
//            MediaType contentType = body.contentType();
//            if (contentType != null) {
//                charset = contentType.charset(charset);
//            }
//            String bodyString = buffer.clone().readString(charset);

            Request request = chain.request();
            if (isNetworkConnected(activity)) {
                // 有网络时, 缓存5s，根据实际情况设置
                int maxAge = 5;
                request = request.newBuilder()
                        .removeHeader("User-Agent")
                        .header("User-Agent", getUserAgent(activity))
                        .build();

                Response response = chain.proceed(request);
                return response.newBuilder()
                        .removeHeader("Pragma")
                        .removeHeader("Cache-Control")
                        .header("Cache-Control", "public, max-age=" + maxAge)
                        .build();
            } else {
                // 无网络时，缓存为3天
                int maxStale = 60 * 60 * 24 * 3;
                request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_CACHE)
                        .removeHeader("User-Agent")
                        .header("User-Agent", getUserAgent(activity))
                        .build();

                Response response = chain.proceed(request);
                return response.newBuilder()
                        .removeHeader("Pragma")
                        .removeHeader("Cache-Control")
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                        .build();
            }
        }
    };

    /**
     * 下载进度拦截器
     */
    private Interceptor downInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Response response = chain.proceed(chain.request());
            if(null == downLoadListener){
                return response;
            }
            return response.newBuilder().body(
                    new CSResponseBody(response.body(), downLoadListener)).build();
        }
    };

    private SSLSocketFactory getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final X509TrustManager[] trustAllCerts = new X509TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(
                        X509Certificate[] chain,
                        String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(
                        X509Certificate[] chain,
                        String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }};

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts,
                    new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext
                    .getSocketFactory();

            return sslSocketFactory;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Gson getGsonBuilder(){
        return new GsonBuilder()
//                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .registerTypeAdapter(Integer.class, new IntegerDefaultAdapter())
                .registerTypeAdapter(int.class, new IntegerDefaultAdapter())
                .create();
    }

    /** 设置下载监听
     * @param downLoadListener
     * @return
     */
    public RetrofitCreateHelper setDownLoadListener(RxDownLoadListener downLoadListener){
        this.downLoadListener = downLoadListener;
        return this;
    }

    /**
     *  去除字符串中的空格、回车、换行符、制表符
     */
    private String replaceBlank(String str) {
        String dest = "";
        if (str!=null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    private String stringToKeyValue(String msg){
        StringBuilder mMessage = new StringBuilder();
        try {
            msg = URLDecoder.decode(msg, "UTF-8");
            mMessage.append("\n");
            if(msg.contains("?") && !msg.endsWith("?")) {
                mMessage.append(msg.substring(0, msg.lastIndexOf("?")));
                msg = msg.substring(msg.lastIndexOf("?") + 1, msg.length());
                mMessage.append("\n");
            }
            for (String key : msg.split("&")) {
                mMessage.append(key);
                mMessage.append("\n");
            }
            mMessage.append("\n");
        }catch (Exception e){
            mMessage.append("");
        }
        return mMessage.toString().replace("=","  =  ");
    }

    /**
     * 判断是否有网络
     *
     * @return 返回值
     */
    private boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();

            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }
}