package org.junit.runners;

import org.junit.Repeat;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(RepeatRunner.class) // 커스텀 Runner 를 지정한다.
public class MyTestClass {

    @Test
    @Repeat(10) // 반복 횟수를 지정한다..
    public void testMyCode10Times() {
        //your test code goes here
        System.out.println("testMyCode10Times::your test code goes here");
    }

    @Test
    @Repeat(5) // 반복 횟수를 지정한다..
    public void testMyCode5Times() {
        //your test code goes here
        System.out.println("testMyCode5Times::your test code goes here");
    }
}