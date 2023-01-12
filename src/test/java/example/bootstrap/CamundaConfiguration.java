package example.bootstrap;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.StrongUuidGenerator;
import org.camunda.bpm.engine.spring.ProcessEngineFactoryBean;
import org.camunda.bpm.engine.spring.SpringExpressionManager;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

@Configuration
public class CamundaConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(CamundaConfiguration.class);

    @Autowired
    ApplicationContext applicationContext;

    @Bean
    public ProcessEngineConfigurationImpl processEngineConfiguration() {
        final SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(org.h2.Driver.class);
        dataSource.setUrl("jdbc:h2:mem:camunda-test;DB_CLOSE_DELAY=1000");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        final SpringProcessEngineConfiguration config = new SpringProcessEngineConfiguration();
        config.setDataSource(dataSource);
        config.setDatabaseSchemaUpdate("true");
        config.setExpressionManager(new SpringExpressionManager(applicationContext, null));
        config.setTransactionManager(new DataSourceTransactionManager(dataSource));
        config.setHistory(ProcessEngineConfiguration.HISTORY_FULL);
        config.setJobExecutorActivate(false);
        config.setIdGenerator(new StrongUuidGenerator());
        config.setTelemetryReporterActivate(false);
        config.setDbMetricsReporterActivate(false);

        return config;
    }

    @Bean
    public ProcessEngineFactoryBean processEngineFactoryBean() {
        final ProcessEngineFactoryBean factoryBean = new ProcessEngineFactoryBean();
        factoryBean.setProcessEngineConfiguration(processEngineConfiguration());
        return factoryBean;
    }

    @Bean
    public JavaDelegate callADelegate() {
        return (execution) -> {
            logger.info(">>> executing callADelegate");
        };
    }

    @Bean
    public JavaDelegate compensateADelegate() {
        return (execution) -> {
            logger.info(">>> executing compensateADelegate");
        };
    }

    @Bean
    public JavaDelegate callBDelegate() {
        return (execution) -> {
            logger.info(">>> executing callBDelegate");
        };
    }

    @Bean
    public JavaDelegate compensateBDelegate() {
        return (execution) -> {
            logger.info(">>> executing compensateBDelegate");
        };
    }

    @Bean
    public JavaDelegate sendAuditMsgOkDelegate() {
        return (execution) -> {
            logger.info(">>> executing sendAuditMsgOkDelegate");
            execution.getProcessEngineServices()
                .getRuntimeService()
                .createMessageCorrelation("audit_msg")
                .correlate();
        };
    }

    @Bean
    public JavaDelegate sendAuditMsgKoDelegate() {
        return (execution) -> {
            logger.info(">>> executing sendAuditMsgKoDelegate");
            execution.getProcessEngineServices()
                .getRuntimeService()
                .createMessageCorrelation("audit_msg")
                .correlate();
        };
    }

    @Bean
    public JavaDelegate throwErrorDelegate() {
        return (execution) -> {
            throw new BpmnError("throwErrorDelegate");
        };
    }

    @Bean
    public JavaDelegate auditDelegate() {
        return (execution) -> {
            logger.info(">>> executing auditDelegate");
        };
    }

    @Bean
    public JavaDelegate handleErrorDelegate() {
        return (execution) -> {
            logger.info(">>> executing handleErrorDelegate");
        };
    }
}
