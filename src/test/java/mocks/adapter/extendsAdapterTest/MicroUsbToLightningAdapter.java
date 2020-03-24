package mocks.adapter.extendsAdapterTest;

import base.DesignPattern;
import base.Pattern;

@DesignPattern(pattern = {Pattern.ADAPTER_ADAPTER})
class MicroUsbToLightningAdapter extends Iphone implements MicroUsbPhone {

    private final LightningPhone lightningPhone;

    public MicroUsbToLightningAdapter(LightningPhone lightningPhone) {
        this.lightningPhone = lightningPhone;
    }

    @Override
    public void useMicroUsb() {
        System.out.println("MicroUsb connected");
        super.useLightning();
    }

    @Override
    public void recharge() {
        super.recharge();
    }
}


