package tool.designpatterns.verifiers.multiclassverifiers.adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.gradle.internal.impldep.org.junit.Assert.assertFalse;
import static org.gradle.internal.impldep.org.junit.Assert.assertTrue;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import org.junit.jupiter.api.Test;
import tool.designpatterns.Pattern;
import utilities.TestHelper;

public class AdapterVerifierTest {

    @Test
    public void testVerifyImplementsAdapter() throws IOException {
        ClassOrInterfaceDeclaration adapter = TestHelper.getMockClassOrI(
            "adapter" + "/implementsAdapter", "MicroUsbToLightningAdapter");
        ClassOrInterfaceDeclaration adaptee = TestHelper.getMockClassOrI(
            "adapter/implementsAdapter", "LightningPhone");
        HashMap<Pattern, List<ClassOrInterfaceDeclaration>> patternGroup = createPatternGroup(
            adapter, adaptee);

        System.out.println(new AdapterVerifier().verifyGroup(patternGroup).getFullMessage());
        assertFalse(new AdapterVerifier().verifyGroup(patternGroup).hasError());
    }

    @Test
    public void testVerifyExtendsAdapter() throws IOException {
        ClassOrInterfaceDeclaration adapter = TestHelper.getMockClassOrI(
            "adapter/extendsAdapter", "MicroUsbToLightningAdapter");

        ClassOrInterfaceDeclaration adaptee = TestHelper.getMockClassOrI("adapter/extendsAdapter",
            "Iphone");
        HashMap<Pattern, List<ClassOrInterfaceDeclaration>> patternGroup = createPatternGroup(
            adapter, adaptee);

        assertFalse(new AdapterVerifier().verifyGroup(patternGroup).hasError());
    }

    @Test
    public void testVerifyInterfaces() throws IOException {
        ClassOrInterfaceDeclaration concreteAdaptee = TestHelper.getMockClassOrI(
            "adapter" + "/implementsAdapter", "Iphone");
        ClassOrInterfaceDeclaration adaptee = TestHelper.getMockClassOrI(
            "adapter" + "/implementsAdapter", "LightningPhone");

        ClassOrInterfaceDeclaration adapter = TestHelper.getMockClassOrI(
            "adapter" + "/implementsAdapter", "MicroUsbToLightningAdapter");

        HashMap<Pattern, List<ClassOrInterfaceDeclaration>> patternGroup = createPatternGroup(
            adapter, adaptee);
        NodeList<ClassOrInterfaceType> adapteeImplements = concreteAdaptee.getImplementedTypes();

        assertFalse(new AdapterVerifier().verifyGroup(patternGroup).hasError());
        adapter.setImplementedTypes(adapteeImplements);
        assertTrue(new AdapterVerifier().verifyGroup(patternGroup).hasError());
    }

    @Test
    public void testIsWrapping() throws IOException {
        ClassOrInterfaceDeclaration adapter = TestHelper.getMockClassOrI(
            "adapter" + "/implementsAdapter", "NotWrappingAdapter");
        ClassOrInterfaceDeclaration adaptee = TestHelper.getMockClassOrI(
            "adapter" + "/implementsAdapter", "LightningPhone");

        HashMap<Pattern, List<ClassOrInterfaceDeclaration>> patternGroup = createPatternGroup(
            adapter, adaptee);
        assertTrue(new AdapterVerifier().verifyGroup(patternGroup).hasError());
    }

    private HashMap<Pattern, List<ClassOrInterfaceDeclaration>> createPatternGroup(
        ClassOrInterfaceDeclaration adapter, ClassOrInterfaceDeclaration adaptee) {
        HashMap<Pattern, List<ClassOrInterfaceDeclaration>> patternGroup = new HashMap<>();
        List<ClassOrInterfaceDeclaration> a = new ArrayList<>();
        List<ClassOrInterfaceDeclaration> b = new ArrayList<>();

        a.add(adapter);
        b.add(adaptee);

        patternGroup.put(Pattern.ADAPTER_ADAPTER, a);
        patternGroup.put(Pattern.ADAPTER_ADAPTEE, b);
        return patternGroup;
    }
}
