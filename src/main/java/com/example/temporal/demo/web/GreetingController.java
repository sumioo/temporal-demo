package com.example.temporal.demo.web;

import com.example.temporal.demo.common.Constants;
import com.example.temporal.demo.workflow.GreetingWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.RetryOptions;

import java.time.Duration;

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
    RetryOptions retryOptions = RetryOptions.newBuilder()
            .setInitialInterval(Duration.ofSeconds(1)) // 首次重试间隔
            .setBackoffCoefficient(2.0) // 指数退避系数，每次间隔 x2
            .setMaximumInterval(Duration.ofMinutes(1)) // 最大重试间隔
            .setMaximumAttempts(1) // 最多重试 3 次
            .setDoNotRetry(NullPointerException.class.getName()) // 遇到空指针异常不重试
            .build();

    WorkflowOptions options = WorkflowOptions.newBuilder()
            .setTaskQueue(Constants.TASK_QUEUE)
            .setWorkflowId("greeting-" + name)
            .setRetryOptions(retryOptions) // <--- 在这里应用重试策略
            .build();
    // 1. 创建一个与 options 绑定的 Workflow Stub
    GreetingWorkflow workflow = client.newWorkflowStub(GreetingWorkflow.class, options);

    // 2. 使用 WorkflowClient.start() 来“发起并忘记”
    // 这一行会立即向 Temporal Server 发送启动命令，然后马上返回，完全不阻塞。
    WorkflowClient.start(workflow::getGreeting, name);

    // 3. 直接返回，不再需要 @Async 方法
    return ResponseEntity.ok("Workflow started successfully. WorkflowId: " + options.getWorkflowId());
  }
}

