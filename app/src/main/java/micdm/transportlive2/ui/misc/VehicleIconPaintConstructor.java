package micdm.transportlive2.ui.misc;

import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;

import javax.inject.Inject;

public class VehicleIconPaintConstructor extends PaintConstructor {

    private static final int SHADOW_SIZE = 5;

    @Inject
    ColorConstructor colorConstructor;

    @Override
    Paint newPaint(String value) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        paint.setColorFilter(new LightingColorFilter(Color.BLACK, colorConstructor.getByString(value)));
        paint.setShadowLayer(SHADOW_SIZE, 0, 0, 0xFF333333);
        return paint;
    }
}
