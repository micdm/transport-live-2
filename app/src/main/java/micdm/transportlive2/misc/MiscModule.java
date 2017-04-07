package micdm.transportlive2.misc;

import dagger.Module;
import dagger.Provides;
import micdm.transportlive2.App;
import micdm.transportlive2.AppScope;
import micdm.transportlive2.ComponentHolder;

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
    @AppScope
    IdFactory provideIdFactory() {
        return new IdFactory();
    }

    @Provides
    ObservableCache provideObservableCache() {
        return new ObservableCache();
    }

    @Provides
    @AppScope
    AnalyticsTracker provideAnalyticsTracker(App app) {
        return new AnalyticsTracker(app);
    }

    @Provides
    @AppScope
    TimberTree provideTimberTree() {
        return new TimberTree();
    }
}
