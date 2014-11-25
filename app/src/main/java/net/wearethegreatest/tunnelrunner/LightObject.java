package net.wearethegreatest.tunnelrunner;

public class LightObject {
    final public int MIN_POS = 1;
    final public int MAX_POS = 32;

    private int position = 17;
    public int red = 255;
    public int green = 0;
    public int blue = 0;

    public void setPosition(int num) {
        if (num < MIN_POS) {
            position = MIN_POS;
        } else if (num > MAX_POS) {
            position = MAX_POS;
        } else {
            position = num;
        }
    }

    public int getPosition() {
        return position;
    }

}
