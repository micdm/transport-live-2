package micdm.transportlive2.ui;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import micdm.transportlive2.App;
import micdm.transportlive2.AppScope;
import micdm.transportlive2.ComponentHolder;
import micdm.transportlive2.R;
import micdm.transportlive2.ui.misc.MiscModule;

@Module(includes = {MiscModule.class})
public class UiModule {

    @Provides
    @AppScope
    RoutesPresenter provideRoutesPresenter() {
        RoutesPresenter instance = new RoutesPresenter();
        ComponentHolder.getAppComponent().inject(instance);
        instance.init();
        return instance;
    }

    @Provides
    @AppScope
    AllVehiclesPresenter provideAllVehiclesPresenter() {
        AllVehiclesPresenter instance = new AllVehiclesPresenter();
        ComponentHolder.getAppComponent().inject(instance);
        instance.init();
        return instance;
    }

    @Provides
    @AppScope
    PathsPresenter providePathsPresenter() {
        PathsPresenter instance = new PathsPresenter();
        ComponentHolder.getAppComponent().inject(instance);
        instance.init();
        return instance;
    }

    @Provides
    @AppScope
    PreferencesPresenter providePreferencesPresenter() {
        PreferencesPresenter instance = new PreferencesPresenter();
        ComponentHolder.getAppComponent().inject(instance);
        instance.init();
        return instance;
    }

    @Provides
    @AppScope
    Presenters providePresenters() {
        Presenters instance = new Presenters();
        ComponentHolder.getAppComponent().inject(instance);
        return instance;
    }

    @Provides
    @AppScope
    @Named("vehicleIcon")
    Bitmap provideVehicleIcon(App app) {
        Drawable drawable = ContextCompat.getDrawable(app, R.drawable.ic_vehicle);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @Provides
    @AppScope
    @Named("stationIcon")
    Bitmap provideStationIcon(App app, Resources resources) {
        Drawable drawable = ContextCompat.getDrawable(app, R.drawable.ic_station);
        int iconSize = resources.getDimensionPixelSize(R.dimen.station_marker_size);
        Bitmap bitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        int iconPadding = resources.getDimensionPixelSize(R.dimen.station_marker_padding);
        drawable.setBounds(iconPadding, iconPadding, canvas.getWidth() - iconPadding, canvas.getHeight() - iconPadding);
        drawable.setColorFilter(resources.getColor(R.color.station_icon), PorterDuff.Mode.SRC_ATOP);
        drawable.draw(canvas);
        return bitmap;
    }

    @Provides
    @Named("vehicleIconText")
    @AppScope
    Paint provideVehicleIconTextPaint(@Named("vehicleIcon") Bitmap vehicleIcon) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(vehicleIcon.getWidth() * 0.25f);
        return paint;
    }

    @Provides
    ValueAnimator provideMarkerAnimator(TypeEvaluator<LatLng> typeEvaluator) {
        return ValueAnimator.ofObject(typeEvaluator, new LatLng(0, 0));
    }

    @Provides
    @AppScope
    TypeEvaluator<LatLng> provideLatLngTypeEvaluator() {
        return (fraction, startValue, endValue) -> new LatLng(
            startValue.latitude + (endValue.latitude - startValue.latitude) * fraction,
            startValue.longitude + (endValue.longitude - startValue.longitude) * fraction
        );
    }
}
