package org.junit.runners.statements;

import org.junit.runners.model.Statement;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FailOnTimeoutAndTimeUnit extends Statement {

    private final Statement next;

    private final long timeout;
    private final TimeUnit timeUnit;


    public FailOnTimeoutAndTimeUnit(Statement next, long timeout, TimeUnit timeUnit) {
        if (next == null) {
            throw new IllegalArgumentException("next statement can not be null.");
        }

        if (timeout < 0) {
            throw new IllegalArgumentException("timeout must be non-negative.");
        }

        if (timeUnit == null) {
            throw new IllegalArgumentException("timeUnit can not be null.");
        }

        this.next = next;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    public FailOnTimeoutAndTimeUnit(Statement next, long timeout) {
        this(next, timeout, TimeUnit.MILLISECONDS);
    }


    @Override
    public void evaluate() throws Throwable {
        if (this.timeout == 0) {
            this.next.evaluate();
        } else {
            long startTime = System.currentTimeMillis();
            this.next.evaluate();
            long elapsed = System.currentTimeMillis() - startTime;

            if (elapsed > timeUnit.toMillis(this.timeout)) {
                throw new TimeoutException(String.format("Test took %s %s; limit was %s ms.", elapsed, this.timeUnit.toString(), this.timeout));
            }
        }
    }

}
