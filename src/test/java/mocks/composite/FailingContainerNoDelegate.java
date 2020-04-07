package mocks.composite;

import java.util.ArrayList;
import java.util.List;

import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;

public class FailingContainerNoDelegate implements Component {

    List<Component> children = new ArrayList<>();

    @Override
    public void bar() {
        children.stream().forEach(Component::bar);
    }

    @Override
    public void test(int a) {
        for (Component ci : children) {
            ci.test(a);
        }
    }

    @Override
    public void foo() {
        for (int i = 0; i < children.size(); i++) {
            System.out.println("No delegation");
        }
    }

    @Override
    public void test(String a) {
        int pntr = 0;
        while (true) {
            children.get(pntr).test(a);
            pntr++;
        }
    }

}
