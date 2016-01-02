package com.eitraz.automation.scripts;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LocalCacheTest {
    @Test
    public void testGet() throws Exception {
        LocalCache cache = new LocalCache();
        assertEquals("myValue", cache.get("myKey", () -> "myValue"));
        assertEquals("myValue", cache.get("myKey"));
    }
}