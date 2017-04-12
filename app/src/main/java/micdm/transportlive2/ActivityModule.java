package micdm.transportlive2;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;

import dagger.Module;
import dagger.Provides;
import micdm.transportlive2.ui.misc.ActivityLifecycleWatcher;

@Module
class ActivityModule {

    private final Activity activity;

    ActivityModule(Activity activity) {
        this.activity = activity;
    }

    @Provides
    @ActivityScope
    Activity provideActivity() {
        return activity;
    }

    @Provides
    @ActivityScope
    Context provideContext() {
        return activity;
    }

    @Provides
    @ActivityScope
    LayoutInflater provideLayoutInflater(Context context) {
        return (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Provides
    @ActivityScope
    ActivityLifecycleWatcher provideActivityLifecycleWatcher() {
        return new ActivityLifecycleWatcher();
    }
}
