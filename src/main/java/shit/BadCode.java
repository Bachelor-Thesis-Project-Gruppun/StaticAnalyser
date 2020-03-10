package shit;

import base.DesignPattern;
import base.Pattern;

@DesignPattern(pattern = {Pattern.IMMUTABLE})
public class BadCode {

    public String FUCKOFFF;

    public BadCode() {
        FUCKOFFF = "APA";
    }

    public void setFUCKOFFF(String a) {
        this.FUCKOFFF = a;
    }

}
