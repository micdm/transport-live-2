package micdm.transportlive;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;

import javax.inject.Inject;

import micdm.transportlive.ui.VehiclesController;
import micdm.transportlive.ui.misc.ActivityLifecycleWatcher;
import micdm.transportlive.ui.misc.ActivityLifecycleWatcher.Stage;

public class MainActivity extends AppCompatActivity {

    @Inject
    ActivityLifecycleWatcher activityLifecycleWatcher;

    private Router router;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActivityComponent();
        ComponentHolder.getActivityComponent().inject(this);
        activityLifecycleWatcher.setState(Stage.CREATE, savedInstanceState);
        setContentView(R.layout.a__main);
        router = initRouter(savedInstanceState);
    }

    private void setupActivityComponent() {
        ComponentHolder.setActivityComponent(
            ComponentHolder.getAppComponent().activityComponentBuilder()
                .activityModule(new ActivityModule(this))
                .build()
        );
    }

    private Router initRouter(Bundle savedInstanceState) {
        Router router = Conductor.attachRouter(this, (ViewGroup) findViewById(R.id.v__main__container), savedInstanceState);
        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(new VehiclesController()));
        }
        return router;
    }

    @Override
    protected void onStart() {
        super.onStart();
        activityLifecycleWatcher.setState(Stage.START);
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
    public void onBackPressed() {
        if (!router.handleBack()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityLifecycleWatcher.setState(Stage.DESTROY);
        ComponentHolder.resetActivityComponent();
    }
}
