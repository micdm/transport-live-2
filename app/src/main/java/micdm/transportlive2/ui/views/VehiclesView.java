package micdm.transportlive2.ui.views;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collection;

import javax.inject.Inject;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import micdm.transportlive2.ComponentHolder;
import micdm.transportlive2.R;
import micdm.transportlive2.data.loaders.Result;
import micdm.transportlive2.misc.CommonFunctions;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.misc.Irrelevant;
import micdm.transportlive2.models.RouteGroup;
import micdm.transportlive2.ui.PathsPresenter;
import micdm.transportlive2.ui.Presenters;
import micdm.transportlive2.ui.RoutesPresenter;

public class VehiclesView extends PresentedView implements RoutesPresenter.View, PathsPresenter.View {

    @Inject
    CommonFunctions commonFunctions;
    @Inject
    Context context;
    @Inject
    LayoutInflater layoutInflater;
    @Inject
    Presenters presenters;

    @BindView(R.id.v__vehicles__loading)
    LoadingView loadingView;
    @BindView(R.id.v__vehicles__loaded)
    DrawerLayout loadedView;
    @BindView(R.id.v__vehicles__content)
    ViewGroup contentView;
    @BindView(R.id.v__vehicles__map)
    CustomMapView mapView;
    @BindView(R.id.v__vehicles__main_toolbar)
    MainToolbarView mainToolbarView;
    @BindView(R.id.v__vehicles__selected_stations)
    SelectedStationsView selectedStationsView;
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
    void setupViews() {
        loadingView.setVisibility(View.GONE);
        loadedView.setVisibility(View.GONE);
        mainToolbarView.addToggle(loadedView);
        cannotLoadView.setVisibility(View.GONE);
        aboutView.setVisibility(View.GONE);
    }

    @Override
    void attachToPresenters() {
        presenters.getRoutesPresenter().attach(this);
        presenters.getPathsPresenter().attach(this);
    }

    @Override
    void detachFromPresenters() {
        presenters.getRoutesPresenter().detach(this);
        presenters.getPathsPresenter().detach(this);
    }

    @Override
    Disposable subscribeForEvents() {
        return new CompositeDisposable(
            subscribeForForecast(),
            subscribeForAbout(),
            subscribeForRequiredData()
        );
    }

    private Disposable subscribeForForecast() {
        Observable<ForecastView> common =
            Observable
                .merge(
                    mapView.getSelectStationRequests(),
                    selectedStationsView.getSelectStationRequests()
                )
                .map(stationId -> {
                    ForecastView view = (ForecastView) layoutInflater.inflate(R.layout.v__vehicles__forecast, contentView, false);
                    view.setStationId(stationId);
                    return view;
                })
                .share();
        return new CompositeDisposable(
            common.subscribe(view -> {
                loadedView.closeDrawers();
                contentView.addView(view);
            }),
            Observable
                .merge(
                    common.compose(commonFunctions.getPrevious()),
                    common.switchMap(view ->
                        view.getCloseRequests().compose(commonFunctions.toConst(view))
                    )
                )
                .subscribe(contentView::removeView)
        );
    }

    private Disposable subscribeForAbout() {
        return Observable
            .merge(
                mainToolbarView.getGoToAboutRequests().map(o -> View.VISIBLE),
                aboutView.getCloseRequests().map(o -> View.GONE)
            )
            .subscribe(aboutView::setVisibility);
    }

    private Disposable subscribeForRequiredData() {
        Observable<Result<Collection<RouteGroup>>> common = presenters.getRoutesPresenter().getResults().compose(commonFunctions.toMainThread());
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
        return cannotLoadView.getRetryRequest().switchMap(o -> presenters.getPreferencesPresenter().getSelectedRoutes());
    }
}
