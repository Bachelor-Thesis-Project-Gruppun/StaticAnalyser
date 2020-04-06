package mocks.composite;

import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;

@DesignPattern(pattern = {
    Pattern.COMPOSITE_COMPONENT
})
public interface Component {

    void test(int a);

    void test(String a);

    void foo();

    void bar();
}
