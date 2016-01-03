package com.eitraz.automation.rule;

import com.eitraz.automation.rule.condition.Condition;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static com.eitraz.automation.rule.When.not;
import static com.eitraz.automation.rule.When.when;
import static org.junit.Assert.*;

public class WhenTest {
    private static final Condition TRUE = () -> true;
    private static final Condition FALSE = () -> false;

    private static final Runnable nothing = () -> {
    };

    @Test
    public void testRule() {
        when(TRUE).and(FALSE)
                .or(TRUE).and(not(FALSE))
                .then(() -> when(FALSE)
                        .then(() -> System.out.println("Then 1"))
                        .otherwise(() -> System.out.println("Then 2"))
                        .run()
                )
                .otherwise(() -> System.out.println("Otherwise"))
                .run();

        System.out.println("==========================");

        when(FALSE)
                .then(() -> System.out.println("Then"))
                .otherwise()
                .when(FALSE)
                .then(() -> System.out.println("Then 2"))
                .otherwise(() -> System.out.println("Otherwise"))
                .run();

        System.out.println("==========================");
        when(TRUE).then(() -> System.out.println("Then again")).run();
    }

    @Test
    public void testWhen() throws Exception {
        final AtomicInteger counter = new AtomicInteger();

        assertTrue(when(TRUE).then(counter::incrementAndGet).run());
        assertEquals(1, counter.get());

        assertFalse(when(FALSE).then(counter::incrementAndGet).run());
        assertEquals(1, counter.get());
    }

    @Test
    public void testAnd() throws Exception {
        final AtomicInteger counter = new AtomicInteger();

        assertTrue(when(TRUE).and(TRUE).then(counter::incrementAndGet).run());
        assertEquals(1, counter.get());

        assertFalse(when(TRUE).and(FALSE).then(counter::incrementAndGet).run());
        assertEquals(1, counter.get());

        assertFalse(when(FALSE).and(TRUE).then(counter::incrementAndGet).run());
        assertEquals(1, counter.get());
    }

    @Test
    public void testOr() throws Exception {
        final AtomicInteger counter = new AtomicInteger();

        assertTrue(when(TRUE).or(TRUE).then(counter::incrementAndGet).run());
        assertEquals(1, counter.get());

        assertTrue(when(FALSE).or(TRUE).then(counter::incrementAndGet).run());
        assertEquals(2, counter.get());

        assertTrue(when(TRUE).or(FALSE).then(counter::incrementAndGet).run());
        assertEquals(3, counter.get());

        assertFalse(when(FALSE).and(FALSE).then(counter::incrementAndGet).run());
        assertEquals(3, counter.get());
    }

    @Test
    public void testNot() throws Exception {
        assertTrue(not(FALSE).isTrue());
        assertFalse(not(TRUE).isTrue());
    }

    @Test
    public void testOtherwise() throws Exception {
        final AtomicInteger counter = new AtomicInteger();

        assertFalse(when(FALSE).then(nothing).otherwise(counter::incrementAndGet).run());
        assertEquals(1, counter.get());

        assertTrue(when(TRUE).then(nothing).otherwise(counter::incrementAndGet).run());
        assertEquals(1, counter.get());
    }

    @Test
    public void testOtherwiseChain() throws Exception {
        final AtomicInteger counter = new AtomicInteger();

        assertTrue(when(FALSE).then(nothing).otherwise().when(TRUE).then(counter::incrementAndGet).run());
        assertEquals(1, counter.get());

        assertFalse(when(FALSE).then(nothing).otherwise()
                .when(FALSE).then(counter::incrementAndGet).run());
        assertEquals(1, counter.get());

        assertFalse(when(FALSE).then(nothing).otherwise()
                .when(FALSE).then(nothing).otherwise(counter::incrementAndGet).run());
        assertEquals(2, counter.get());
    }
}