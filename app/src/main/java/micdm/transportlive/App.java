package micdm.transportlive;

import android.app.Application;

import javax.inject.Inject;

import micdm.transportlive.data.PathsStore;
import micdm.transportlive.data.SelectedRoutesStore;
import timber.log.Timber;

public class App extends Application {

    @Inject
    PathsStore pathsStore;
    @Inject
    SelectedRoutesStore selectedRoutesStore;

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
        Timber.plant(new Timber.DebugTree() {
            @Override
            protected String createStackElementTag(StackTraceElement element) {
                return String.format(":o) %s", super.createStackElementTag(element));
            }
        });
    }
}
