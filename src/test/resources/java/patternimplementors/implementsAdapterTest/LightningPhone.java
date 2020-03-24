package implementsAdapterTest;

import base.DesignPattern;
import base.Pattern;

@DesignPattern(pattern = {Pattern.ADAPTER_INTERFACE, Pattern.ADAPTER_ADAPTEE})
interface LightningPhone {

    void recharge();

    void useLightning();
}