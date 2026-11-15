package com.aaminter.starbankrecommendation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Primary
    @Bean(name = "defaultDataSource")
    @ConfigurationProperties("spring.datasource")
    public DataSource defaultDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "recommendationsDataSource")
    @ConfigurationProperties("custom.datasource.recommendations")
    public DataSource recommendationsDataSource() {
        return DataSourceBuilder.create().build();
    }
}