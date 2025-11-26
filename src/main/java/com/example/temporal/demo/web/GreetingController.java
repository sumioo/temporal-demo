package com.example.temporal.demo.web;

import com.example.temporal.demo.common.Constants;
import com.example.temporal.demo.workflow.GreetingWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class GreetingController {
  private final WorkflowClient client;

  public GreetingController(WorkflowClient client) {
    this.client = client;
  }

  @PostMapping("/greet/{name}")
  public ResponseEntity<String> greet(@PathVariable String name) {
    GreetingWorkflow workflow =
        client.newWorkflowStub(
            GreetingWorkflow.class,
            WorkflowOptions.newBuilder()
                .setTaskQueue(Constants.TASK_QUEUE)
                .setWorkflowId("greet-" + name)
                .build());

    String result = workflow.getGreeting(name);
    return ResponseEntity.ok(result);
  }
}

