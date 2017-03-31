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
import micdm.transportlive.data.BaseLoader;
import micdm.transportlive.misc.Irrelevant;
import micdm.transportlive.models.Path;
import micdm.transportlive.models.RouteGroup;
import micdm.transportlive.models.Vehicle;
import micdm.transportlive.ui.views.CannotLoadView;
import micdm.transportlive.ui.views.CustomMapView;
import micdm.transportlive.ui.views.LoadingView;

public class VehiclesController extends BaseController implements RoutesPresenter.View, VehiclesPresenter.View, SelectedRoutesPresenter.View, PathsPresenter.View {

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
            subscribeForData(),
            subscribeForNavigation()
        );
    }

    private static class Watcher<T1, T2, T3> {

        private static class Result<T1, T2, T3> {

            private final T1 first;
            private final T2 second;
            private final T3 third;

            private Result(T1 first, T2 second, T3 third) {
                this.first = first;
                this.second = second;
                this.third = third;
            }
        }

        private final Observable<BaseLoader.Result<T1>> first;
        private final Observable<BaseLoader.Result<T2>> second;
        private final Observable<BaseLoader.Result<T3>> third;

        private Watcher(Observable<BaseLoader.Result<T1>> first, Observable<BaseLoader.Result<T2>> second, Observable<BaseLoader.Result<T3>> third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }

        private Observable<Object> getLoading() {
            return Observable
                .combineLatest(
                    first,
                    second,
                    third,
                    (a, b, c) -> a.isLoading() || b.isLoading() || c.isLoading()
                )
                .filter(value -> value)
                .map(o -> Irrelevant.INSTANCE);
        }

        private Observable<Result<T1, T2, T3>> getSuccess() {
            return Observable.combineLatest(
                first.filter(BaseLoader.Result::isSuccess),
                second.filter(BaseLoader.Result::isSuccess),
                third.filter(BaseLoader.Result::isSuccess),
                (a, b, c) -> new Result<>(a.getData(), b.getData(), c.getData())
            );
        }

        private Observable<Object> getFail() {
            return Observable
                .combineLatest(
                    first,
                    second,
                    third,
                    (a, b, c) -> a.isFail() || b.isFail() || c.isFail()
                )
                .filter(value -> value)
                .map(o -> Irrelevant.INSTANCE);
        }
    }

    private Disposable subscribeForData() {
        Watcher<Collection<RouteGroup>, Collection<Vehicle>, Collection<Path>> watcher = new Watcher<>(
            presenterStore.getRoutesPresenter(this).getResults()
                .observeOn(AndroidSchedulers.mainThread()),
            presenterStore.getVehiclesPresenter(this).getResults()
                .observeOn(AndroidSchedulers.mainThread()),
            presenterStore.getPathsPresenter(this).getResults()
                .observeOn(AndroidSchedulers.mainThread())

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
                mapView.setVehicles(result.second);
                mapView.setPaths(result.third);
                cannotLoadView.setVisibility(View.GONE);
            }),
            watcher.getFail().subscribe(o -> {
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
