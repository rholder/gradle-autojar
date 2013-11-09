package com.foo;

import org.apache.commons.lang.StringUtils;

public class Foo {
//    public native void sayHi(String who, int times);
//
//    static {
//        System.loadLibrary("HelloImpl");
//    }

    public Foo() {
        System.out.println("This is a simple application that prints this line.");
        System.out.println(StringUtils.capitalize("this is run through StringUtils from commons-lang."));
        
        //sayHi("User, this is from a native lib and should be printed 3 times.", 3);
    }

    public static void main(String args[]) {
        new Foo();
    }
}