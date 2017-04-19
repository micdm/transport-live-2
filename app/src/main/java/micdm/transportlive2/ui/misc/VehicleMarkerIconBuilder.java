package micdm.transportlive2.ui.misc;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.util.Pools;

import javax.inject.Inject;
import javax.inject.Named;

public class VehicleMarkerIconBuilder {

    public class BitmapWrapper {

        private final Bitmap bitmap;
        private final Canvas canvas;

        private BitmapWrapper(Bitmap bitmap, Canvas canvas) {
            this.bitmap = bitmap;
            this.canvas = canvas;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public void recycle() {
            bitmap.eraseColor(Color.TRANSPARENT);
            bitmaps.release(this);
        }
    }

    private static final int BITMAP_POOL_SIZE = 1000;

    private final Matrix matrix = new Matrix();
    private final Rect bounds = new Rect();
    private final Pools.Pool<BitmapWrapper> bitmaps = new Pools.SimplePool<>(BITMAP_POOL_SIZE);

    @Inject
    @Named("vehicleIcon")
    Bitmap original;
    @Inject
    PaintConstructor paintConstructor;
    @Inject
    @Named("vehicleIconText")
    Paint textPaint;

    public BitmapWrapper build(String routeId, String routeNumber, double direction) {
        BitmapWrapper bitmapWrapper = bitmaps.acquire();
        if (bitmapWrapper == null) {
            bitmapWrapper = newBitmapWrapper();
        }
        matrix.setRotate((float) direction, original.getWidth() / 2, original.getHeight() / 2);
        bitmapWrapper.canvas.drawBitmap(original, matrix, paintConstructor.getByString(routeId));
        textPaint.getTextBounds(routeNumber, 0, routeNumber.length(), bounds);
        bitmapWrapper.canvas.drawText(routeNumber, bitmapWrapper.canvas.getWidth() / 2 - bounds.width() / 2,
                                   bitmapWrapper.canvas.getHeight() / 2 + bounds.height() / 2, textPaint);
        return bitmapWrapper;
    }

    private BitmapWrapper newBitmapWrapper() {
        Bitmap bitmap = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        return new BitmapWrapper(bitmap, canvas);
    }
}
