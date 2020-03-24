package patternverifiers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;

import base.Pattern;
import org.junit.jupiter.api.Test;
import utilities.TestHelper;

public class adapterVerifierTest {

    @Test
    public void testVerifyExtendsAdapter() throws IOException {
        List<CompilationUnit> adapter = new ArrayList<>();
        adapter.add(TestHelper.getMockCompUnit("adapter" + "/implementsAdapter",
                                               "MicroUsbToLightningAdapter"));

        List<CompilationUnit> adaptee = new ArrayList<>();
        adaptee.add(TestHelper.getMockCompUnit("adapter/implementsAdapter", "LightningPhone"));
        HashMap<Pattern, List<CompilationUnit>> patternGroup = new HashMap<>();
        patternGroup.put(Pattern.ADAPTER_ADAPTER, adapter);
        patternGroup.put(Pattern.ADAPTER_ADAPTEE, adaptee);

        new AdapterVerifier().verifyGroup(patternGroup);
    }
}
