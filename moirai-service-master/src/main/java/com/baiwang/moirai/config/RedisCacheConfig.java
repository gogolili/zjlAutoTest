package com.baiwang.moirai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RedisCacheConfig extends CachingConfigurerSupport {

    Logger logger = LoggerFactory.getLogger(RedisCacheConfig.class);

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.password}")
    private String password;

    @Value("${spring.redis.database:1}")
    private String dataBase;

    @Value("${spring.redis.expire:10}")
    private String expire;

    @Value("${spring.redis.pool.max-active:1024}")
    private String maxActive;

    @Value("${spring.redis.pool.max-wait:10000}")
    private String maxWait;

    @Value("${spring.redis.pool.max-idle:200}")
    private String maxIdle;

    @Value("${spring.redis.pool.min-idle:50}")
    private String minIdle;

    @Value("${spring.redis.timeout:10000}")
    private String timeout;


    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        logger.info("RedisCacheConfig host = 【{}】 dataBase = 【{}】",host,dataBase);
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setHostName(this.host);
        factory.setPort(this.port);
        factory.setPassword(this.password);
        factory.setDatabase(Integer.valueOf(dataBase));
        factory.setTimeout(Integer.valueOf(timeout));
        factory.setPoolConfig(jedisPoolConfig());
        return factory;
    }

    @Bean
    public StringRedisTemplate redisTrafficTemplate() {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setDefaultSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisTemplate redisTemplate) {
        RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);

        //默认超时时间,单位秒
        cacheManager.setDefaultExpiration(3000);
        //根据缓存名称设置超时时间,0为不超时
        Map<String,Long> expires = new ConcurrentHashMap<>();
        cacheManager.setExpires(expires);

        return cacheManager;
    }

    @Bean
    public JedisPoolConfig jedisPoolConfig(){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(Integer.valueOf(maxIdle));
        jedisPoolConfig.setMinIdle(Integer.valueOf(minIdle));
        //最大的连接数
        jedisPoolConfig.setMaxTotal(Integer.valueOf(maxActive));
        //连接池中连接用完时,新的请求等待时间,毫秒。直到超时发生
        jedisPoolConfig.setMaxWaitMillis(Long.valueOf(maxWait));
        //检查连接可用性, 确保获取的redis实例可用
        jedisPoolConfig.setTestOnBorrow(true);
        return jedisPoolConfig;
    }
}
