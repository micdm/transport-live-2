package micdm.transportlive.ui;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import micdm.transportlive.misc.Irrelevant;
import micdm.transportlive.ui.views.CannotLoadView;
import micdm.transportlive.ui.views.LoadingView;
import micdm.transportlive.ui.views.RoutesView;

public class RoutesController extends BaseController implements RoutesPresenter.View, SelectedRoutesPresenter.View {

    @Inject
    PresenterStore presenterStore;

    @BindView(R.id.v__routes__loading)
    LoadingView loadingView;
    @BindView(R.id.v__routes__routes)
    RoutesView routesView;
    @BindView(R.id.v__routes__cannot_load)
    CannotLoadView cannotLoadView;

    public RoutesController() {
        ComponentHolder.getAppComponent().inject(this);
    }

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
            subscribeForSelectedRoutes()
        );
    }

    private Disposable subscribeForLoadingState() {
        Observable<RoutesLoader.State> states = presenterStore.getRoutesPresenter(this).getLoaderStates().observeOn(AndroidSchedulers.mainThread());
        return new CompositeDisposable(
            states.ofType(StateLoading.class).subscribe(o -> {
                loadingView.setVisibility(View.VISIBLE);
                routesView.setVisibility(View.GONE);
                cannotLoadView.setVisibility(View.GONE);
            }),
            states.ofType(StateSuccess.class).subscribe(state -> {
                loadingView.setVisibility(View.GONE);
                routesView.setVisibility(View.VISIBLE);
                routesView.setRouteGroups(state.routeGroups);
                cannotLoadView.setVisibility(View.GONE);
            }),
            states.ofType(StateFail.class).subscribe(o -> {
                loadingView.setVisibility(View.GONE);
                routesView.setVisibility(View.GONE);
                cannotLoadView.setVisibility(View.VISIBLE);
            })
        );
    }

    private Disposable subscribeForSelectedRoutes() {
        return presenterStore.getSelectedRoutesPresenter(this).getSelectedRoutes().subscribe(routesView::setSelectedRoutes);
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
    public Observable<Collection<String>> getSelectRoutesRequests() {
        return routesView.getSelectedRoutes();
    }
}