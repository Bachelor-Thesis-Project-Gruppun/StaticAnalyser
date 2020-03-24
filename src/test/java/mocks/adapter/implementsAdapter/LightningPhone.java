package mocks.adapter.implementsAdapter;

import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;

@DesignPattern(pattern = {Pattern.ADAPTER_ADAPTEE})
interface LightningPhone {

    void recharge();

    void useLightning();
}