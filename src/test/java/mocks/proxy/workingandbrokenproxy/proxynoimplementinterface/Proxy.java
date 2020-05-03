package mocks.proxy.workingandbrokenproxy.proxynoimplementinterface;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(justification = "Mock")
public class Proxy {

    private Subject theSubject;

    public Proxy(Subject theSubject) {
        this.theSubject = theSubject;
    }

    public int[] getTextIDs() {
        this.theSubject.getText(0);
        return this.theSubject.getTextIDs();
    }

    public String getText(int id) {
        return theSubject.getText(id);
    }
}
