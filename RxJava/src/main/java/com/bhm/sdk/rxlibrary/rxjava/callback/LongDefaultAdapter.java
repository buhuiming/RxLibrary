package com.bhm.sdk.rxlibrary.rxjava.callback;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * 针对长整型的解析，先写一个解析适配器，实现JsonSerializer, JsonDeserializer
 *重写解析方法，先尝试用String类型解析，如果等于空字符串”“或者null，则返回0值
 *否则再尝试用长整型解析，并且catch数字格式异常转成Json解析异常抛出
 * Created by bhm on 2018/4/16.
 */

public class LongDefaultAdapter implements JsonSerializer<Long>, JsonDeserializer<Long> {
    @Override
    public Long deserialize(JsonElement json, Type typeOfT,
                               JsonDeserializationContext context)
            throws JsonParseException {
        try {
            if (json.getAsString() == null || json.getAsString().equals("")){
                return 0L;
            }
        } catch (Exception ignore){
            return 0L;
        }
        try {
            return json.getAsLong();
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    @Override
    public JsonElement serialize(Long src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src);
    }
}