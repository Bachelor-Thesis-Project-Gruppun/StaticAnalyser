package mocks.proxy.workingproxy;

import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;

@DesignPattern(pattern = {Pattern.PROXY_INTERFACE})
public interface ProxyInterface {

    int[] getTextIDs();

    String getText(int id);
}
