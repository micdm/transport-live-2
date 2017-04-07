package micdm.transportlive2;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import micdm.transportlive2.ui.misc.ActivityLifecycleWatcher;
import micdm.transportlive2.ui.misc.MarkerIconBuilder;

@Module
class ActivityModule {

    private final Activity activity;

    ActivityModule(Activity activity) {
        this.activity = activity;
    }

    @Provides
    @ActivityScope
    Activity provideActivity() {
        return activity;
    }

    @Provides
    @ActivityScope
    Context provideContext() {
        return activity;
    }

    @Provides
    @ActivityScope
    LayoutInflater provideLayoutInflater(Context context) {
        return (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Provides
    @ActivityScope
    ActivityLifecycleWatcher provideActivityLifecycleWatcher() {
        return new ActivityLifecycleWatcher();
    }

    @Provides
    @ActivityScope
    Bitmap provideVehicleIcon(Context context) {
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_vehicle);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @Provides
    @Named("vehicleIconText")
    @ActivityScope
    Paint provideVehicleIconTextPaint(Bitmap vehicleIcon) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(vehicleIcon.getWidth() * 0.25f);
        return paint;
    }

    @Provides
    @ActivityScope
    MarkerIconBuilder provideMarkerIconBuilder() {
        MarkerIconBuilder instance = new MarkerIconBuilder();
        ComponentHolder.getActivityComponent().inject(instance);
        return instance;
    }
}
