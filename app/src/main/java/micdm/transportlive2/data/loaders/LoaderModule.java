package micdm.transportlive2.data.loaders;

import dagger.Module;
import dagger.Provides;
import micdm.transportlive2.AppScope;
import micdm.transportlive2.ComponentHolder;
import micdm.transportlive2.data.loaders.remote.RemoteModule;
import micdm.transportlive2.data.loaders.remote.ServerConnector;
import micdm.transportlive2.data.stores.RoutesStore;
import micdm.transportlive2.misc.IdFactory;

@Module(includes = {RemoteModule.class})
public class LoaderModule {

    @Provides
    @AppScope
    Loaders provideLoaders() {
        Loaders instance = new Loaders();
        ComponentHolder.getAppComponent().inject(instance);
        return instance;
    }

    @Provides
    @AppScope
    RoutesLoader provideRoutesLoader(IdFactory idFactory, RoutesStore routesStore, ServerConnector serverConnector) {
        RoutesLoader instance = new RoutesLoader(
            new StoreCacheLoader<>(routesStore),
            new RoutesLoader.RoutesServerLoader(idFactory, serverConnector),
            new RoutesLoader.RoutesStoreClient(routesStore)
        );
        ComponentHolder.getAppComponent().inject(instance);
        instance.init();
        return instance;
    }

    @Provides
    @AppScope
    SearchStationsLoader provideSearchLoader() {
        SearchStationsLoader instance = new SearchStationsLoader();
        ComponentHolder.getAppComponent().inject(instance);
        return instance;
    }
}
