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
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovyShell;
import org.apache.log4j.Logger;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ScriptRunner implements Startable, Stopable,
        EntryRemovedListener<String, Script>, EntryAddedListener<String, Script>, MapClearedListener {
    protected static final Logger logger = Logger.getLogger(ScriptRunner.class);

    private TellstickAutomation tellstick;

    private final IMap<String, Script> hazelcastScripts;
    private String hazelcastScriptsListenerId;

    private boolean scriptsUpdated = true;
    private String cachedScript;

    private LocalCache localCache = new LocalCache();

    public ScriptRunner(HazelcastInstance hazelcast, TellstickAutomation tellstick) {
        hazelcastScripts = hazelcast.getMap("automation.scripts");
        this.tellstick = tellstick;
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

        Set<String> imports = new HashSet<>();

        StringBuilder builder = new StringBuilder();
        for (Script s : scripts) {
            StringBuilder scriptBuilder = new StringBuilder();

            BufferedReader reader = new BufferedReader(new StringReader(s.getScript()));
            String line;
            try {
                int emptyLineCounter = 0;
                while ((line = reader.readLine()) != null) {
                    // Skip
                    if (line.startsWith("package")) {
                        emptyLineCounter++;
                    }
                    // Import
                    else if (line.startsWith("import")) {
                        emptyLineCounter++;
                        imports.add(line);
                    }
                    // Empty line
                    else if (line.isEmpty()) {
                        if (emptyLineCounter++ == 0)
                            scriptBuilder.append("\n");
                    }
                    // Script line
                    else {
                        emptyLineCounter = 0;
                        scriptBuilder.append(line).append("\n");
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Unable to build script", e);
            }

            builder.append(String.format("// --------------------\n// Script: %s\n// --------------------\n%s\n\n", s.getName(), scriptBuilder.toString().trim()));
        }

        StringBuilder importsStringBuilder = new StringBuilder();
        imports.forEach(l -> importsStringBuilder.append(l).append("\n"));
        importsStringBuilder.append("\n");

        String script = importsStringBuilder.append(builder.toString()).toString();

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Script:\n%s", script));
        }

        return script;
    }

    public void runScripts() {
        String script = getScript();

        // Configuration
        CompilerConfiguration configuration = new CompilerConfiguration();

        // Binding
        Binding binding = new Binding();

        // Output
        StringWriter output = new StringWriter();
        binding.setProperty("out", new PrintWriter(output));

        binding.setVariable("tellstick", tellstick);
        binding.setVariable("cache", localCache);

        // Evaluate
        GroovyShell shell = new GroovyShell(getClass().getClassLoader(), binding, configuration);
        try {
            shell.evaluate(script);
        } catch (Throwable e) {
            logger.error("Script exception", e);
        }

        logger.info(String.format("Script output:\n%s", output.toString()));
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
