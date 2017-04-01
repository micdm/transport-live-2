package micdm.transportlive;

import dagger.Component;
import micdm.transportlive.data.DataModule;
import micdm.transportlive.data.loaders.PathLoader;
import micdm.transportlive.data.stores.PathsStore;
import micdm.transportlive.data.loaders.RoutesLoader;
import micdm.transportlive.data.stores.RoutesStore;
import micdm.transportlive.data.stores.SelectedRoutesStore;
import micdm.transportlive.data.loaders.remote.ServerConnector;
import micdm.transportlive.data.loaders.VehiclesLoader;
import micdm.transportlive.misc.Cache;
import micdm.transportlive.misc.CommonFunctions;
import micdm.transportlive.misc.MiscModule;
import micdm.transportlive.ui.PathsPresenter;
import micdm.transportlive.ui.RoutesController;
import micdm.transportlive.ui.RoutesPresenter;
import micdm.transportlive.ui.SelectedRoutesPresenter;
import micdm.transportlive.ui.UiModule;
import micdm.transportlive.ui.VehiclesController;
import micdm.transportlive.ui.VehiclesPresenter;
import micdm.transportlive.ui.misc.PaintConstructor;

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
    void inject(RoutesController target);
    void inject(RoutesLoader target);
    void inject(RoutesPresenter target);
    void inject(SelectedRoutesPresenter target);
    void inject(SelectedRoutesStore target);
    void inject(ServerConnector target);
    void inject(VehiclesController target);
    void inject(VehiclesLoader target);
    void inject(VehiclesPresenter target);
    void inject(RoutesStore target);
}
