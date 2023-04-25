package cn.gmlee.tools.dt.conf;

import cn.gmlee.tools.ds.config.dynamic.DynamicDatasourceAutoConfiguration;
import cn.gmlee.tools.dt.core.DtTransactionInterceptor;
import cn.gmlee.tools.dt.repository.TxRepository;
import cn.gmlee.tools.dt.server.CcServer;
import cn.gmlee.tools.dt.server.TxServer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Role;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * 事务管理配置类.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(DtProperties.class)
@AutoConfigureAfter({DynamicDatasourceAutoConfiguration.class, TransactionAutoConfiguration.class})
public class DtTransactionManagementAutoConfiguration {

    @Bean
    public TxRepository txRepository(){
        return new TxRepository();
    }

    @Bean
    public TxServer txServer(){
        return new TxServer();
    }

    @Bean
    public CcServer ccServer(){
        return new CcServer();
    }

    @Primary
    @Bean("DtTransactionInterceptor")
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public TransactionInterceptor transactionInterceptor(TransactionManager transactionManager) {
        TransactionInterceptor interceptor = new DtTransactionInterceptor();
        interceptor.setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
        interceptor.setTransactionManager(transactionManager);
        return interceptor;
    }
}
