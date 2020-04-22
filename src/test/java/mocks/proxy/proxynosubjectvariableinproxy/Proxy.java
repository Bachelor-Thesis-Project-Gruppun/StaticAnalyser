package mocks.proxy.proxynosubjectvariableinproxy;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(justification = "Mock")
public class Proxy implements ProxyInterface {

    public Proxy(Subject theSubject) {

    }

    @Override
    public int[] getTextIDs() {
        return new int[]{0};
    }

    @Override
    public String getText(int id) {
        return "0";
    }
}
