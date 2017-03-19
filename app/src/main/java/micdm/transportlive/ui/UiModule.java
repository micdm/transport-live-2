package micdm.transportlive.ui;

import com.bluelinelabs.conductor.Controller;

import dagger.Module;
import dagger.Provides;
import micdm.transportlive.AppScope;
import micdm.transportlive.ComponentHolder;

@Module
public class UiModule {

    @Provides
    @AppScope
    PresenterStore providePresenterStore() {
        return new PresenterStore();
    }

    @Provides
    @AppScope
    Controller provideRootController(MainController controller) {
        return controller;
    }

    @Provides
    @AppScope
    MainController provideMainController() {
        MainController instance = new MainController();
        ComponentHolder.getAppComponent().inject(instance);
        return instance;
    }

    @Provides
    @AppScope
    MapController provideMapController() {
        MapController instance = new MapController();
        ComponentHolder.getAppComponent().inject(instance);
        return instance;
    }

    @Provides
    @AppScope
    RoutesController provideRoutesController() {
        RoutesController instance = new RoutesController();
        ComponentHolder.getAppComponent().inject(instance);
        return instance;
    }
}
