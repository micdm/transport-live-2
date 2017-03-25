package micdm.transportlive;

import dagger.Component;
import micdm.transportlive.data.DataModule;
import micdm.transportlive.data.RoutesLoader;
import micdm.transportlive.data.SelectedRoutesStore;
import micdm.transportlive.data.ServerConnector;
import micdm.transportlive.data.VehiclesLoader;
import micdm.transportlive.ui.RoutesController;
import micdm.transportlive.ui.RoutesPresenter;
import micdm.transportlive.ui.SelectedRoutesPresenter;
import micdm.transportlive.ui.UiModule;
import micdm.transportlive.ui.VehiclesController;
import micdm.transportlive.ui.VehiclesPresenter;

@AppScope
@Component(modules = {AppModule.class, DataModule.class, RxModule.class, UiModule.class})
public interface AppComponent {

    ActivityComponent.Builder activityComponentBuilder();

    void inject(App target);
    void inject(RoutesController target);
    void inject(RoutesLoader target);
    void inject(RoutesPresenter target);
    void inject(SelectedRoutesPresenter target);
    void inject(SelectedRoutesStore target);
    void inject(ServerConnector target);
    void inject(VehiclesController target);
    void inject(VehiclesLoader target);
    void inject(VehiclesPresenter target);
}
