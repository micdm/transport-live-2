package micdm.transportlive2.ui.presenters;

import dagger.Module;
import dagger.Provides;
import micdm.transportlive2.AppScope;
import micdm.transportlive2.ComponentHolder;

@Module
public class PresenterModule {

    @Provides
    @AppScope
    CurrentStationPresenter provideCurrentStationPresenter() {
        CurrentStationPresenter instance = new CurrentStationPresenter();
        ComponentHolder.getAppComponent().inject(instance);
        instance.init();
        return instance;
    }

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
    SearchPresenter provideSearchPresenter() {
        SearchPresenter instance = new SearchPresenter();
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
}
