package micdm.transportlive.ui;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.RouterTransaction;
import com.jakewharton.rxbinding2.view.RxView;

import org.joda.time.Duration;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import micdm.transportlive.ComponentHolder;
import micdm.transportlive.R;
import micdm.transportlive.data.loaders.Result;
import micdm.transportlive.misc.CommonFunctions;
import micdm.transportlive.misc.Irrelevant;
import micdm.transportlive.models.Path;
import micdm.transportlive.models.RouteGroup;
import micdm.transportlive.models.Vehicle;
import micdm.transportlive.ui.misc.ActivityLifecycleWatcher;
import micdm.transportlive.ui.misc.ActivityLifecycleWatcher.Stage;
import micdm.transportlive.ui.views.CannotLoadView;
import micdm.transportlive.ui.views.CustomMapView;
import micdm.transportlive.ui.views.LoadingView;

public class VehiclesController extends BaseController implements RoutesPresenter.View, VehiclesPresenter.View, SelectedRoutesPresenter.View, PathsPresenter.View {

    private static final Duration LOAD_VEHICLES_INTERVAL = Duration.standardSeconds(10);

    @Inject
    ActivityLifecycleWatcher activityLifecycleWatcher;
    @Inject
    CommonFunctions commonFunctions;
    @Inject
    PresenterStore presenterStore;

    @BindView(R.id.v__vehicles__loading)
    LoadingView loadingView;
    @BindView(R.id.v__vehicles__map)
    CustomMapView mapView;
    @BindView(R.id.v__vehicles__cannot_load)
    CannotLoadView cannotLoadView;
    @BindView(R.id.v__vehicles__select_routes)
    View selectRoutesView;

    public VehiclesController() {
        ComponentHolder.getActivityComponent().inject(this);
    }

    @NonNull
    @Override
    View inflateContent(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.c__vehicles, container, false);
    }

    @Override
    protected void setupViews() {
        loadingView.setVisibility(View.GONE);
        mapView.setVisibility(View.GONE);
        cannotLoadView.setVisibility(View.GONE);
    }

    @Override
    protected Disposable subscribeForEvents() {
        return new CompositeDisposable(
            subscribeForRequiredData(),
            subscribeForPaths(),
            subscribeForVehicles(),
            subscribeForNavigation()
        );
    }

    private Disposable subscribeForRequiredData() {
        Observable<Result<Collection<RouteGroup>>> common =
            presenterStore.getRoutesPresenter(this).getResults()
                .compose(commonFunctions.toMainThread());
        return new CompositeDisposable(
            common.filter(Result::isLoading).subscribe(o -> {
                loadingView.setVisibility(View.VISIBLE);
                mapView.setVisibility(View.GONE);
                cannotLoadView.setVisibility(View.GONE);
            }),
            common.filter(Result::isSuccess).map(Result::getData).subscribe(groups -> {
                loadingView.setVisibility(View.GONE);
                mapView.setVisibility(View.VISIBLE);
                mapView.setRoutes(groups);
                cannotLoadView.setVisibility(View.GONE);
            }),
            common.filter(Result::isFail).subscribe(o -> {
                loadingView.setVisibility(View.GONE);
                mapView.setVisibility(View.GONE);
                cannotLoadView.setVisibility(View.VISIBLE);
            })
        );
    }

    private Disposable subscribeForPaths() {
        Observable<Result<Collection<Path>>> common =
            presenterStore.getPathsPresenter(this).getResults()
                .compose(commonFunctions.toMainThread());
        return new CompositeDisposable(
            common
                .filter(Result::isSuccess)
                .map(Result::getData)
                .subscribe(mapView::setPaths)
        );
    }

    private Disposable subscribeForVehicles() {
        Observable<Result<Collection<Vehicle>>> common =
            presenterStore.getVehiclesPresenter(this).getResults()
                .compose(commonFunctions.toMainThread());
        return new CompositeDisposable(
            common
                .filter(Result::isSuccess)
                .map(Result::getData)
                .subscribe(mapView::setVehicles)
        );
    }

    private Disposable subscribeForNavigation() {
        return RxView.clicks(selectRoutesView).subscribe(o ->
            getRouter().pushController(RouterTransaction.with(new RoutesController()))
        );
    }

    @Override
    public Observable<Object> getLoadRoutesRequests() {
        return cannotLoadView.getRetryRequest().startWith(Irrelevant.INSTANCE);
    }

    @Override
    public Observable<Collection<String>> getLoadVehiclesRequests() {
        return activityLifecycleWatcher.getState(Stage.RESUME, true)
            .switchMap(o ->
                presenterStore.getSelectedRoutesPresenter(this).getSelectedRoutes()
            )
            .switchMap(routeIds ->
                Observable
                    .interval(0, LOAD_VEHICLES_INTERVAL.getStandardSeconds(), TimeUnit.SECONDS)
                    .compose(commonFunctions.toConst(routeIds))
                    .takeUntil(activityLifecycleWatcher.getState(Stage.PAUSE, true))
            );
    }

    @Override
    public Observable<Collection<String>> getSelectRoutesRequests() {
        return Observable.empty();
    }

    @Override
    public Observable<Collection<String>> getLoadPathsRequests() {
        return cannotLoadView.getRetryRequest()
            .startWith(Irrelevant.INSTANCE)
            .switchMap(o -> presenterStore.getSelectedRoutesPresenter(this).getSelectedRoutes());
    }
}
