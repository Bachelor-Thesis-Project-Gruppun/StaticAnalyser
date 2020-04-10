package mocks.decorator.correctpattern;

import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;

@DesignPattern(pattern = {Pattern.DECORATOR_INTERFACE_COMPONENT})
public interface IBeverageComponent {

    int getCost();
}
