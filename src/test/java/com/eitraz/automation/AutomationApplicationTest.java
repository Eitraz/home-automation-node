package com.eitraz.automation;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Test;

public class AutomationApplicationTest {
    @ClassRule
    public static final DropwizardAppRule<AutomationConfiguration> RULE =
            new DropwizardAppRule<>(AutomationApplication.class, ResourceHelpers.resourceFilePath("automation.yaml"));

    @Test
    public void testTest() throws Exception {
        Thread.sleep(10000);
    }
}