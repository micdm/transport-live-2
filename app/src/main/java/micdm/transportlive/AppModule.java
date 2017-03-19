package micdm.transportlive;

import android.content.Context;
import android.content.SharedPreferences;

import dagger.Module;
import dagger.Provides;
import micdm.transportlive.misc.CommonFunctions;

@Module
class AppModule {

    private static final String SHARED_PREFERENCES_NAME = "global";

    private final App app;

    AppModule(App app) {
        this.app = app;
    }

    @Provides
    @AppScope
    App provideApp() {
        return app;
    }

    @Provides
    @AppScope
    SharedPreferences provideSharedPreferences() {
        return app.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @Provides
    @AppScope
    CommonFunctions provideCommonFunctions() {
        return new CommonFunctions();
    }
}
