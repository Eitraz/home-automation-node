package com.eitraz.automation.tellstick;

import com.codahale.metrics.health.HealthCheck;

public class TellstickHealthCheck extends HealthCheck {
    private final TellstickAutomation tellstick;

    public TellstickHealthCheck(TellstickAutomation tellstick) {
        this.tellstick = tellstick;
    }

    @Override
    protected Result check() throws Exception {
        return tellstick.getTellstick() != null ? Result.healthy() : Result.unhealthy("Tellstick is null");
    }
}
