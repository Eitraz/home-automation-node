package com.eitraz.automation.scripts;

import com.hazelcast.core.HazelcastInstance;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

public abstract class ScriptLoader {
    protected static final Logger logger = Logger.getLogger(ScriptLoader.class);

    public void populateHazelcast(HazelcastInstance hazelcast) {
        Map<String, Script> hazelcastScripts = hazelcast.getMap("automation.scripts");
        getScripts().forEach(s -> hazelcastScripts.put(s.getName(), s));

        if (hazelcastScripts.isEmpty()) {
            logger.info("No scripts active!");
        } else {
            logger.info("Active scripts:");
            hazelcastScripts.values().forEach(s -> logger.info(String.format("\t%s, priority: %d%n", s.getName(), s.getPriority())));
        }
    }

    protected abstract List<Script> getScripts();
}
