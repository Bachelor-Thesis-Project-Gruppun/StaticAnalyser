package mocks.proxy.duplicateworkingproxies.workingproxy2;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(justification = "Mock")
public class Proxy implements ProxyInterface {

    private Subject theSubject;

    public Proxy(Subject theSubject) {
        this.theSubject = theSubject;
    }

    @Override
    public int[] getTextIDs() {
        this.theSubject.getText(0);
        return this.theSubject.getTextIDs();
    }

    @Override
    public String getText(int id) {
        return theSubject.getText(id);
    }
}
