package micdm.transportlive2.ui.misc;

import android.graphics.Color;

public class ColorConstructor {

    private static final float SATURATION_VALUE = 0.57f;
    private static final float VALUE_VALUE = 0.87f;

    public int getByString(String value) {
        int hue = (int) (359 * (value.hashCode() & 0xFFFFFFFFL) / 0xFFFFFFFFL);
        return Color.HSVToColor(new float[] {hue, SATURATION_VALUE, VALUE_VALUE});
    }
}
