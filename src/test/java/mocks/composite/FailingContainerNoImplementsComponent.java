package mocks.composite;

import java.util.ArrayList;
import java.util.List;

import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;

@DesignPattern(pattern = {Pattern.COMPOSITE_CONTAINER})
public class FailingContainerNoImplementsComponent {

    List<Component> children = new ArrayList<>();

    public void bar() {
        children.stream().forEach(Component::bar);
    }

    public void test(int a) {
        for (Component ci : children) {
            ci.test(a);
        }
    }

    public void foo() {
        for (int i = 0; i < children.size(); i++) {
            System.out.println("No delegation");
        }
    }

    public void test(String a) {
        int pntr = 0;
        while (true) {
            children.get(pntr).test(a);
            pntr++;
        }
    }

}
