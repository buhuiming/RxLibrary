package com.bhm.sdk.rxlibrary.rxjava;

import com.bhm.sdk.rxlibrary.utils.ResultException;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import retrofit2.Converter;

public class GsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private final Gson gson;
    private final Type type;


    GsonResponseBodyConverter(Gson gson, Type type){
        this.gson = gson;
        this.type = type;
    }
    @Override
    public T convert(ResponseBody value) throws IOException {
        String response = value.string();
        //先将返回的json数据解析到Response中，如果code==200，则解析到我们的实体基类中，否则抛异常
        DataResponse httpResult = gson.fromJson(response, DataResponse.class);
        if (httpResult.getCode() == 200 || httpResult.getRet() == 200){
            //200的时候就直接解析，不可能出现解析异常。因为我们实体基类中传入的泛型，就是数据成功时候的格式
            return gson.fromJson(response, type);
        }else if(httpResult.getCode() == 0 && httpResult.getRet() == 0){
            try{
                //这个情况就是没有code、ret的一个json，直接给它按预定的实体解析，抛出后再抛一个自定义ResultException
                return gson.fromJson(response, type);
            }catch (Exception e) {
                //抛一个自定义ResultException 传入失败时候的状态码，和信息
                throw new ResultException(httpResult.getCode(), httpResult.getMsg(), response);
            }
        }else{
            //抛一个自定义ResultException 传入失败时候的状态码，和信息
            throw new ResultException(httpResult.getCode(), httpResult.getMsg(), response);
        }
    }
}
