package micdm.transportlive.misc;

import dagger.Module;
import dagger.Provides;
import micdm.transportlive.AppScope;
import micdm.transportlive.ComponentHolder;

@Module
public class MiscModule {

    @Provides
    @AppScope
    CommonFunctions provideCommonFunctions() {
        return new CommonFunctions();
    }

    @Provides
    @AppScope
    Cache provideCache() {
        Cache instance = new Cache();
        ComponentHolder.getAppComponent().inject(instance);
        instance.init();
        return instance;
    }

    @Provides
    @AppScope
    ObservableCache provideObservableCache() {
        return new ObservableCache();
    }
}
