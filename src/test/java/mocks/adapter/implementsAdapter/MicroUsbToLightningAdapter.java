package mocks.adapter.implementsAdapter;

import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;

@DesignPattern(pattern = {Pattern.ADAPTER_ADAPTER})
class MicroUsbToLightningAdapter implements LightningPhone {

    private final LightningPhone lightningPhone;

    public MicroUsbToLightningAdapter(LightningPhone lightningPhone) {
        this.lightningPhone = lightningPhone;
    }

    @Override
    public void useLightning() {
        System.out.println("MicroUsb connected");
        lightningPhone.useLightning();
    }

    @Override
    public void recharge() {
        lightningPhone.recharge();
    }
}


