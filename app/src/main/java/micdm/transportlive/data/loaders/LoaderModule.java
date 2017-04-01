package micdm.transportlive.data.loaders;

import dagger.Module;
import dagger.Provides;
import micdm.transportlive.AppScope;
import micdm.transportlive.data.loaders.remote.RemoteModule;

@Module(includes = {RemoteModule.class})
public class LoaderModule {

    @Provides
    @AppScope
    Loaders provideLoaders() {
        return new Loaders();
    }
}
