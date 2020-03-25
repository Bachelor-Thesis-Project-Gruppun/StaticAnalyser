package tool.designpatterns.verifiers.multiclassverifiers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.gradle.internal.impldep.org.junit.Assert.assertFalse;
import static org.gradle.internal.impldep.org.junit.Assert.assertTrue;

import com.github.javaparser.ast.CompilationUnit;
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
        HashMap<Pattern, List<CompilationUnit>> patternGroup = createPatternGroup(adapter, adaptee);

        assertTrue(new AdapterVerifier().verifyGroup(patternGroup).getValue());
    }

    @Test
    public void testVerifyExtendsAdapter() throws IOException {
        ClassOrInterfaceDeclaration adapter = TestHelper.getMockCompUnit(
            "adapter" + "/extendsAdapter", "MicroUsbToLightningAdapter").findAll(
            ClassOrInterfaceDeclaration.class).get(0);

        ClassOrInterfaceDeclaration adaptee = TestHelper.getMockCompUnit("adapter/extendsAdapter",
                                                                         "Iphone").findAll(
            ClassOrInterfaceDeclaration.class).get(0);
        HashMap<Pattern, List<CompilationUnit>> patternGroup = createPatternGroup(adapter, adaptee);

        assertTrue(new AdapterVerifier().verifyGroup(patternGroup).getValue());
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

        HashMap<Pattern, List<CompilationUnit>> patternGroup = createPatternGroup(adapter, adaptee);
        NodeList<ClassOrInterfaceType> adapteeImplements = concreteAdaptee.getImplementedTypes();

        assertTrue(new AdapterVerifier().verifyGroup(patternGroup).getValue());
        adapter.setImplementedTypes(adapteeImplements);
        assertFalse(new AdapterVerifier().verifyGroup(patternGroup).getValue());
    }

    @Test
    public void testIsWrapping() throws IOException {
        ClassOrInterfaceDeclaration adapter = TestHelper.getMockCompUnit(
            "adapter" + "/implementsAdapter", "NotWrappingAdapter").findAll(
            ClassOrInterfaceDeclaration.class).get(0);
        ClassOrInterfaceDeclaration adaptee = TestHelper.getMockCompUnit(
            "adapter" + "/implementsAdapter", "LightningPhone").findAll(
            ClassOrInterfaceDeclaration.class).get(0);

        HashMap<Pattern, List<CompilationUnit>> patternGroup = createPatternGroup(adapter, adaptee);
        assertFalse(new AdapterVerifier().verifyGroup(patternGroup).getValue());

    }

    private HashMap<Pattern, List<CompilationUnit>> createPatternGroup(
        ClassOrInterfaceDeclaration adapter, ClassOrInterfaceDeclaration adaptee) {
        HashMap<Pattern, List<CompilationUnit>> patternGroup = new HashMap<>();
        List<CompilationUnit> a = new ArrayList<>();
        List<CompilationUnit> b = new ArrayList<>();

        a.add(adapter.findCompilationUnit().get());
        b.add(adaptee.findCompilationUnit().get());

        patternGroup.put(Pattern.ADAPTER_ADAPTER, a);
        patternGroup.put(Pattern.ADAPTER_ADAPTEE, b);
        return patternGroup;
    }
}
