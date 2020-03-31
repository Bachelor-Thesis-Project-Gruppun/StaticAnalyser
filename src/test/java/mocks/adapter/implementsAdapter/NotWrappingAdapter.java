package mocks.adapter.implementsAdapter;

public class NotWrappingAdapter implements LightningPhone {

    private final LightningPhone lightningPhone;

    public NotWrappingAdapter() {
        lightningPhone = new Iphone2();
    }

    @Override
    public void recharge() {
        System.out.println("Hello");
    }

    @Override
    public void useLightning() {
        System.out.println("World");
    }
}
