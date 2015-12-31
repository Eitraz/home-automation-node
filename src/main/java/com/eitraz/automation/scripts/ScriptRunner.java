package com.eitraz.automation.scripts;

import com.eitraz.automation.tellstick.TellstickAutomation;
import com.eitraz.library.lifecycle.Startable;
import com.eitraz.library.lifecycle.Stopable;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.MapClearedListener;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.apache.log4j.Logger;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

public class ScriptRunner implements Startable, Stopable,
        EntryRemovedListener<String, Script>, EntryAddedListener<String, Script>, MapClearedListener {
    protected static final Logger logger = Logger.getLogger(ScriptRunner.class);

    private final IMap<String, Script> hazelcastScripts;
    private String hazelcastScriptsListenerId;

    private boolean scriptsUpdated = true;
    private String cachedScript;

    public ScriptRunner(HazelcastInstance hazelcast) {
        hazelcastScripts = hazelcast.getMap("automation.scripts");
    }

    private String getScript() {
        if (scriptsUpdated) {
            scriptsUpdated = false;
            cachedScript = buildScript();
        }
        return cachedScript;
    }

    private String buildScript() {
        ArrayList<Script> scripts = new ArrayList<>(hazelcastScripts.values());
        scripts.sort((o1, o2) -> Integer.compare(o2.getPriority(), o1.getPriority()));

        logger.info(String.format("Rebuilding script for %d scripts", scripts.size()));

        StringBuilder builder = new StringBuilder();
        for (Script s : scripts) {
            builder.append(String.format("// Script: %s\n%s\n", s.getName(), s.getScript()));
        }

        String script = builder.toString();

        if (logger.isDebugEnabled()) {
            logger.debug("SCRIPT START >>>");
            logger.debug(script);
            logger.debug("<<< END");
        }

        return script;
    }

    public void runScripts(TellstickAutomation tellstick) {
        String script = getScript();

        // Configuration
        CompilerConfiguration configuration = new CompilerConfiguration();

        // Binding
        Binding binding = new Binding();

        // Output
        StringWriter output = new StringWriter();
        binding.setProperty("out", new PrintWriter(output));

        binding.setVariable("tellstick", tellstick);

        // Evaluate
        GroovyShell shell = new GroovyShell(getClass().getClassLoader(), binding, configuration);
        try {
            shell.evaluate(script);
        } catch (CompilationFailedException e) {
            logger.error(e);
        }

        logger.info(String.format("Script output:\t%s", output.toString()));
    }

    @Override
    public void doStart() {
        hazelcastScriptsListenerId = hazelcastScripts.addEntryListener(this, false);
    }

    @Override
    public void doStop() {
        if (hazelcastScriptsListenerId != null) {
            hazelcastScripts.removeEntryListener(hazelcastScriptsListenerId);
            hazelcastScriptsListenerId = null;
        }
    }

    @Override
    public void entryAdded(EntryEvent<String, Script> event) {
        scriptsUpdated = true;
    }

    @Override
    public void entryRemoved(EntryEvent<String, Script> event) {
        scriptsUpdated = true;
    }

    @Override
    public void mapCleared(MapEvent event) {
        scriptsUpdated = true;
    }
}
