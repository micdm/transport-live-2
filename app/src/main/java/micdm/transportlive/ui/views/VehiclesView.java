package micdm.transportlive.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

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
import micdm.transportlive.misc.Id;
import micdm.transportlive.misc.Irrelevant;
import micdm.transportlive.models.RouteGroup;
import micdm.transportlive.ui.PathsPresenter;
import micdm.transportlive.ui.RoutesPresenter;
import micdm.transportlive.ui.SelectedRoutesPresenter;

public class VehiclesView extends PresentedView implements RoutesPresenter.View, PathsPresenter.View {

    @Inject
    CommonFunctions commonFunctions;
    @Inject
    PathsPresenter pathsPresenter;
    @Inject
    RoutesPresenter routesPresenter;
    @Inject
    SelectedRoutesPresenter selectedRoutesPresenter;

    @BindView(R.id.v__vehicles__loading)
    LoadingView loadingView;
    @BindView(R.id.v__vehicles__loaded)
    View loadedView;
    @BindView(R.id.v__vehicles__search_route)
    SearchRouteView searchRouteView;
    @BindView(R.id.v__vehicles__cannot_load)
    CannotLoadView cannotLoadView;
    @BindView(R.id.v__vehicles__about)
    AboutView aboutView;

    public VehiclesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ComponentHolder.getActivityComponent().inject(this);
        }
    }

    @Override
    void inflateContent(LayoutInflater layoutInflater) {
        layoutInflater.inflate(R.layout.v__vehicles, this);
    }

    @Override
    protected void setupViews() {
        loadingView.setVisibility(View.GONE);
        loadedView.setVisibility(View.GONE);
        cannotLoadView.setVisibility(View.GONE);
        aboutView.setVisibility(View.GONE);
    }

    @Override
    void attachToPresenters() {
        routesPresenter.attach(this);
        pathsPresenter.attach(this);
    }

    @Override
    void detachFromPresenters() {
        routesPresenter.detach(this);
        pathsPresenter.detach(this);
    }

    @Override
    protected Disposable subscribeForEvents() {
        return new CompositeDisposable(
            subscribeForAbout(),
            subscribeForRequiredData()
        );
    }

    private Disposable subscribeForAbout() {
        return Observable
            .merge(
                searchRouteView.getGoToAboutRequests().map(o -> View.VISIBLE),
                aboutView.getCloseRequests().map(o -> View.GONE)
            )
            .subscribe(aboutView::setVisibility);
    }

    private Disposable subscribeForRequiredData() {
        Observable<Result<Collection<RouteGroup>>> common = routesPresenter.getResults().compose(commonFunctions.toMainThread());
        return new CompositeDisposable(
            common.filter(Result::isLoading).subscribe(o -> {
                loadingView.setVisibility(View.VISIBLE);
                loadedView.setVisibility(View.GONE);
                cannotLoadView.setVisibility(View.GONE);
            }),
            common.filter(Result::isSuccess).map(Result::getData).subscribe(groups -> {
                loadingView.setVisibility(View.GONE);
                loadedView.setVisibility(View.VISIBLE);
                cannotLoadView.setVisibility(View.GONE);
            }),
            common.filter(Result::isFail).subscribe(o -> {
                loadingView.setVisibility(View.GONE);
                loadedView.setVisibility(View.GONE);
                cannotLoadView.setVisibility(View.VISIBLE);
            })
        );
    }

    @Override
    public Observable<Object> getLoadRoutesRequests() {
        return cannotLoadView.getRetryRequest().startWith(Irrelevant.INSTANCE);
    }

    @Override
    public Observable<Collection<Id>> getLoadPathsRequests() {
        return cannotLoadView.getRetryRequest().switchMap(o -> selectedRoutesPresenter.getSelectedRoutes());
    }
}
