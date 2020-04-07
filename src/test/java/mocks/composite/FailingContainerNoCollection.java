package mocks.composite;

import java.util.ArrayList;
import java.util.List;

import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;

public class FailingContainerNoCollection implements Component {

    Component child = new Leaf();
    List<Integer> notChildren = new ArrayList<>();

    @Override
    public void bar() {
        for (Integer i : notChildren) {
            child.bar();
        }
    }

    @Override
    public void test(int a) {
        for (Integer i : notChildren) {
            child.test(a);
        }
    }

    @Override
    public void foo() {
        for (int i = 0; i < notChildren.size(); i++) {
            child.foo();
        }
    }

    @Override
    public void test(String a) {
        int pntr = 0;
        while (true) {
            child.test(a);
            pntr++;
        }
    }

}
