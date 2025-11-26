package com.example.temporal.demo.workflow;

import com.example.temporal.demo.common.Constants;
import com.example.temporal.demo.workflow.GreetingWorkflow;

import io.temporal.workflow.Async;
import io.temporal.workflow.ChildWorkflowOptions;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;

import java.util.ArrayList;
import java.util.List;

public class MainGreetingWorkflowImpl implements MainGreetingWorkflow {

    @Override
    public String orchestrate(String name) {
        List<Promise<String>> childPromises = new ArrayList<>(5);

        // 1. 一次性启动 5 个子工作流（完全并发）
        for (int i = 0; i < 5; i++) {
            GreetingWorkflow child = Workflow.newChildWorkflowStub(
                    GreetingWorkflow.class,
                    ChildWorkflowOptions.newBuilder()
                            .setWorkflowId("child-" + name + "-" + i)
                            .setTaskQueue(Constants.TASK_QUEUE)
                            .build());
            childPromises.add(Async.function(child::getGreeting, name));
        }

        // 2. 实时感知：任意一个完成就处理（这里简单拼字符串）
        StringBuilder sb = new StringBuilder();
        while (!childPromises.isEmpty()) {
            int doneIndex = -1;
            for (int i = 0; i < childPromises.size(); i++) {
                if (childPromises.get(i).isCompleted()) { // 关键 API：isCompleted()
                    doneIndex = i;
                    break;
                }
            }
            if (doneIndex == -1) { // 理论上不会，保险
                doneIndex = 0;
            }
            String result = childPromises.get(doneIndex).get();
            sb.append(result).append(System.lineSeparator());
            System.out.println("【Main】子工作流" + doneIndex + "已完成，结果 = " + result);
            childPromises.remove(doneIndex); // 按索引删，一定成功
            System.out.println("remaining = " + childPromises.size());
        }

        return "Main done, all child results:" + System.lineSeparator() + sb;
    }
}