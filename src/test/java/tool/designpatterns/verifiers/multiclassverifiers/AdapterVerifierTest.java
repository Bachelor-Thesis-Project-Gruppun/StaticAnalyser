package tool.designpatterns.verifiers.multiclassverifiers;

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
        ClassOrInterfaceDeclaration adapter = TestHelper.getMockCompUnit(
            "adapter" + "/implementsAdapter", "MicroUsbToLightningAdapter").findAll(
            ClassOrInterfaceDeclaration.class).get(0);
        ClassOrInterfaceDeclaration adaptee = TestHelper.getMockCompUnit(
            "adapter/implementsAdapter", "LightningPhone").findAll(
            ClassOrInterfaceDeclaration.class).get(0);
        HashMap<Pattern, List<ClassOrInterfaceDeclaration>> patternGroup = createPatternGroup(
            adapter, adaptee);

        assertFalse(new AdapterVerifier().verifyGroup(patternGroup).hasError());
    }

    @Test
    public void testVerifyExtendsAdapter() throws IOException {
        ClassOrInterfaceDeclaration adapter = TestHelper.getMockCompUnit(
            "adapter" + "/extendsAdapter", "MicroUsbToLightningAdapter").findAll(
            ClassOrInterfaceDeclaration.class).get(0);

        ClassOrInterfaceDeclaration adaptee = TestHelper.getMockCompUnit("adapter/extendsAdapter",
                                                                         "Iphone").findAll(
            ClassOrInterfaceDeclaration.class).get(0);
        HashMap<Pattern, List<ClassOrInterfaceDeclaration>> patternGroup = createPatternGroup(
            adapter, adaptee);

        assertFalse(new AdapterVerifier().verifyGroup(patternGroup).hasError());
    }

    @Test
    public void testVerifyInterfaces() throws IOException {
        ClassOrInterfaceDeclaration concreteAdaptee = TestHelper.getMockCompUnit(
            "adapter" + "/implementsAdapter", "Iphone").findAll(ClassOrInterfaceDeclaration.class)
                                                                .get(0);
        ClassOrInterfaceDeclaration adaptee = TestHelper.getMockCompUnit(
            "adapter" + "/implementsAdapter", "LightningPhone").findAll(
            ClassOrInterfaceDeclaration.class).get(0);

        ClassOrInterfaceDeclaration adapter = TestHelper.getMockCompUnit(
            "adapter" + "/implementsAdapter", "MicroUsbToLightningAdapter").findAll(
            ClassOrInterfaceDeclaration.class).get(0);

        HashMap<Pattern, List<ClassOrInterfaceDeclaration>> patternGroup = createPatternGroup(
            adapter, adaptee);
        NodeList<ClassOrInterfaceType> adapteeImplements = concreteAdaptee.getImplementedTypes();

        assertFalse(new AdapterVerifier().verifyGroup(patternGroup).hasError());
        adapter.setImplementedTypes(adapteeImplements);
        assertTrue(new AdapterVerifier().verifyGroup(patternGroup).hasError());
    }

    @Test
    public void testIsWrapping() throws IOException {
        ClassOrInterfaceDeclaration adapter = TestHelper.getMockCompUnit(
            "adapter" + "/implementsAdapter", "NotWrappingAdapter").findAll(
            ClassOrInterfaceDeclaration.class).get(0);
        ClassOrInterfaceDeclaration adaptee = TestHelper.getMockCompUnit(
            "adapter" + "/implementsAdapter", "LightningPhone").findAll(
            ClassOrInterfaceDeclaration.class).get(0);

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
