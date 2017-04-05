package micdm.transportlive.ui;

import dagger.Module;
import dagger.Provides;
import micdm.transportlive.AppScope;
import micdm.transportlive.ComponentHolder;
import micdm.transportlive.ui.misc.MiscModule;

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
    SelectedRoutesPresenter provideSelectedRoutesPresenter() {
        SelectedRoutesPresenter instance = new SelectedRoutesPresenter();
        ComponentHolder.getAppComponent().inject(instance);
        instance.init();
        return instance;
    }

    @Provides
    @AppScope
    VehiclesPresenter provideVehiclesPresenter() {
        VehiclesPresenter instance = new VehiclesPresenter();
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
}
