package com.zj.cas.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;


@Configuration
public class DBConfiguration {


    @Bean
    public DataSource getDataSource() {

        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl("jdbc:mysql://localhost:3306/testdb?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&allowMultiQueries=true&useSSL=false&serverTimezone=GMT&allowPublicKeyRetrieval=true");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("root.1217");
        return druidDataSource;

    }


    @Bean
    public JdbcTemplate getJdbctempalte() {
        return new JdbcTemplate(getDataSource());
    }
}
