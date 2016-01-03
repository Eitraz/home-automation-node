package com.eitraz.automation.rule;

import com.eitraz.automation.rule.condition.Condition;
import com.eitraz.automation.rule.condition.NotCondition;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class When implements Condition {
    private When previous;

    private Runnable then;
    private Runnable otherwise;

    private List<List<Condition>> or = new ArrayList<>();
    private List<Condition> conditions;

    private When() {
        conditions = new ArrayList<>();
        or.add(conditions);
    }

    /**
     * Entry point
     *
     * @param condition condition
     * @return When
     */
    public static When when(Condition condition) {
        return new When().and(condition);
    }

    /**
     * @param condition condition
     * @return When
     */
    public When and(Condition condition) {
        // Add condition to condition list
        conditions.add(condition);
        return this;
    }

    /**
     * @param condition condition
     * @return When
     */
    public When or(Condition condition) {
        // Create a new condition list
        conditions = new ArrayList<>();
        or.add(conditions);

        // Add condition
        conditions.add(condition);
        return this;
    }

    /**
     * @param condition condition
     * @return negative value of provided condition
     */
    public static Condition not(Condition condition) {
        return new NotCondition(condition);
    }

    /**
     * @param runnable runnable to be run if when evaluates to true
     * @return Then
     */
    public Then then(Runnable runnable) {
        this.then = runnable;
        return new Then(this);
    }

    @Override
    public boolean isTrue() {
        boolean isTrue = false;

        conditions:
        for (List<Condition> conditions : or) {
            for (Condition condition : conditions) {
                // Continue outer loop if false
                if (!condition.isTrue())
                    continue conditions;
            }

            // All conditions validated was true
            isTrue = true;
            break;
        }

        return isTrue;
    }

    /**
     * Then
     */
    public class Then {
        private When when;

        private Then(When when) {
            this.when = when;
        }

        /**
         * @param runnable runnable to be run if when evalutes to false
         * @return executable
         */
        public Executable otherwise(Runnable runnable) {
            when.otherwise = runnable;
            return new Executable(when);
        }

        /**
         * @return otherwise chain
         */
        public Otherwise otherwise() {
            return new Otherwise(when);
        }

        /**
         * Execute when statement
         *
         * @return true if when validated to true, false otherwise
         */
        public boolean run() {
            return new Executable(when).run();
        }
    }

    /**
     * Otherwise
     */
    public class Otherwise {
        private When when;

        private Otherwise(When when) {
            this.when = when;
        }

        public When when(Condition condition) {
            When when = When.when(condition);
            when.previous = this.when;
            return when;
        }
    }

    /**
     * Executable
     */
    public class Executable {
        private When when;

        private Executable(When when) {
            this.when = when;
        }

        /**
         * Execute when statement
         *
         * @return true if when validated to true, false otherwise
         */
        public boolean run() {
            Deque<When> stack = new ArrayDeque<>();

            // Add all 'otherwise' to stack
            When when = this.when;
            do {
                stack.push(when);
                when = when.previous;
            } while (when != null);

            return run(stack);
        }

        /**
         * @param stack 'otherwise' stack
         * @return true if when validated to true, false otherwise
         */
        private Boolean run(Deque<When> stack) {
            When when = stack.pop();

            boolean isTrue = when.isTrue();

            // True
            if (isTrue) {
                when.then.run();
            }
            // False and otherwise is set
            else if (when.otherwise != null) {
                when.otherwise.run();
            }
            // False and there is chained 'otherwise'
            else if (!stack.isEmpty()) {
                return run(stack);
            }

            return isTrue;
        }
    }
}