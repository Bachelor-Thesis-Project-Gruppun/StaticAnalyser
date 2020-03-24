package mocks.adapter.implementsAdapter;

import base.DesignPattern;
import base.Pattern;

@DesignPattern(pattern = {Pattern.ADAPTER_INTERFACE})
interface MicroUsbPhone {

    void recharge();

    void useMicroUsb();
}
