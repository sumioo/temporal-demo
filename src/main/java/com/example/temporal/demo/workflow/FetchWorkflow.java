package com.example.temporal.demo.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.List;

@WorkflowInterface
public interface FetchWorkflow {
    @WorkflowMethod
    String fetchUrls(List<String> urls);
}