package com.baiwang.moirai.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {

    Logger logger = LoggerFactory.getLogger(RedisUtils.class);

    @Autowired
    @Qualifier("redisTrafficTemplate")
    private StringRedisTemplate redisTemplate;

    /**
     * set
     *
     * @param redisKey key
     * @param object   value
     * @param seconds  超时时间
     */
    public void set(String redisKey, String object, long seconds) {
        if (object != null) {
            if (seconds != -1) {
                redisTemplate.opsForValue().set(redisKey, object, seconds, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(redisKey, object);
            }
        }
    }

    /**
     * set
     *
     * @param redisKey key
     * @param object   value
     * @param seconds  超时时间
     */
    public void set(String redisKey, Object object, long seconds) {
        if (object != null) {
            String str = JSONObject.toJSONString(object);
            if (seconds != -1) {
                redisTemplate.opsForValue().set(redisKey, str, seconds, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(redisKey, str);
            }
        }
    }

    /**
     * get方法
     * @param redisKey redisKey
     */

    public <T> T get(String redisKey) {
        Object object = redisTemplate.opsForValue().get(redisKey);
        return (T)object;
    }

    /**
     * get方法
     * @param redisKey redisKey
     */

    public <T> T get(String redisKey, Class<T> tClass) {
        Object object = redisTemplate.opsForValue().get(redisKey);
        if (object != null){
            return JSON.parseObject((String) object, tClass);
        }
        return null;
    }

    /**
     * 获取列表
     */
    public <T> List getList(String redisKey, Class<T> tClass) {
        String object = get(redisKey);
        if (object == null) {
            return null;
        }
        List list = JSON.parseObject(object, List.class);
        if (list == null) {
            return null;
        }
        List<T> result = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            T item = JSON.parseObject(JSONObject.toJSONString(list.get(i)), tClass);
            result.add(item);
        }
        return result;
    }

    /**
     * 删除缓存
     * @param redisKey
     */
    public void delete(String redisKey){
        redisTemplate.delete(redisKey);
    }

    /**
     * 批量删除缓存
     * @param redisKey
     */
    public void delete(Set<String> redisKey){
        redisTemplate.delete(redisKey);
    }

    /**
     * 模糊查询
     * @param redisKey
     * @return
     */
    public Set<String> keys(String redisKey){
        return redisTemplate.keys(redisKey);
    }
}
