package com.eitraz.automation.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;

public class ScriptsConfiguration {
    private File path;

    @JsonProperty
    public File getPath() {
        return path;
    }

    @JsonProperty
    public void setPath(File path) {
        this.path = path;
    }
}
