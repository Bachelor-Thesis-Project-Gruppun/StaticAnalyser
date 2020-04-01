package mocks.composite;

import java.util.ArrayList;
import java.util.List;

import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;

@DesignPattern(pattern = {
    Pattern.COMPOSITE_CONTAINER, Pattern.COMPOSITE_LEAF
})
public class CompositeMock implements CompositeInterface {

    List<CompositeInterface> foo = new ArrayList<>();

}
