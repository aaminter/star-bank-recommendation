package com.aaminter.starbankrecommendation.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Configures the dedicated data source for recommendation rules and statistics.
 */
@Configuration
public class DataSourceConfig {

    /**
     * Creates the data source using {@code custom.datasource.recommendations} properties.
     *
     * @return recommendation database data source
     */
    @Bean(name = "recommendationsDataSource", defaultCandidate = false)
    @Qualifier("recommendationsDataSource")
    @ConfigurationProperties("custom.datasource.recommendations")
    public DataSource recommendationsDataSource() {
        return DataSourceBuilder.create().build();
    }
}
