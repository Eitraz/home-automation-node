package com.eitraz.automation.scripts;

import java.io.Serializable;

public class Script implements Serializable {
    private final int priority;
    private final String name;
    private final String script;

    public Script(int priority, String name, String script) {
        this.priority = priority;
        this.name = name;
        this.script = script;
    }

    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }

    public String getScript() {
        return script;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Script script = (Script) o;

        return !(getName() != null ? !getName().equals(script.getName()) : script.getName() != null);

    }

    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }
}
