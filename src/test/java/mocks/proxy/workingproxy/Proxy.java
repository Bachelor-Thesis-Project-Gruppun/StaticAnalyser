package mocks.proxy.workingproxy;

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
