package tool.designpatterns.verifiers.multiclassverifiers.proxy;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.gradle.internal.impldep.org.junit.Assert.assertFalse;
import static org.gradle.internal.impldep.org.junit.Assert.assertTrue;
import static tool.designpatterns.Pattern.*;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import org.junit.jupiter.api.Test;
import tool.designpatterns.Pattern;
import utilities.TestHelper;

public class ProxyVerifierTest {

    @Test
    void testVerifyGroup() throws FileNotFoundException {
        Map<Pattern, List<ClassOrInterfaceDeclaration>> patternGroup = new HashMap<>();

        ClassOrInterfaceDeclaration proxy = TestHelper.getMockClassOrI(
            "proxy/workingproxy", "Proxy");
        ClassOrInterfaceDeclaration subject = TestHelper.getMockClassOrI(
            "proxy/workingproxy", "Subject");
        ClassOrInterfaceDeclaration proxyI = TestHelper.getMockClassOrI(
            "proxy/workingproxy", "ProxyInterface");

        List<ClassOrInterfaceDeclaration> proxyList = new ArrayList<ClassOrInterfaceDeclaration>();
        proxyList.add(proxy);

        List<ClassOrInterfaceDeclaration> subjectList = new ArrayList<ClassOrInterfaceDeclaration>();
        subjectList.add(subject);

        List<ClassOrInterfaceDeclaration> proxyIList = new ArrayList<ClassOrInterfaceDeclaration>();
        proxyIList.add(proxyI);

        patternGroup.put(PROXY_PROXY, proxyList);
        patternGroup.put(PROXY_SUBJECT, subjectList);
        patternGroup.put(PROXY_INTERFACE, proxyIList);

        assertFalse(new ProxyVerifier().verifyGroup(patternGroup).hasError());
    }

    @Test
    void testGetValidMethods() throws FileNotFoundException {
        Map<Pattern, List<ClassOrInterfaceDeclaration>> patternGroup = new HashMap<>();

        ClassOrInterfaceDeclaration proxy = TestHelper.getMockClassOrI(
            "proxy/workingproxy", "Proxy");
        ClassOrInterfaceDeclaration subject = TestHelper.getMockClassOrI(
            "proxy/workingproxy", "Subject");
        ClassOrInterfaceDeclaration proxyI = TestHelper.getMockClassOrI(
            "proxy/workingproxy", "ProxyInterface");


        // Missing an entry of proxy interface in map
        assertTrue(new ProxyVerifier().verifyGroup(patternGroup).hasError());
        List<ClassOrInterfaceDeclaration> proxyIList = new ArrayList<ClassOrInterfaceDeclaration>();
        patternGroup.put(PROXY_INTERFACE, proxyIList);
        proxyIList.add(proxyI);

        // Missing an entry of proxy subject in map
        assertTrue(new ProxyVerifier().verifyGroup(patternGroup).hasError());
        List<ClassOrInterfaceDeclaration> subjectList = new ArrayList<ClassOrInterfaceDeclaration>();
        patternGroup.put(PROXY_SUBJECT, subjectList);
        subjectList.add(subject);

        // Missing an entry of proxy proxy in map
        assertTrue(new ProxyVerifier().verifyGroup(patternGroup).hasError());
        List<ClassOrInterfaceDeclaration> proxyList = new ArrayList<ClassOrInterfaceDeclaration>();
        patternGroup.put(PROXY_PROXY, proxyList);
        proxyList.add(proxy);

        assertFalse(new ProxyVerifier().verifyGroup(patternGroup).hasError());
    }

    @Test
    void testMissingPatterParts() throws FileNotFoundException {
        Map<Pattern, List<ClassOrInterfaceDeclaration>> patternGroup = new HashMap<>();

        ClassOrInterfaceDeclaration proxy = TestHelper.getMockClassOrI(
            "proxy/workingproxy", "Proxy");
        ClassOrInterfaceDeclaration subject = TestHelper.getMockClassOrI(
            "proxy/workingproxy", "Subject");
        ClassOrInterfaceDeclaration proxyI = TestHelper.getMockClassOrI(
            "proxy/workingproxy", "ProxyInterface");

        // List of proxy proxies is empty
        List<ClassOrInterfaceDeclaration> proxyList = new ArrayList<ClassOrInterfaceDeclaration>();
        patternGroup.put(PROXY_PROXY, proxyList);
        assertTrue(new ProxyVerifier().verifyGroup(patternGroup).hasError());
        proxyList.add(proxy);

        // List of proxy interfaces is empty
        List<ClassOrInterfaceDeclaration> proxyIList = new ArrayList<ClassOrInterfaceDeclaration>();
        patternGroup.put(PROXY_INTERFACE, proxyIList);
        assertTrue(new ProxyVerifier().verifyGroup(patternGroup).hasError());
        proxyIList.add(proxyI);

        // List of proxy subjects is empty
        List<ClassOrInterfaceDeclaration> subjectList = new ArrayList<ClassOrInterfaceDeclaration>();
        patternGroup.put(PROXY_SUBJECT, subjectList);
        assertTrue(new ProxyVerifier().verifyGroup(patternGroup).hasError());
        subjectList.add(subject);

        assertFalse(new ProxyVerifier().verifyGroup(patternGroup).hasError());
    }

}
