package micdm.transportlive.ui.misc;

import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;

import java.util.HashMap;
import java.util.Map;

public class PaintConstructor {

    private static final float SATURATION_VALUE = 0.57f;
    private static final float VALUE_VALUE = 0.87f;
    private static final int SHADOW_SIZE = 5;

    private final Map<String, Paint> paints = new HashMap<>();

    Paint getByString(String value) {
        Paint paint = paints.get(value);
        if (paint == null) {
            paint = newPaint(value);
            paints.put(value, paint);
        }
        return paint;
    }

    private Paint newPaint(String value) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        int hue = (int) (359 * (value.hashCode() & 0xFFFFFFFFL) / 0xFFFFFFFFL);
        int color = Color.HSVToColor(new float[] {hue, SATURATION_VALUE, VALUE_VALUE});
        paint.setColorFilter(new LightingColorFilter(Color.BLACK, color));
        paint.setShadowLayer(SHADOW_SIZE, 0, 0, 0xFF333333);
        return paint;
    }
}
