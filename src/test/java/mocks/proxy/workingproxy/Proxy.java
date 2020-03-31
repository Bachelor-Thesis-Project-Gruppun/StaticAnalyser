package mocks.proxy.workingproxy;

import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;

@DesignPattern(pattern = {Pattern.PROXY_PROXY})
public class Proxy implements ProxyInterface {

    private Subject subject;

    public Proxy(Subject subject) {
        this.subject = subject;
    }

    @Override
    public int[] getTextIDs() {
        return subject.getTextIDs();
    }

    @Override
    public String getText(int id) {
        return subject.getText(id);
    }
}
