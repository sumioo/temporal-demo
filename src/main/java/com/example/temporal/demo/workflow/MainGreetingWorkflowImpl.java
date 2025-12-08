package com.example.temporal.demo.workflow;

import com.example.temporal.demo.common.Constants;

import io.temporal.api.enums.v1.ParentClosePolicy;
import io.temporal.workflow.Async;
import io.temporal.workflow.ChildWorkflowOptions;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;

import java.util.ArrayList;
import java.util.List;

public class MainGreetingWorkflowImpl implements MainGreetingWorkflow {
    private static final org.slf4j.Logger log = Workflow.getLogger(MainGreetingWorkflowImpl.class);

    @Override
    public String orchestrate(String name, int childCount) {
        List<Promise<String>> childPromises = new ArrayList<>(5);

        // 1. 一次性启动 5 个子工作流（完全并发）
        for (int i = 0; i < childCount; i++) {
            GreetingWorkflow child = Workflow.newChildWorkflowStub(
                    GreetingWorkflow.class,
                    ChildWorkflowOptions.newBuilder()
                            .setWorkflowId("child-" + name + "-" + i)
                            .setTaskQueue(Constants.TASK_QUEUE)
                            .setParentClosePolicy(ParentClosePolicy.PARENT_CLOSE_POLICY_TERMINATE)
                            .build());
            childPromises.add(Async.function(child::getGreeting, name));
        }

        // 2. 实时感知：任意一个完成就处理（这里简单拼字符串）
        StringBuilder sb = new StringBuilder();
        // try {
            // Promise.allOf(childPromises).get(); // 子工作流错误会被抛出错误
        // } catch (Exception e) {
        //     log.error("=============== waitting all =====================: " + e.getClass());
        // }
        for (Promise<String> promise : childPromises) {
            // promise.get() will block until the child workflow completes.
            try {
                sb.append(promise.get());
            } catch (Exception e) {
                log.error("=============== waitting child ======================: " + e.getClass() + e.getMessage());
            }
        }

        return "Main done, all child results:" + System.lineSeparator() + sb;
    }
}