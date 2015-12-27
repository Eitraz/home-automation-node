package com.eitraz.automation;

import org.junit.Test;

public class AutomationTest {
    @Test
    public void testAutomation() throws Exception {
        Automation automation = new Automation();

        automation.doStart();

        Thread.sleep(120000);

        automation.doStop();

    }
}