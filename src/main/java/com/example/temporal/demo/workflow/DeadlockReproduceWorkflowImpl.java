package com.example.temporal.demo.workflow;

public class DeadlockReproduceWorkflowImpl implements DeadlockReproduceWorkflow{
    
    @Override
    public String orchestrate() {
        System.out.println("【DeadlockReproduceWorkflowImpl】orchestrate start");
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 2000) {
            // 什么都不做，但也不yield
            // 这会触发 PotentialDeadlockException
        }
        return "";
    }

}
