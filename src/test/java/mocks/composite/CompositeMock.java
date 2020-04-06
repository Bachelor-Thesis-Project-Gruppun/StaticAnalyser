package mocks.composite;

import java.util.ArrayList;
import java.util.List;

import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;

public class CompositeMock implements CompositeInterface {

    List<CompositeInterface> foo = new ArrayList<>();

    @Override
    public void bar() {
        foo.stream().forEach(CompositeInterface::bar);

    }

    @Override
    public void test(int a) {
        for (CompositeInterface ci : foo) {
            ci.test(a);
        }
    }

    @Override
    public void test(String a) {
        int pntr = 0;
        while (true) {
            foo.get(pntr).test(a);
            pntr++;
        }
    }
}
