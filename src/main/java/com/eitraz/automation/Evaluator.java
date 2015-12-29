package com.eitraz.automation;

import com.eitraz.automation.tellstick.TellstickAutomation;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;

public class Evaluator {
    private HazelcastInstance hazelcast;
    private TellstickAutomation tellstick;

    private long lastEvaluation = 0;
    private ILock evaluateLock;

    public Evaluator(HazelcastInstance hazelcast, TellstickAutomation tellstick) {
        this.hazelcast = hazelcast;
        this.tellstick = tellstick;
        evaluateLock = hazelcast.getLock("evaluate-lock");
    }

    public TellstickAutomation getTellstick() {
        return tellstick;
    }

    public void evaluate() {
        evaluateLock.lock();
        try {
            long time = System.currentTimeMillis();
            // Don't overdo it
            if (time - lastEvaluation > 1000) {
                System.out.println("GOGOGOGO");

                lastEvaluation = time;
            }
        } finally {
            evaluateLock.unlock();
        }
    }
}
