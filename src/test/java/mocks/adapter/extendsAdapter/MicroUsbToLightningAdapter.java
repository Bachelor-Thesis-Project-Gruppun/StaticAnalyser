package mocks.adapter.extendsAdapter;

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


