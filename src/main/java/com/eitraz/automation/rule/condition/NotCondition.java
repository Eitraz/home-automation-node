package com.eitraz.automation.rule.condition;

public class NotCondition implements Condition {
    private Condition condition;

    public NotCondition(Condition condition) {
        this.condition = condition;
    }

    @Override
    public boolean isTrue() {
        return !condition.isTrue();
    }
}