package org.junit.runners.statements;

import org.junit.Repeat;
import org.junit.runners.model.Statement;

import java.lang.reflect.Method;

public class RepeatRun extends Statement {

    private final Statement next;

    private final int repeat;

    public RepeatRun(Statement next, Method testMethod) {
        this.next = next;
        this.repeat = getRepeatCount(testMethod);
    }

    private int getRepeatCount(Method method) {
        Repeat repeat = method.getAnnotation(Repeat.class);
        if (repeat == null) {
            return 1;
        }
        return Math.max(1, repeat.value());
    }

    @Override
    public void evaluate() throws Throwable {
        for (int i = 0; i < this.repeat; i++) {
            this.next.evaluate();
        }
    }
}