package micdm.transportlive2.ui.misc;

import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class PaintConstructor {

    private static final int SHADOW_SIZE = 5;

    @Inject
    ColorConstructor colorConstructor;

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
        paint.setColorFilter(new LightingColorFilter(Color.BLACK, colorConstructor.getByString(value)));
        paint.setShadowLayer(SHADOW_SIZE, 0, 0, 0xFF333333);
        return paint;
    }
}
