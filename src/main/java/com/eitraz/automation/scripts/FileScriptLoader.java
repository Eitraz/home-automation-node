package com.eitraz.automation.scripts;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileScriptLoader extends ScriptLoader {
    private static final Pattern PRIORITY_PATTERN = Pattern.compile("(\\d+)\\.(.*)\\.groovy");

    private File directory;

    public FileScriptLoader(File directory) {
        this.directory = directory;
    }

    @Override
    protected List<Script> getScripts() {
        logger.info(String.format("Loading scripts from '%s'", directory.getAbsolutePath()));

        File[] files = directory.listFiles((dir, name) -> {
            return name.toLowerCase().endsWith(".groovy");
        });

        List<Script> scripts = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                int priority;
                String name;

                Matcher matcher = PRIORITY_PATTERN.matcher(file.getName());
                if (matcher.matches()) {
                    priority = Integer.parseInt(matcher.group(1));
                    name = matcher.group(2);
                }
                // Invalid filename
                else {
                    throw new RuntimeException("Invalid script file name, should be of style priority.name.groovy (for example 10.example.groovy)");
                }

                try {
                    scripts.add(new Script(priority, name, FileUtils.readFileToString(file)));
                } catch (IOException e) {
                    throw new RuntimeException("Unable to load script " + file.getAbsolutePath());
                }
            }
        }

        return scripts;
    }
}
