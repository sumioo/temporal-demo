package com.example.temporal.demo.workflow;

import com.example.temporal.demo.common.Constants;
import io.temporal.workflow.Async;
import io.temporal.workflow.ChildWorkflowOptions;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;

import java.util.ArrayList;
import java.util.List;

public class BatchFetchWorkflowImpl implements BatchFetchWorkflow {

    @Override
    public String orchestrate(List<List<String>> urlBatches) {
        List<Promise<String>> childPromises = new ArrayList<>();

        for (int i = 0; i < urlBatches.size(); i++) {
            FetchWorkflow child = Workflow.newChildWorkflowStub(
                    FetchWorkflow.class,
                    ChildWorkflowOptions.newBuilder()
                            .setWorkflowId("child-fetch-" + i)
                            .setTaskQueue(Constants.TASK_QUEUE)
                            .build());
            childPromises.add(Async.function(child::fetchUrls, urlBatches.get(i)));
        }

        StringBuilder sb = new StringBuilder();
        while (!childPromises.isEmpty()) {
            Workflow.await(() -> childPromises.stream().anyMatch(Promise::isCompleted));

            int doneIndex = -1;
            for (int i = 0; i < childPromises.size(); i++) {
                if (childPromises.get(i).isCompleted()) {
                    doneIndex = i;
                    break;
                }
            }

            if (doneIndex != -1) {
                Promise<String> completedPromise = childPromises.get(doneIndex);
                try {
                    String result = completedPromise.get();
                    sb.append("Child workflow result: ").append(result).append(System.lineSeparator());
                } catch (Exception e) {
                    sb.append("Child workflow failed: ").append(e.getMessage()).append(System.lineSeparator());
                } finally {
                    childPromises.remove(doneIndex);
                }
            }
        }
        return sb.toString();
    }
}