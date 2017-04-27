package micdm.transportlive2;

import dagger.Component;
import micdm.transportlive2.data.DataModule;
import micdm.transportlive2.data.loaders.ForecastLoader;
import micdm.transportlive2.data.loaders.Loaders;
import micdm.transportlive2.data.loaders.PathLoader;
import micdm.transportlive2.data.loaders.RoutesLoader;
import micdm.transportlive2.data.loaders.SearchStationsLoader;
import micdm.transportlive2.data.loaders.StationLoader;
import micdm.transportlive2.data.loaders.VehiclesLoader;
import micdm.transportlive2.data.loaders.remote.ServerConnector;
import micdm.transportlive2.data.stores.PathStore;
import micdm.transportlive2.data.stores.RoutesStore;
import micdm.transportlive2.data.stores.Stores;
import micdm.transportlive2.misc.Cache;
import micdm.transportlive2.misc.CommonFunctions;
import micdm.transportlive2.misc.MiscModule;
import micdm.transportlive2.misc.ObservableCache;
import micdm.transportlive2.ui.UiModule;
import micdm.transportlive2.ui.misc.MiscFunctions;
import micdm.transportlive2.ui.misc.PathPaintConstructor;
import micdm.transportlive2.ui.misc.VehicleIconPaintConstructor;
import micdm.transportlive2.ui.misc.VehicleMarkerIconBuilder;
import micdm.transportlive2.ui.presenters.AllVehiclesPresenter;
import micdm.transportlive2.ui.presenters.CurrentStationPresenter;
import micdm.transportlive2.ui.presenters.ForecastPresenter;
import micdm.transportlive2.ui.presenters.PathsPresenter;
import micdm.transportlive2.ui.presenters.PreferencesPresenter;
import micdm.transportlive2.ui.presenters.Presenters;
import micdm.transportlive2.ui.presenters.RoutesPresenter;
import micdm.transportlive2.ui.presenters.SearchPresenter;
import micdm.transportlive2.ui.presenters.StationPresenter;
import micdm.transportlive2.ui.presenters.VehiclesPresenter;

@AppScope
@Component(modules = {AppModule.class, DataModule.class, MiscModule.class, RxModule.class, UiModule.class})
public interface AppComponent {

    ActivityComponent.Builder activityComponentBuilder();

    CommonFunctions getCommonFunctions();
    ObservableCache getObservableCache();

    void inject(App target);
    void inject(Cache target);
    void inject(VehicleIconPaintConstructor target);
    void inject(PathLoader target);
    void inject(PathsPresenter target);
    void inject(PathStore target);
    void inject(RoutesPresenter target);
    void inject(ServerConnector target);
    void inject(VehiclesLoader target);
    void inject(AllVehiclesPresenter target);
    void inject(RoutesStore target);
    void inject(CommonFunctions target);
    void inject(MiscFunctions target);
    void inject(VehicleMarkerIconBuilder target);
    void inject(ForecastLoader target);
    void inject(ForecastPresenter target);
    void inject(VehiclesPresenter target);
    void inject(PreferencesPresenter target);
    void inject(Stores target);
    void inject(RoutesLoader target);
    void inject(Loaders target);
    void inject(Presenters target);
    void inject(StationLoader target);
    void inject(StationPresenter target);
    void inject(SearchStationsLoader target);
    void inject(SearchPresenter target);
    void inject(CurrentStationPresenter target);
    void inject(PathPaintConstructor target);
}
