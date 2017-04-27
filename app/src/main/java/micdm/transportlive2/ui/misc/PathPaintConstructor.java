package micdm.transportlive2.ui.misc;

import android.graphics.Paint;

import javax.inject.Inject;

public class PathPaintConstructor extends PaintConstructor {

    @Inject
    ColorConstructor colorConstructor;

    @Override
    Paint newPaint(String value) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(colorConstructor.getByString(value) & 0xAAFFFFFF);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        return paint;
    }
}
