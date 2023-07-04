package com.yolt.creditscoring.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfiguration {

    public static final String CREDIT_SCORE_EXECUTOR = "creditScoreExecutor";

    @Value("${yolt.creditScoreExecutor.async:true}")
    private boolean asyncEnabled;

    @Bean(CREDIT_SCORE_EXECUTOR)
    public TaskExecutor batchExecutor(TaskExecutorBuilder builder) {
        return !asyncEnabled ? new SyncTaskExecutor() : builder
                .corePoolSize(3)
                .maxPoolSize(10)
                .queueCapacity(0)
                .threadNamePrefix(CREDIT_SCORE_EXECUTOR + "-")
                .build();
    }

}
