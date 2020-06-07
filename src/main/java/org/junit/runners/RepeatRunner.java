package org.junit.runners;

import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.statements.RepeatRun;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class RepeatRunner extends BlockJUnit4ClassRunner {

    public RepeatRunner(Class<?> klass) throws InitializationError {
        super(klass);
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
        Method withRules;
        try {
            withRules = this.getClass().getSuperclass().getDeclaredMethod("withRules", FrameworkMethod.class, Object.class, Statement.class);
            withRules.setAccessible(true);

        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("no withRules method.");
        }

        Statement result;
        try {
            result = (Statement) withRules.invoke(this, frameworkMethod, testInstance, statement);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("can not access to method.");
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("can not invoke method.");
        }

        return result;
    }

    protected Statement withPotentialRepeat(FrameworkMethod frameworkMethod, Statement statement) {
        return new RepeatRun(statement, frameworkMethod.getMethod());
    }
}