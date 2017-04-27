package micdm.transportlive2.ui.misc;

import android.graphics.Paint;

import java.util.HashMap;
import java.util.Map;

public abstract class PaintConstructor {

    private final Map<String, Paint> paints = new HashMap<>();

    public Paint getByString(String value) {
        Paint paint = paints.get(value);
        if (paint == null) {
            paint = newPaint(value);
            paints.put(value, paint);
        }
        return paint;
    }

    abstract Paint newPaint(String value);
}
