package tool.designpatterns.verifiers.multiclassverifiers.proxy.helpers;

import com.github.javaparser.ast.body.VariableDeclarator;

/**
 * Simple Tuple2 that groups an InterfaceSubjectProxyTuple with the proxy classes subject variable.
 */
public class InterfaceSubjectProxyVariableTuple {

    private final InterfaceSubjectProxyTuple interfaceSubjectProxy;
    private final VariableDeclarator proxyVarible;

    public InterfaceSubjectProxyVariableTuple(
        InterfaceSubjectProxyTuple interfaceSubjectProxy, VariableDeclarator proxyVarible) {
        this.interfaceSubjectProxy = interfaceSubjectProxy;
        this.proxyVarible = proxyVarible;
    }

    public InterfaceSubjectProxyTuple getInterfaceSubjectProxy() {
        return interfaceSubjectProxy;
    }

    public VariableDeclarator getProxyVarible() {
        return proxyVarible;
    }
}

