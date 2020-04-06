package mocks.composite;

import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;

@DesignPattern(pattern = {Pattern.COMPOSITE_LEAF})
public class Leaf implements Component {

    @Override
    public void test(int a) {
        System.out.println(a);
    }

    @Override
    public void test(String a) {
        System.out.println(a);
    }

    @Override
    public void foo() {
        System.out.println("foo");
    }

    @Override
    public void bar() {
        System.out.println("bar");
    }

}
