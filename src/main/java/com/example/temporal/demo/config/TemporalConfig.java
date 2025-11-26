package com.example.temporal.demo.config;

import com.example.temporal.demo.activities.GreetingActivitiesImpl;
import com.example.temporal.demo.common.Constants;
import com.example.temporal.demo.workflow.GreetingWorkflowImpl;
import com.example.temporal.demo.workflow.MainGreetingWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemporalConfig {

  @Bean
  public WorkflowServiceStubs workflowServiceStubs() {
    return WorkflowServiceStubs.newInstance();
  }

  @Bean
  public WorkflowClientOptions workflowClientOptions() {
    return WorkflowClientOptions.newBuilder().setNamespace(Constants.NAMESPACE).build();
  }

  @Bean
  public WorkflowClient workflowClient(
      WorkflowServiceStubs workflowServiceStubs, WorkflowClientOptions options) {
    return WorkflowClient.newInstance(workflowServiceStubs, options);
  }

  @Bean
  public WorkerFactory workerFactory(WorkflowClient client) {
    return WorkerFactory.newInstance(client);
  }

  @Bean
  public Worker worker(WorkerFactory factory, @Autowired GreetingActivitiesImpl activities) {
    Worker worker = factory.newWorker(Constants.TASK_QUEUE);
    worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class, MainGreetingWorkflowImpl.class);
    worker.registerActivitiesImplementations(activities);
    return worker;
  }

  @Bean
  public ApplicationRunner startTemporalWorkers(WorkerFactory factory) {
    return args -> factory.start();
  }
}

