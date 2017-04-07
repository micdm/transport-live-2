package micdm.transportlive2;

import android.app.Application;

import javax.inject.Inject;

import micdm.transportlive2.data.stores.PathsStore;
import micdm.transportlive2.data.stores.SelectedRoutesStore;
import micdm.transportlive2.misc.TimberTree;
import timber.log.Timber;

public class App extends Application {

    @Inject
    PathsStore pathsStore;
    @Inject
    SelectedRoutesStore selectedRoutesStore;
    @Inject
    TimberTree timberTree;

    @Override
    public void onCreate() {
        super.onCreate();
        setupAppComponent();
        ComponentHolder.getAppComponent().inject(this);
        setupLogging();
    }

    private void setupAppComponent() {
        ComponentHolder.setAppComponent(
            DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build()
        );
    }

    private void setupLogging() {
        Timber.plant(timberTree);
    }
}
