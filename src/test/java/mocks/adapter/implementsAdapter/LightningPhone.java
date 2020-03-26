package mocks.adapter.implementsAdapter;

import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;

// From the example on wikipedia: https://en.wikipedia.org/wiki/Adapter_pattern
@DesignPattern(pattern = {Pattern.ADAPTER_ADAPTEE})
interface LightningPhone {

    void recharge();

    void useLightning();
}