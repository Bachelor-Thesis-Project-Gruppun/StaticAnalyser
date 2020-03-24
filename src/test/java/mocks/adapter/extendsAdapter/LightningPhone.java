package mocks.adapter.extendsAdapter;

import base.DesignPattern;
import base.Pattern;

@DesignPattern(pattern = {Pattern.ADAPTER_INTERFACE})
interface LightningPhone {

    void recharge();

    void useLightning();
}