package com.eitraz.automation.evaulation;

import com.eitraz.automation.AutomationApplication;
import com.eitraz.automation.LifeCycleInstance;
import com.eitraz.automation.scripts.ScriptRunner;
import com.eitraz.automation.tellstick.TellstickAutomation;
import com.eitraz.library.lifecycle.Startable;
import com.eitraz.library.lifecycle.Stopable;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.ILock;
import com.hazelcast.core.Member;

import java.io.Serializable;

public class Evaluator implements Startable, Stopable {
    private HazelcastInstance hazelcast;
    private TellstickAutomation tellstick;

    private long lastEvaluation = 0;
    private ILock evaluateLock;

    private IExecutorService evaluateExecutor;

    private Thread baseEvaulateIntervalThread;
    private ILock baseEvaluateIntervalLock;
    private static Evaluator instance;

    private final ScriptRunner scriptRunner;

    public Evaluator(HazelcastInstance hazelcast, TellstickAutomation tellstick) {
        // ... Well
        if (instance != null)
            throw new RuntimeException("A evaluator already exists!");
        instance = this;

        this.hazelcast = hazelcast;
        this.tellstick = tellstick;

        evaluateLock = hazelcast.getLock("evaluate-lock");
        baseEvaluateIntervalLock = hazelcast.getLock("base-evaluate-interval-lock");

        evaluateExecutor = hazelcast.getExecutorService("evaluate-executor");
        hazelcast.getConfig().getExecutorConfig("evaluate-executor").setPoolSize(1);

        scriptRunner = LifeCycleInstance.register(new ScriptRunner(hazelcast, tellstick));
    }

    public TellstickAutomation getTellstick() {
        return tellstick;
    }

    public void requestEvaluation() {
        evaluateExecutor.executeOnMember(new RequestEvaluation(), getMemberWithHighestPriority());
    }

    private void evaluate() {
        evaluateLock.lock();
        try {
            long time = System.currentTimeMillis();
            // Don't overdo it
            if (time - lastEvaluation > 1000) {
                doEvaluation();

                lastEvaluation = time;
            }
        } finally {
            evaluateLock.unlock();
        }
    }

    private void doEvaluation() {
        scriptRunner.runScripts();
    }

    @Override
    public void doStart() {
        baseEvaulateIntervalThread = new Thread("baseEvaulateIntervalThread") {
            @Override
            public void run() {
                long previousTime = System.currentTimeMillis();
                baseEvaluateIntervalLock.lock();
                try {
                    while (baseEvaulateIntervalThread == this) {
                        long time = System.currentTimeMillis();

                        if (time - previousTime > 1000 * 30) {
                            requestEvaluation();
                            previousTime = time;
                        }

                        try {
                            Thread.sleep(2500);
                        } catch (InterruptedException ignored) {
                        }
                    }
                } finally {
                    baseEvaluateIntervalLock.unlock();
                }
            }
        };
        baseEvaulateIntervalThread.start();
    }

    @Override
    public void doStop() {
        Thread previousBaseEvaulateIntervalThread = baseEvaulateIntervalThread;
        baseEvaulateIntervalThread = null;
        try {
            previousBaseEvaulateIntervalThread.join(5000);
        } catch (InterruptedException ignored) {
        }
    }

    private Member getMemberWithHighestPriority() {
        int priority = Integer.MIN_VALUE;
        Member member = null;
        for (Member m : hazelcast.getCluster().getMembers()) {
            Integer p = m.getIntAttribute(AutomationApplication.HAZELCAST_MEMBER_PRIORITY);
            if (p > priority) {
                priority = p;
                member = m;
            }
        }
        return member;
    }

    static class RequestEvaluation implements Runnable, Serializable {
        @Override
        public void run() {
            instance.evaluate();
        }
    }
}
