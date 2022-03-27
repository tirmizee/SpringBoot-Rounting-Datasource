package com.tirmizee.configs;

import com.tirmizee.adapters.MasterReplicaRoutingDataSource;
import com.tirmizee.constants.DatasourceType;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.tirmizee.repositories")
public class DatasourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.master.hikari")
    public DataSource masterDataSource() {
        return new HikariDataSource();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.slave.hikari")
    public DataSource slaveDataSource() {
        return new HikariDataSource();
    }

    @Bean(name = "jpaProperty")
    @ConfigurationProperties("spring.datasource.jpa.properties")
    public Properties jpaProperty() {
        return new Properties();
    }

    @Bean(name = "routingDataSource")
    public MasterReplicaRoutingDataSource routingDataSource() {
        Map<Object, Object> datasourceMap = new HashMap<>();
        datasourceMap.put(DatasourceType.READ_ONLY, slaveDataSource());
        datasourceMap.put(DatasourceType.READ_WRITE, masterDataSource());
        MasterReplicaRoutingDataSource routingDataSource = new MasterReplicaRoutingDataSource();
        routingDataSource.setDefaultTargetDataSource(masterDataSource());
        routingDataSource.setTargetDataSources(datasourceMap);
        return routingDataSource;
    }

    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory( @Qualifier("routingDataSource") DataSource dataSource) {
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.getJpaDialect().setPrepareConnection(false); // ให้ support TransactionSynchronizationManager.isCurrentTransactionReadOnly()
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan(new String[]{"com.tirmizee.entities"});
        factory.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        factory.setJpaProperties(jpaProperty());
        return factory;
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("entityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory){
        return new JpaTransactionManager(entityManagerFactory.getObject());
    }

}
