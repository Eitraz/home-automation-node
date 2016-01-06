package com.eitraz.automation;

import com.eitraz.automation.configuration.AutomationConfiguration;
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
//        Thread.sleep(60000 * 60 * 2);

        while (!RULE.getEnvironment().getApplicationContext().isStopped()) {
            Thread.sleep(1000);
        }

//        System.out.println("================================================================================");
//        System.out.println("Tellstick: " + RULE.getConfiguration().getTellstick());
//        RULE.getConfiguration().getTellstick().getDevices().forEach(System.out::println);
    }
}