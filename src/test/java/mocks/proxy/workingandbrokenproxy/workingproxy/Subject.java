package mocks.proxy.workingandbrokenproxy.workingproxy;

import java.util.HashMap;
import java.util.Map;

public class Subject implements ProxyInterface {

    Map<Integer, String> texts;

    public Subject() {
        texts = new HashMap<>();
        for (int i = 0; i < 128; i++) {
            texts.put(i, "Numero " + i);
        }
    }

    @Override
    public int[] getTextIDs() {
        int[] arr = new int[texts.size()];
        texts.keySet().forEach(integer -> {
            arr[integer] = integer.intValue();
        });

        return arr;
    }

    @Override
    public String getText(int id) {
        return texts.get(id);
    }
}
