package mocks.adapter.implementsAdapter;

import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;

// From the example on wikipedia: https://en.wikipedia.org/wiki/Adapter_pattern
@DesignPattern(pattern = {Pattern.ADAPTER_ADAPTER})
class MicroUsbToLightningAdapter implements MicroUsbPhone {

    private final LightningPhone lightningPhone;

    public MicroUsbToLightningAdapter(LightningPhone lightningPhone) {
        this.lightningPhone = lightningPhone;
    }

    @Override
    public void useMicroUsb() {
        System.out.println("MicroUsb connected");
        lightningPhone.useLightning();
    }

    @Override
    public void recharge() {
        lightningPhone.recharge();
    }
}


