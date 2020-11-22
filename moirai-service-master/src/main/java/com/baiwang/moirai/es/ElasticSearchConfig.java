package com.baiwang.moirai.es;



import com.baiwang.cloud.common.model.BaseJsonModel;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.xpack.client.PreBuiltXPackTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * ES配置类
 */
@Configuration
public class ElasticSearchConfig extends BaseJsonModel {

    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchConfig.class);

    @Value("${ElasticSearch.clusterName:Defaults}")
    private String clusterName;
    @Value("${ElasticSearch.clusterNodes:Defaults}")
    private String clusterNodes;
    @Value("${ElasticSearch.port:9300}")
    private String port;
    @Value("${ElasticSearch.security:Defaults}")
    private String security;
    @Value("${ElasticSearch.ESIndex:Defaults}")
    private String ESIndex;
    @Value("${ElasticSearch.type:Defaults}")
    private String type;
    @Value("${ElasticSearch.pool:5}")
    private String poolSize;
    // 日志输出位置默认true，表示输出到文件，经Kafka保存到ES
    @Value("${aspect.bwcloud:true}")
    private boolean aspectBwcloud;

    public ElasticSearchConfig() {
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getClusterNodes() {
        return clusterNodes;
    }

    public void setClusterNodes(String clusterNodes) {
        this.clusterNodes = clusterNodes;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getSecurity() {
        return security;
    }

    public void setSecurity(String security) {
        this.security = security;
    }

    public String getESIndex() {
        return ESIndex;
    }

    public void setESIndex(String ESIndex) {
        this.ESIndex = ESIndex;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(String poolSize) {
        this.poolSize = poolSize;
    }

    @Bean
    public TransportClient init() throws UnknownHostException {
        logger.info("es config clusterNodes={} clusterName={} security={} type={} index={}",clusterNodes,clusterName,security,type,ESIndex);
        TransportClient transportClient = null;
        Settings esSetting = Settings.builder()
                .put("cluster.name", clusterName)
                .put("client.transport.sniff", false)
                .put("xpack.security.user", security)
                .put("thread_pool.search.size", Integer.parseInt(poolSize))
                .build();
        try{
            transportClient = new PreBuiltXPackTransportClient(esSetting);
            InetSocketTransportAddress inetSocketTransportAddress =
                    new InetSocketTransportAddress(InetAddress.getByName(clusterNodes), Integer.valueOf(port));
            transportClient.addTransportAddresses(inetSocketTransportAddress);
        }catch (Exception e){
            logger.error("ElasticSearchConfig初始化异常 e = ",e);
        }
        return transportClient;
    }
}