package micdm.transportlive.ui;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jakewharton.rxbinding2.view.RxView;

import javax.inject.Inject;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import micdm.transportlive.R;
import micdm.transportlive.data.Loader;
import micdm.transportlive.misc.Irrelevant;
import micdm.transportlive.ui.views.CannotLoadView;
import micdm.transportlive.ui.views.CustomMapView;
import micdm.transportlive.ui.views.LoadingView;

public class MapController extends PresentedController implements VehiclesPresenter.View {

    @Inject
    PresenterStore presenterStore;

    @BindView(R.id.v__map__loading)
    LoadingView loadingView;
    @BindView(R.id.v__map__map)
    CustomMapView mapView;
    @BindView(R.id.v__map__cannot_load)
    CannotLoadView cannotLoadView;
    @BindView(R.id.v__map__select_routes)
    View selectRoutesView;

    @NonNull
    @Override
    View inflateContent(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.c__map, container, false);
    }

    @Override
    protected Disposable subscribeForEvents() {
        return new CompositeDisposable(
            subscribeForLoadingState(),
            subscribeForRoutes()
        );
    }

    private Disposable subscribeForLoadingState() {
        return presenterStore.getVehiclesPresenter(this).getLoadingState().subscribe(state -> {
            if (state == Loader.LoadingState.START) {
                loadingView.setVisibility(View.VISIBLE);
                mapView.setVisibility(View.GONE);
                cannotLoadView.setVisibility(View.GONE);
            } else if (state == Loader.LoadingState.SUCCESS) {
                loadingView.setVisibility(View.GONE);
                mapView.setVisibility(View.VISIBLE);
                cannotLoadView.setVisibility(View.GONE);
            } else if (state == Loader.LoadingState.FAIL) {
                loadingView.setVisibility(View.GONE);
                mapView.setVisibility(View.GONE);
                cannotLoadView.setVisibility(View.VISIBLE);
            }
        });
    }

    private Disposable subscribeForRoutes() {
        return presenterStore.getVehiclesPresenter(this).getRouteGroups().subscribe(mapView::setVehicles);
    }

    Observable<Object> getGoToSelectRoutesRequests() {
        return RxView.clicks(selectRoutesView);
    }

    @Override
    public Observable<Object> getLoadDataRequests() {
        return Observable.just(Irrelevant.INSTANCE);
    }

    @Override
    public Observable<Object> getReloadDataRequests() {
        return Observable.empty();
    }
}
