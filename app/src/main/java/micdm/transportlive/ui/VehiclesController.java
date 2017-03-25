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
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import micdm.transportlive.ComponentHolder;
import micdm.transportlive.R;
import micdm.transportlive.data.BaseLoader.StateFail;
import micdm.transportlive.data.BaseLoader.StateLoading;
import micdm.transportlive.data.BaseLoader.StateSuccess;
import micdm.transportlive.data.RoutesLoader;
import micdm.transportlive.ui.views.CannotLoadView;
import micdm.transportlive.ui.views.CustomMapView;
import micdm.transportlive.ui.views.LoadingView;
import timber.log.Timber;

public class VehiclesController extends BaseController implements VehiclesPresenter.View, SelectedRoutesPresenter.View {

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
            subscribeForLoadingState(),
            subscribeForNavigation()
        );
    }

    private Disposable subscribeForLoadingState() {
        Observable<RoutesLoader.State> states = presenterStore.getVehiclesPresenter(this).getLoaderStates().observeOn(AndroidSchedulers.mainThread());
        return new CompositeDisposable(
            states.ofType(StateLoading.class).subscribe(o -> {
                loadingView.setVisibility(View.VISIBLE);
                mapView.setVisibility(View.GONE);
                cannotLoadView.setVisibility(View.GONE);
            }),
            states.ofType(StateSuccess.class).subscribe(state -> {
                loadingView.setVisibility(View.GONE);
                mapView.setVisibility(View.VISIBLE);
                Timber.d("VEHICLECONTROLLER %s", VehiclesController.this);
                mapView.setVehicles(state.routeGroups);
                cannotLoadView.setVisibility(View.GONE);
            }),
            states.ofType(StateFail.class).subscribe(o -> {
                loadingView.setVisibility(View.GONE);
                mapView.setVisibility(View.GONE);
                cannotLoadView.setVisibility(View.VISIBLE);
            })
        );
    }

    private Disposable subscribeForNavigation() {
        return RxView.clicks(selectRoutesView).subscribe(o ->
            getRouter().pushController(RouterTransaction.with(new RoutesController()))
        );
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
}
