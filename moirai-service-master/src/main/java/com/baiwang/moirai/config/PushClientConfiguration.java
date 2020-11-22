package com.baiwang.moirai.config;

import com.baiwang.cloud.spush.client.SPushClient;
import com.baiwang.spush.common.options.ServerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Auther: liuzhenyun
 * @Date: 2019-06-20 09:51
 * @Description:
 */
//@ConditionalOnProperty(name = "use.method", havingValue = "true")
@Configuration
public class PushClientConfiguration {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    //@ConditionalOnProperty(name = "use.method", havingValue = "true")
    @Bean
    public SPushClient spushClient(){
        SPushClient client = null;
        try {
            client = new SPushClient("");
            client.option(ServerOptions.CLIENT_REQUEST_CHARSET,"UTF-8");
            client.option(ServerOptions.CLIENT_CONNECT_TIMEOUT,3500);
            client.option(ServerOptions.CLIENT_MAX_PER_ROUTE,2000);
            client.option(ServerOptions.CLIENT_SCOKET_TIMEOUT,3000);
            client.init();
            client.start();
            logger.info("-----------初始化SPushClient成功，拥有推送能力--------");
        } catch (Exception e) {
            logger.error("初始化SPushClient失败",e);
        }
        return client;
    }
}
