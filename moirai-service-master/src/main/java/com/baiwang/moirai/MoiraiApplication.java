/*
 * @项目名称: Moirai-service
 * @文件名称: CostingApplication.java
 * @Date: 17-9-22 下午12:25
 * @author Lance cui
 *
 */

package com.baiwang.moirai;

import com.baiwang.cloud.logaop.EnableBwLogAop;
import com.baiwang.cloud.logaop.aspect.ServiceLogAspect;
import com.baiwang.cloud.logaop.utils.PropertiesUtil;
import com.baiwang.moirai.serviceimpl.PrintLogWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.sql.DataSource;

@SpringBootApplication(scanBasePackages = {"com.baiwang.moirai", "com.baiwang.cloud"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableBwLogAop(proxyTargetClass = true)
public class MoiraiApplication extends WebMvcConfigurerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(MoiraiApplication.class);

    // 创建事务管理器
    @Bean(name = "transactionManager")
    public PlatformTransactionManager txManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    public static void main(String[] args) {
        SpringApplication.run(MoiraiApplication.class, args);
        //本地部署启动--两参数的设置值均为false,则日志收集启动成功
        //本地部署启动设置环境变量：例如 java -jar -Daspect.bwcloud=false moirai-service.jar --spring.profiles.active=dkh
        boolean isShortcircuit = PropertiesUtil.getBooleanValue("aspect.shortcircuit", false);
        boolean bwcloud = PropertiesUtil.getBooleanValue("aspect.bwcloud", true);
        logger.info("ServiceLogAspect shortcircuit = 【{}】 bwcloud = 【{}】", isShortcircuit,bwcloud);
    }

    @Primary
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean("loadNoBalanced")
    RestTemplate loadNoBalanced() {
        return new RestTemplate();
    }
}
