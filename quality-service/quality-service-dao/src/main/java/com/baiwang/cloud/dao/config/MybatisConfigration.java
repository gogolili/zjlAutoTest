package com.baiwang.cloud.dao.config;/**
 * All rights Reserved, Designed By www.baiwang.com
 *
 * @Description: TODO
 * @author: 云平台技术部   wangwei
 * @date: 2020年02月05日 15:01
 * @version v1.0
 * @Copyright: 2020 www.baiwang.com Inc. All rights reserved. 注意：本内容仅限于百望股份有限公司内部传阅，禁止外泄以及用于其他的商业目
 */

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * @Description: Mybatis 数据库配置类
 * @author: wangwei
 * @date: 2020/2/5 15:01
 */
@Configuration
@MapperScan(basePackages = "com.baiwang.cloud.dao.impl")
public class MybatisConfigration {
    @Bean
    public SqlSessionFactory sqlSessionFactory(@Autowired DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        //Mybatis扫描mapper配置文件路径
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*.xml"));
        return bean.getObject();
    }
    //事务配置
    @Bean
    public DataSourceTransactionManager testTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
    //SqlSessionTemplate 模板与数据源连接池映射
    @Bean
    public SqlSessionTemplate testSqlSessionTemplate(SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
