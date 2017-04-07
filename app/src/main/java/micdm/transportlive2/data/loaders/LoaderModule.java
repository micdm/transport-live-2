package micdm.transportlive2.data.loaders;

import dagger.Module;
import dagger.Provides;
import micdm.transportlive2.AppScope;
import micdm.transportlive2.data.loaders.remote.RemoteModule;

@Module(includes = {RemoteModule.class})
public class LoaderModule {

    @Provides
    @AppScope
    Loaders provideLoaders() {
        return new Loaders();
    }
}
