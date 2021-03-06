package com.bhm.sdk.rxlibrary.rxjava;

import com.bhm.sdk.rxlibrary.utils.ResultException;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
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
        if(httpResult == null){
            //这种情况是请求成功，但是json不是合理的
            throw new ResultException(200, "json is illegal", response);
        }else if (httpResult.getCode() == 200 || httpResult.getRet() == 200){
            //200的时候就直接解析
            try{
                return gson.fromJson(response, type);
            }catch (Exception e) {
                if(httpResult.getData().toString().equals("[]")){
                    //这种情况是一个空数组，但是声明的却不是一个数组
                    httpResult.setData(null);
                    return gson.fromJson(gson.toJson(httpResult), type);
                }else {
                    throw new ResultException(httpResult.getCode(), httpResult.getMsg(), response);
                }
            }
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
