package micdm.transportlive2.ui.misc;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import micdm.transportlive2.AppScope;
import micdm.transportlive2.ComponentHolder;

@Module
public class MiscModule {

    @Provides
    @Named("pathLine")
    @AppScope
    PaintConstructor providePathLinePaintConstructor() {
        PathPaintConstructor instance = new PathPaintConstructor();
        ComponentHolder.getAppComponent().inject(instance);
        return instance;
    }

    @Provides
    @Named("vehicleIcon")
    @AppScope
    PaintConstructor provideVehicleIconPaintConstructor() {
        VehicleIconPaintConstructor instance = new VehicleIconPaintConstructor();
        ComponentHolder.getAppComponent().inject(instance);
        return instance;
    }

    @Provides
    @AppScope
    ColorConstructor provideColorConstructor() {
        return new ColorConstructor();
    }

    @Provides
    @AppScope
    MiscFunctions provideMiscFunctions() {
        MiscFunctions instance = new MiscFunctions();
        ComponentHolder.getAppComponent().inject(instance);
        return instance;
    }

    @Provides
    @AppScope
    VehicleMarkerIconBuilder provideVehicleMarkerIconBuilder() {
        VehicleMarkerIconBuilder instance = new VehicleMarkerIconBuilder();
        ComponentHolder.getAppComponent().inject(instance);
        return instance;
    }
}
