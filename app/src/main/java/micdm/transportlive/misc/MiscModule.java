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
        CommonFunctions instance = new CommonFunctions();
        ComponentHolder.getAppComponent().inject(instance);
        return instance;
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
    ObservableCache provideObservableCache() {
        return new ObservableCache();
    }
}
