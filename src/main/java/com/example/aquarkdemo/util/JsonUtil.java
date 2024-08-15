package com.example.aquarkdemo.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import java.util.LinkedList;
import java.util.List;

/**
 * Json 工具類
 */
@Slf4j
@RequiredArgsConstructor
public class JsonUtil {

    private static ObjectMapper instance = getInstance();

    // 取得單例物件
    public static synchronized ObjectMapper getInstance() {
        if (instance == null) {
            instance = getObjectMapperInstance();
        }
        return instance;
    }


    /**
     * 序列化成 json
     * @param obj 序列化的物件
     * @return json
     * @throws JsonProcessingException
     */
    public static String serialize(final Object obj) throws JsonProcessingException {
        return instance.writeValueAsString(obj);
    }

    /**
     * 反序列化
     * @param json json
     * @param clazz 反序列化後的物件類型
     * @return 反序列化後的物件
     * @throws JsonProcessingException
     */
    public static <T> T deserialize(String json, final Class<T> clazz) throws JsonProcessingException {
        if(json == null) {
            return null;
        }
        if (json.startsWith("\"") && json.endsWith("\"")) {
            json = json.substring(1, json.length() - 1);
        }

        json = json.replace("\\\"", "\"");
        return instance.readValue(json, clazz);
    }

    /**
     * 反序列化
     * @param json json
     * @param typeReference 反序列化後的物件類型
     * @return 反序列化後的物件
     * @throws JsonProcessingException
     */
    public static <T> T deserialize(String json, final TypeReference<T> typeReference) throws JsonProcessingException {
        if(json == null) {
            return null;
        }
        if (json.startsWith("\"") && json.endsWith("\"")) {
            json = json.substring(1, json.length() - 1);
        }

        json = json.replace("\\\"", "\"");
        return instance.readValue(json, typeReference);
    }

    /**
     * 使用 Stream 反序列化
     * @param json json字串
     * @param clazz 反序列化後的物件類型
     * @return 反序列化後的物件
     * @param <T> 泛型
     * @throws IOException
     */
    public static <T> List<T> streamDeserializeToList(String json, final Class<T> clazz) throws IOException {
        if(json == null) {
            return null;
        }
        List<T> list = new LinkedList<>();
        JsonFactory jsonFactory = getInstance().getFactory();
        try (JsonParser jsonParser = jsonFactory.createParser(json)) {

            if(jsonParser.nextToken() != JsonToken.END_ARRAY) {
                throw new IllegalArgumentException("JSON 格式錯誤");
            }
            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                T obj = getInstance().readValue(jsonParser, clazz);
                list.add(obj);
            }
            return list;
        }
    }

    /**
     * 使用 Stream 反序列化
     * @param json json字串
     * @param clazz 反序列化後的物件類型
     * @return 反序列化後的物件
     * @param <T> 泛型
     * @throws IOException
     */
    public static <T> T streamDeserialize(String json, final Class<T> clazz) throws IOException {
        if(json == null) {
            return null;
        }
        JsonFactory jsonFactory = getInstance().getFactory();
        try (JsonParser jsonParser = jsonFactory.createParser(json)) {
            if(jsonParser.nextToken() != JsonToken.END_OBJECT) {
                throw new IllegalArgumentException("JSON 格式錯誤");
            }
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                T obj = getInstance().readValue(jsonParser, clazz);
                return obj;
            }
        }
        return null;
    }

    /**
     * 使用 Stream 反序列化
     * @param json json字串
     * @param typeReference 反序列化後的物件類型
     * @return 反序列化後的物件
     * @param <T> 泛型
     * @throws IOException
     */
    public static <T> T streamDeserialize(String json, final TypeReference<T> typeReference) throws IOException {
        if(json == null) {
            return null;
        }
        JsonFactory jsonFactory = getInstance().getFactory();
        try (JsonParser jsonParser = jsonFactory.createParser(json)) {
            if (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                throw new IllegalArgumentException("JSON 格式錯誤");
            }
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                T obj = getInstance().readValue(jsonParser, typeReference);
                return obj;
            }
        }
        return null;
    }

    /**
     * 使用 Stream 反序列化
     * @param json json字串
     * @param typeReference 反序列化後的物件類型
     * @return 反序列化後的物件
     * @param <T> 泛型
     * @throws IOException
     */
    public static <T> List<T> streamDeserializeToList(String json, final TypeReference<T> typeReference) throws IOException {
        if(json == null) {
            return null;
        }
        List<T> list = new LinkedList<>();
        JsonFactory jsonFactory = getInstance().getFactory();
        try (JsonParser jsonParser = jsonFactory.createParser(json)) {

            if(jsonParser.nextToken() != JsonToken.END_ARRAY) {
                throw new IllegalArgumentException("JSON 格式錯誤");
            }
            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                T obj = getInstance().readValue(jsonParser, typeReference);
                list.add(obj);
            }
            return list;
        }
    }



    /**
     * 取得 ObjectMapper 實例
     * @return ObjectMapper
     */
    private static ObjectMapper getObjectMapperInstance() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setDateFormat(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());
        return objectMapper;
    }

}
