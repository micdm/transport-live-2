package micdm.transportlive.ui;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Set;

import javax.inject.Inject;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import micdm.transportlive.misc.Irrelevant;
import micdm.transportlive.R;
import micdm.transportlive.data.Loader;
import micdm.transportlive.ui.views.CannotLoadView;
import micdm.transportlive.ui.views.LoadingView;
import micdm.transportlive.ui.views.RoutesView;

public class RoutesController extends PresentedController implements RoutesPresenter.View, SelectedRoutesPresenter.View {

    @Inject
    PresenterStore presenterStore;

    @BindView(R.id.v__routes__loading)
    LoadingView loadingView;
    @BindView(R.id.v__routes__routes)
    RoutesView routesView;
    @BindView(R.id.v__routes__cannot_load)
    CannotLoadView cannotLoadView;

    @NonNull
    @Override
    View inflateContent(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.c__routes, container, false);
    }

    @Override
    protected void setupViews() {
        loadingView.setVisibility(View.GONE);
        routesView.setVisibility(View.GONE);
        cannotLoadView.setVisibility(View.GONE);
    }

    @Override
    protected Disposable subscribeForEvents() {
        return new CompositeDisposable(
            subscribeForLoadingState(),
            subscribeForRoutes(),
            subscribeForSelectedRoutes()
        );
    }

    private Disposable subscribeForLoadingState() {
        return presenterStore.getRoutesPresenter(this).getLoadingState().subscribe(state -> {
            if (state == Loader.LoadingState.START) {
                loadingView.setVisibility(View.VISIBLE);
                routesView.setVisibility(View.GONE);
                cannotLoadView.setVisibility(View.GONE);
            } else if (state == Loader.LoadingState.SUCCESS) {
                loadingView.setVisibility(View.GONE);
                routesView.setVisibility(View.VISIBLE);
                cannotLoadView.setVisibility(View.GONE);
            } else if (state == Loader.LoadingState.FAIL) {
                loadingView.setVisibility(View.GONE);
                routesView.setVisibility(View.GONE);
                cannotLoadView.setVisibility(View.VISIBLE);
            }
        });
    }

    private Disposable subscribeForRoutes() {
        return presenterStore.getRoutesPresenter(this).getRouteGroups().subscribe(routesView::setRouteGroups);
    }

    private Disposable subscribeForSelectedRoutes() {
        return presenterStore.getSelectedRoutesPresenter(this).getSelectedRoutes().subscribe(routesView::setSelectedRoutes);
    }

    @Override
    public Observable<Object> getLoadDataRequests() {
        return Observable.just(Irrelevant.INSTANCE);
    }

    @Override
    public Observable<Object> getReloadDataRequests() {
        return cannotLoadView.getRetryRequest();
    }

    @Override
    public Observable<Set<String>> getSelectRoutesRequests() {
        return routesView.getSelectedRoutes();
    }
}
