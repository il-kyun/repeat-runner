package org.junit.runners;

import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.statements.RepeatRun;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class RepeatRunner extends BlockJUnit4ClassRunner {

    private static final Method withRulesMethod;

    static {
        try {
            Method withRules = BlockJUnit4ClassRunner.class.getDeclaredMethod("withRules", FrameworkMethod.class, Object.class, Statement.class);
            if (Modifier.isPrivate(withRules.getModifiers())) {
                withRules.setAccessible(true);
            }
            withRulesMethod = withRules;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("no withRules method: " + e.getMessage());
        } catch (Exception e) {
            throw new UndeclaredThrowableException(e, "UndeclaredThrowableException");
        }
    }

    public RepeatRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected void runChild(FrameworkMethod frameworkMethod, RunNotifier notifier) {
        Description description = describeChild(frameworkMethod);
        if (isIgnored(frameworkMethod)) {
            notifier.fireTestIgnored(description);
        } else {
            Statement statement;
            try {
                statement = methodBlock(frameworkMethod);
            } catch (Throwable ex) {
                statement = new Fail(ex);
            }
            runLeaf(statement, description, notifier);
        }
    }

    @Override
    protected Statement methodBlock(FrameworkMethod method) {
        Object test;
        try {
            test = new ReflectiveCallable() {
                @Override
                protected Object runReflectiveCall() throws Throwable {
                    return createTest();
                }
            }.run();
        } catch (Throwable ex) {
            return new Fail(ex);
        }

        Statement statement = methodInvoker(method, test);
        statement = possiblyExpectingExceptions(method, test, statement);
        statement = withPotentialTimeout(method, test, statement);
        statement = withBefores(method, test, statement);
        statement = withAfters(method, test, statement);
        statement = withRulesReflectively(method, test, statement);
        statement = withPotentialRepeat(method, statement);
        return statement;
    }

    private Statement withRulesReflectively(FrameworkMethod frameworkMethod, Object testInstance, Statement statement) {

        Object result;
        try {
            result = withRulesMethod.invoke(this, frameworkMethod, testInstance, statement);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("can not access to method: " + e.getMessage());
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("can not invoke method: " + e.getMessage());
        } catch (Exception e) {
            throw new UndeclaredThrowableException(e, "UndeclaredThrowableException");
        }

        if (result instanceof Statement) {
            return (Statement) result;
        }
        throw new IllegalStateException("withRules method not found");
    }

    protected Statement withPotentialRepeat(FrameworkMethod frameworkMethod, Statement statement) {
        return new RepeatRun(statement, frameworkMethod.getMethod());
    }

    private static class UndeclaredThrowableException extends RuntimeException {
        public UndeclaredThrowableException(Throwable undeclaredThrowable, String message) {
            super(message, undeclaredThrowable);
        }
    }
}