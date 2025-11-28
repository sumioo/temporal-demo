package com.example.temporal.demo.config;

import com.example.temporal.demo.activities.GreetingActivitiesImpl;
import com.example.temporal.demo.common.Constants;
import com.example.temporal.demo.workflow.GreetingWorkflowImpl;
import com.example.temporal.demo.workflow.MainGreetingWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerOptions;
import io.temporal.worker.WorkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemporalConfig {

  @Bean
  public WorkerFactory workerFactory(WorkflowClient client) {
    return WorkerFactory.newInstance(client);
  }

  @Bean
  public Worker worker(WorkerFactory factory, @Autowired GreetingActivitiesImpl activities) {
    // 创建 WorkerOptions 来配置并发数
    WorkerOptions options = WorkerOptions.newBuilder()
        // 设置此 Worker 最多同时执行 5 个 Activity 任务
        .setMaxConcurrentActivityExecutionSize(5)
        // 设置此 Worker 最多同时处理 10 个 Workflow 决策任务
        .setMaxConcurrentWorkflowTaskExecutionSize(10)
        .build();

    Worker worker = factory.newWorker(Constants.TASK_QUEUE, options);
    worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class, MainGreetingWorkflowImpl.class);
    worker.registerActivitiesImplementations(activities);
    return worker;
  }

  @Bean
  public ApplicationRunner startTemporalWorkers(WorkerFactory factory) {
    return args -> factory.start();
  }
}
