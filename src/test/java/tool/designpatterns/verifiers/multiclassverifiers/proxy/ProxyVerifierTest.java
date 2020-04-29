package tool.designpatterns.verifiers.multiclassverifiers.proxy;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.gradle.internal.impldep.org.junit.Assert.assertFalse;
import static org.gradle.internal.impldep.org.junit.Assert.assertTrue;
import static tool.designpatterns.Pattern.PROXY_INTERFACE;
import static tool.designpatterns.Pattern.PROXY_PROXY;
import static tool.designpatterns.Pattern.PROXY_SUBJECT;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import org.junit.jupiter.api.Test;
import tool.designpatterns.Pattern;
import utilities.TestHelper;

public class ProxyVerifierTest {

    @Test
    void testWorkingProxy() throws FileNotFoundException {
        Map<Pattern, List<ClassOrInterfaceDeclaration>> patternGroup = getPatternGroup(
            "proxy/workingproxy", false);
        assertFalse(new ProxyVerifier().verifyGroup(patternGroup).hasError());
    }

    @Test
    void testDuplicateWorkingProxies() throws FileNotFoundException {
        Map<Pattern, List<ClassOrInterfaceDeclaration>> patternGroup = getMultiInstancePatternMap(
            "proxy/duplicateworkingproxies");
        assertFalse(new ProxyVerifier().verifyGroup(patternGroup).hasError());
    }

    @Test
    void testEmptyProxies() {
        Map<Pattern, List<ClassOrInterfaceDeclaration>> proxyGroup = new HashMap<>();
        proxyGroup.put(PROXY_PROXY, new ArrayList<>());
        assertTrue(new ProxyVerifier().verifyGroup(proxyGroup).hasError());
    }

    @Test
    void testEmptySubjects() {
        Map<Pattern, List<ClassOrInterfaceDeclaration>> proxyGroup = new HashMap<>();
        proxyGroup.put(PROXY_SUBJECT, new ArrayList<>());
        assertTrue(new ProxyVerifier().verifyGroup(proxyGroup).hasError());
    }

    @Test
    void testEmptyInterfaces() {
        Map<Pattern, List<ClassOrInterfaceDeclaration>> proxyGroup = new HashMap<>();
        proxyGroup.put(PROXY_INTERFACE, new ArrayList<>());
        assertTrue(new ProxyVerifier().verifyGroup(proxyGroup).hasError());
    }

    @Test
    void testNoSubjectVariableInProxy() {
        Map<Pattern, List<ClassOrInterfaceDeclaration>> proxyGroup = getPatternGroup(
            "proxy/proxynosubjectvariableinproxy", false);
        assertTrue(new ProxyVerifier().verifyGroup(proxyGroup).hasError());
    }

    @Test
    void testProxyNoImplementInterface() {
        Map<Pattern, List<ClassOrInterfaceDeclaration>> proxyGroup = getPatternGroup(
            "proxy/proxynoimplementinterface", false);
        assertTrue(new ProxyVerifier().verifyGroup(proxyGroup).hasError());
    }

    @Test
    void testSubjectNoImplementProxyInterface() {
        Map<Pattern, List<ClassOrInterfaceDeclaration>> proxyGroup = getPatternGroup(
            "proxy/subjectnoimplementproxyinterface", false);
        assertTrue(new ProxyVerifier().verifyGroup(proxyGroup).hasError());
    }

    @Test
    void testDuplicateWorkingAndBroken(){
        Map<Pattern, List<ClassOrInterfaceDeclaration>> patternGroup = getMultiInstancePatternMap(
            "proxy/workingandbrokenproxy");
        assertTrue(new ProxyVerifier().verifyGroup(patternGroup).hasError());
    }

    /**
     * Creates a patternGroupMap with the parts at the given path.
     *
     * @param path           the path.
     * @param pathIsAbsolute whether the given path is an absolute path or not.
     *
     * @return the map.
     */
    private Map<Pattern, List<ClassOrInterfaceDeclaration>> getPatternGroup(
        String path, boolean pathIsAbsolute) {

        Map<Pattern, List<ClassOrInterfaceDeclaration>> map = new HashMap<>();

        List<ClassOrInterfaceDeclaration> proxies = new ArrayList<>();
        try {
            ClassOrInterfaceDeclaration proxy;
            if (pathIsAbsolute) {
                proxy = TestHelper.getMockClassOrIAbsPath(path, "Proxy");
            } else {
                proxy = TestHelper.getMockClassOrI(path, "Proxy");
            }
            proxies.add(proxy);
        } catch (FileNotFoundException error) {
            // Ignore
        }
        map.put(PROXY_PROXY, proxies);

        List<ClassOrInterfaceDeclaration> subjects = new ArrayList<>();
        try {
            ClassOrInterfaceDeclaration subject;
            if (pathIsAbsolute) {
                subject = TestHelper.getMockClassOrIAbsPath(path, "Subject");
            } else {
                subject = TestHelper.getMockClassOrI(path, "Subject");
            }
            subjects.add(subject);
        } catch (FileNotFoundException error) {
            // Ignore
        }
        map.put(PROXY_SUBJECT, subjects);

        List<ClassOrInterfaceDeclaration> interfaces = new ArrayList<>();
        try {
            ClassOrInterfaceDeclaration proxyInterface;
            if (pathIsAbsolute) {
                proxyInterface = TestHelper.getMockClassOrIAbsPath(path, "ProxyInterface");
            } else {
                proxyInterface = TestHelper.getMockClassOrI(path, "ProxyInterface");
            }
            interfaces.add(proxyInterface);
        } catch (FileNotFoundException error) {
            // Ignore
        }

        map.put(PROXY_INTERFACE, interfaces);

        return map;
    }

    /**
     * Get a map for multiple instances in subfolders in the given path.
     *
     * @param path the root path.
     *
     * @return the map
     */
    private Map<Pattern, List<ClassOrInterfaceDeclaration>> getMultiInstancePatternMap(
        String path) {

        String fullPath = "src/test/java/mocks/" + path;
        File file = new File(fullPath).getAbsoluteFile();
        if (!file.isDirectory()) {
            return null;
        }

        Map<Pattern, List<ClassOrInterfaceDeclaration>> map = new HashMap<>();
        map.put(PROXY_PROXY, new ArrayList<>());
        map.put(PROXY_INTERFACE, new ArrayList<>());
        map.put(PROXY_SUBJECT, new ArrayList<>());

        for (File childFile : Objects.requireNonNull(file.listFiles())) {
            if (childFile.isDirectory()) {
                Map<Pattern, List<ClassOrInterfaceDeclaration>> subMap = getPatternGroup(
                    childFile.getPath(), true);

                Pattern curr = PROXY_PROXY;
                if (subMap.containsKey(curr)) {
                    map.get(curr).addAll(subMap.get(curr));
                }

                curr = PROXY_SUBJECT;
                if (subMap.containsKey(curr)) {
                    map.get(curr).addAll(subMap.get(curr));
                }

                curr = PROXY_INTERFACE;
                if (subMap.containsKey(curr)) {
                    map.get(curr).addAll(subMap.get(curr));
                }
            }
        }

        return map;
    }
}
