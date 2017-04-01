package micdm.transportlive.ui;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.RouterTransaction;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.Collection;

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
import micdm.transportlive.ui.misc.ResultWatcher2;
import micdm.transportlive.ui.views.CannotLoadView;
import micdm.transportlive.ui.views.CustomMapView;
import micdm.transportlive.ui.views.LoadingView;

public class VehiclesController extends BaseController implements RoutesPresenter.View, VehiclesPresenter.View, SelectedRoutesPresenter.View, PathsPresenter.View {

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
        ComponentHolder.getAppComponent().inject(this);
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
            subscribeForVehicles(),
            subscribeForNavigation()
        );
    }

    private Disposable subscribeForRequiredData() {
        ResultWatcher2<Collection<RouteGroup>, Collection<Path>> watcher = new ResultWatcher2<>(
            commonFunctions,
            presenterStore.getRoutesPresenter(this).getResults()
                .compose(commonFunctions.toMainThread()),
            presenterStore.getPathsPresenter(this).getResults()
                .compose(commonFunctions.toMainThread())
        );
        return new CompositeDisposable(
            watcher.getLoading().subscribe(o -> {
                loadingView.setVisibility(View.VISIBLE);
                mapView.setVisibility(View.GONE);
                cannotLoadView.setVisibility(View.GONE);
            }),
            watcher.getSuccess().subscribe(result -> {
                loadingView.setVisibility(View.GONE);
                mapView.setVisibility(View.VISIBLE);
                mapView.setRoutes(result.first);
                mapView.setPaths(result.second);
                cannotLoadView.setVisibility(View.GONE);
            }),
            watcher.getFail().subscribe(o -> {
                loadingView.setVisibility(View.GONE);
                mapView.setVisibility(View.GONE);
                cannotLoadView.setVisibility(View.VISIBLE);
            })
        );
    }

    private Disposable subscribeForVehicles() {
        Observable<Result<Collection<Vehicle>>> common =
            presenterStore.getVehiclesPresenter(this).getResults()
                .compose(commonFunctions.toMainThread());
        return new CompositeDisposable(
            common.filter(Result::isSuccess).map(Result::getData).subscribe(mapView::setVehicles)
        );
    }

    private Disposable subscribeForNavigation() {
        return RxView.clicks(selectRoutesView).subscribe(o ->
            getRouter().pushController(RouterTransaction.with(new RoutesController()))
        );
    }

    @Override
    public Observable<Object> getLoadRoutesRequests() {
        return Observable.just(Irrelevant.INSTANCE);
    }

    @Override
    public Observable<Object> getReloadRoutesRequests() {
        return cannotLoadView.getRetryRequest();
    }

    @Override
    public Observable<Collection<String>> getLoadVehiclesRequests() {
        return presenterStore.getSelectedRoutesPresenter(this).getSelectedRoutes();
    }

    @Override
    public Observable<Collection<String>> getReloadVehiclesRequests() {
        return cannotLoadView.getRetryRequest().withLatestFrom(
            presenterStore.getSelectedRoutesPresenter(this).getSelectedRoutes(),
            (o, routes) -> routes
        );
    }

    @Override
    public Observable<Collection<String>> getSelectRoutesRequests() {
        return Observable.empty();
    }

    @Override
    public Observable<Collection<String>> getLoadPathsRequests() {
        return presenterStore.getSelectedRoutesPresenter(this).getSelectedRoutes();
    }

    @Override
    public Observable<Collection<String>> getReloadPathsRequests() {
        return cannotLoadView.getRetryRequest().withLatestFrom(
            presenterStore.getSelectedRoutesPresenter(this).getSelectedRoutes(),
            (o, routes) -> routes
        );
    }
}
