package mocks.adapter.implementsAdapter;

import base.DesignPattern;
import base.Pattern;

@DesignPattern(pattern = {Pattern.ADAPTER_ADAPTEE})
interface LightningPhone {

    void recharge();

    void useLightning();
}