package mocks.proxy.subjectnoimplementproxyinterface;

import java.util.HashMap;
import java.util.Map;

public class Subject{

    Map<Integer, String> texts;

    public Subject() {
        texts = new HashMap<>();
        for (int i = 0; i < 128; i++) {
            texts.put(i, "Numero " + i);
        }
    }

    public int[] getTextIDs() {
        int[] arr = new int[texts.size()];
        texts.keySet().forEach(integer -> {
            arr[integer] = integer.intValue();
        });

        return arr;
    }

    public String getText(int id) {
        return texts.get(id);
    }
}
