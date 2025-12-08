package com.example.temporal.demo.workflow;

import com.example.temporal.demo.common.Constants;

import io.temporal.api.enums.v1.ParentClosePolicy;
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

        // This is the "fan-in" part.
        // We wait for all the child workflows to complete and collect their results.
        StringBuilder sb = new StringBuilder();
        for (Promise<String> promise : childPromises) {
            // promise.get() will block until the child workflow completes.
            try {
                sb.append(promise.get());
            } catch (Exception e) {
                System.err.println("Error fetching URLs: " + e.getMessage());
            }
        }
        return sb.toString();
    }
}