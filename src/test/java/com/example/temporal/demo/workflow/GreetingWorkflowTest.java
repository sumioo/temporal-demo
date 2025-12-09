package com.example.temporal.demo.workflow;

import com.example.temporal.demo.activities.GreetingActivitiesImpl;
import com.example.temporal.demo.common.Constants;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class GreetingWorkflowTest {

  private static TestWorkflowEnvironment testEnv;
  private static WorkflowClient client;

  @BeforeAll
  static void setup() {
    testEnv = TestWorkflowEnvironment.newInstance();
    Worker worker = testEnv.newWorker(Constants.TASK_QUEUE);
    worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);
    worker.registerActivitiesImplementations(new GreetingActivitiesImpl());
    testEnv.start();
    client = testEnv.getWorkflowClient();
  }

  @AfterAll
  static void tearDown() {
    testEnv.close();
  }

  @Test
  void greetingWorkflowReturnsHelloName() {
    GreetingWorkflow workflow =
        client.newWorkflowStub(
            GreetingWorkflow.class,
            WorkflowOptions.newBuilder().setTaskQueue(Constants.TASK_QUEUE).build());
    String result = workflow.getGreeting("Leon");
    Assertions.assertEquals("Hello, Leon!", result);
  }
}

