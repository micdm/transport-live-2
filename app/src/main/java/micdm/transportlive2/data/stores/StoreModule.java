package micdm.transportlive2.data.stores;

import dagger.Module;
import dagger.Provides;
import micdm.transportlive2.AppScope;
import micdm.transportlive2.ComponentHolder;

@Module
public class StoreModule {

    @Provides
    @AppScope
    SelectedRoutesStore provideSelectedRoutesStore() {
        SelectedRoutesStore instance = new SelectedRoutesStore();
        ComponentHolder.getAppComponent().inject(instance);
        instance.init();
        return instance;
    }

    @Provides
    @AppScope
    RoutesStore provideRoutesStore() {
        RoutesStore instance = new RoutesStore();
        ComponentHolder.getAppComponent().inject(instance);
        instance.init();
        return instance;
    }

    @Provides
    @AppScope
    PathsStore providePathsStore() {
        PathsStore instance = new PathsStore();
        ComponentHolder.getAppComponent().inject(instance);
        instance.init();
        return instance;
    }

}
