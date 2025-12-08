package com.example.temporal.demo.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface DeadlockReproduceWorkflow {

    @WorkflowMethod
    String orchestrate();
}