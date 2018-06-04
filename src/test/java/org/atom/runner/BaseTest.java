package org.atom.runner;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(MinecraftServerRunner.class)
public class BaseTest {

    @Test
    public void test() {
        System.out.println("It works!");
    }
}
