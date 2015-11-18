package com.eitraz.automation;

import com.eitraz.library.lifecycle.LifecycleHandler;

public final class LifeCycleInstance {
    private static LifecycleHandler lifecycleHandler;

    private LifeCycleInstance() {
    }

    public static LifecycleHandler get() {
        if (lifecycleHandler == null) {
            lifecycleHandler = new LifecycleHandler();
        }
        return lifecycleHandler;
    }

    public static <T> T register(T object) {
        return get().register(object);
    }
}
