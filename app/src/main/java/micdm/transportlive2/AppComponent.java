package micdm.transportlive2;

import dagger.Component;
import micdm.transportlive2.data.DataModule;
import micdm.transportlive2.data.loaders.PathLoader;
import micdm.transportlive2.data.loaders.RoutesLoader;
import micdm.transportlive2.data.loaders.VehiclesLoader;
import micdm.transportlive2.data.loaders.remote.ServerConnector;
import micdm.transportlive2.data.stores.PathsStore;
import micdm.transportlive2.data.stores.RoutesStore;
import micdm.transportlive2.data.stores.SelectedRoutesStore;
import micdm.transportlive2.misc.Cache;
import micdm.transportlive2.misc.CommonFunctions;
import micdm.transportlive2.misc.MiscModule;
import micdm.transportlive2.ui.PathsPresenter;
import micdm.transportlive2.ui.RoutesPresenter;
import micdm.transportlive2.ui.SelectedRoutesPresenter;
import micdm.transportlive2.ui.UiModule;
import micdm.transportlive2.ui.VehiclesPresenter;
import micdm.transportlive2.ui.misc.MiscFunctions;
import micdm.transportlive2.ui.misc.PaintConstructor;
import micdm.transportlive2.ui.misc.VehicleMarkerIconBuilder;

@AppScope
@Component(modules = {AppModule.class, DataModule.class, MiscModule.class, RxModule.class, UiModule.class})
public interface AppComponent {

    ActivityComponent.Builder activityComponentBuilder();

    CommonFunctions getCommonFunctions();

    void inject(App target);
    void inject(Cache target);
    void inject(PaintConstructor target);
    void inject(PathLoader target);
    void inject(PathsPresenter target);
    void inject(PathsStore target);
    void inject(RoutesLoader target);
    void inject(RoutesPresenter target);
    void inject(SelectedRoutesPresenter target);
    void inject(SelectedRoutesStore target);
    void inject(ServerConnector target);
    void inject(VehiclesLoader target);
    void inject(VehiclesPresenter target);
    void inject(RoutesStore target);
    void inject(CommonFunctions target);
    void inject(MiscFunctions target);
    void inject(VehicleMarkerIconBuilder target);
}
