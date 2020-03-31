package mocks.adapter.implementsAdapter;

// From the example on wikipedia: https://en.wikipedia.org/wiki/Adapter_pattern
class Iphone2 implements LightningPhone {

    private boolean connector;

    @Override
    public void useLightning() {
        connector = true;
        System.out.println("Lightning connected");
    }

    @Override
    public void recharge() {
        if (connector) {
            System.out.println("Recharge started");
            System.out.println("Recharge finished");
        } else {
            System.out.println("Connect Lightning first");
        }
    }
}
