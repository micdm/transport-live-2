package micdm.transportlive2;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import micdm.transportlive2.misc.AnalyticsTracker;
import micdm.transportlive2.ui.misc.ActivityLifecycleWatcher;
import micdm.transportlive2.ui.misc.ActivityLifecycleWatcher.Stage;
import micdm.transportlive2.ui.misc.PermissionChecker;

public class MainActivity extends AppCompatActivity {

    @Inject
    ActivityLifecycleWatcher activityLifecycleWatcher;
    @Inject
    AnalyticsTracker analyticsTracker;
    @Inject
    PermissionChecker permissionChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActivityComponent();
        ComponentHolder.getActivityComponent().inject(this);
        activityLifecycleWatcher.setState(Stage.CREATE, savedInstanceState);
        setContentView(R.layout.a__main);
    }

    private void setupActivityComponent() {
        ComponentHolder.setActivityComponent(
            ComponentHolder.getAppComponent().activityComponentBuilder()
                .activityModule(new ActivityModule(this))
                .build()
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        activityLifecycleWatcher.setState(Stage.START);
        analyticsTracker.trackActivityStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityLifecycleWatcher.setState(Stage.PAUSE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityLifecycleWatcher.setState(Stage.RESUME);
    }

    @Override
    protected void onStop() {
        super.onStop();
        activityLifecycleWatcher.setState(Stage.STOP);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        activityLifecycleWatcher.setState(Stage.SAVE_STATE, outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        activityLifecycleWatcher.setState(Stage.LOW_MEMORY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissionChecker.isResultAcceptable(requestCode)) {
            permissionChecker.setResults(permissions, grantResults);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityLifecycleWatcher.setState(Stage.DESTROY);
        ComponentHolder.resetActivityComponent();
    }
}
