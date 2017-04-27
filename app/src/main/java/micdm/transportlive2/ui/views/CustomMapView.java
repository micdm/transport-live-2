package micdm.transportlive2.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import org.joda.time.Duration;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import micdm.transportlive2.ComponentHolder;
import micdm.transportlive2.R;
import micdm.transportlive2.data.loaders.Result;
import micdm.transportlive2.misc.CommonFunctions;
import micdm.transportlive2.misc.Irrelevant;
import micdm.transportlive2.models.Path;
import micdm.transportlive2.models.Station;
import micdm.transportlive2.models.Vehicle;
import micdm.transportlive2.ui.misc.ActivityLifecycleWatcher;
import micdm.transportlive2.ui.misc.ActivityLifecycleWatcher.Stage;
import micdm.transportlive2.ui.presenters.Presenters;

public class CustomMapView extends PresentedView {

    private static final Duration LOAD_VEHICLES_INTERVAL = Duration.standardSeconds(10);
    private static final int MAX_ROUTE_COUNT_WITH_NO_PENALTY = 3;
    private static final Duration LOAD_VEHICLES_PENALTY_INTERVAL = Duration.standardSeconds(5);

    @Inject
    ActivityLifecycleWatcher activityLifecycleWatcher;
    @Inject
    CommonFunctions commonFunctions;
    @Inject
    Presenters presenters;

    @BindView(R.id.v__custom_map__no_vehicles)
    View noVehiclesView;
    @BindView(R.id.v__custom_map__map)
    MapWrapperView mapWrapperView;

    public CustomMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ComponentHolder.getActivityComponent().inject(this);
        }
    }

    @Override
    void inflateContent(LayoutInflater layoutInflater) {
        layoutInflater.inflate(R.layout.v__custom_map, this);
    }

    @Override
    void setupViews() {
        noVehiclesView.setVisibility(GONE);
    }

    @Override
    Disposable subscribeForEvents() {
        return new CompositeDisposable(
            subscribeForLoadRoutesRequests(),
            subscribeForLoadPathsRequests(),
            subscribeForLoadVehiclesRequests(),
            subscribeForSetCurrentStationRequests(),
            subscribeForChangePreferencesRequests(),
            subscribeForRoutes(),
            subscribeForVehicles(),
            subscribeForPaths()
        );
    }

    private Disposable subscribeForLoadRoutesRequests() {
        return Observable.just(Irrelevant.INSTANCE)
            .subscribe(o -> presenters.getRoutesPresenter().viewInput.loadRoutes.call());
    }

    private Disposable subscribeForLoadPathsRequests() {
        return presenters.getPreferencesPresenter().getSelectedRoutes()
            .subscribe(presenters.getPathsPresenter().viewInput.routes::set);
    }

    private Disposable subscribeForLoadVehiclesRequests() {
        return activityLifecycleWatcher.getState(Stage.RESUME, true)
            .switchMap(o -> presenters.getPreferencesPresenter().getSelectedRoutes())
            .switchMap(routeIds -> {
                Duration interval;
                if (routeIds.size() > MAX_ROUTE_COUNT_WITH_NO_PENALTY) {
                    interval = LOAD_VEHICLES_PENALTY_INTERVAL.multipliedBy(routeIds.size());
                } else {
                    interval = LOAD_VEHICLES_INTERVAL;
                }
                return Observable
                    .interval(0, interval.getStandardSeconds(), TimeUnit.SECONDS)
                    .compose(commonFunctions.toConst(routeIds))
                    .takeUntil(activityLifecycleWatcher.getState(Stage.PAUSE, true));
            })
            .subscribe(presenters.getAllVehiclesPresenter().viewInput.routes::set);
    }

    private Disposable subscribeForSetCurrentStationRequests() {
        return mapWrapperView.getCurrentStation()
            .subscribe(presenters.getCurrentStationPresenter().viewInput.currentStation::set);
    }

    private Disposable subscribeForChangePreferencesRequests() {
        return mapWrapperView.getPreferences()
            .subscribe(presenters.getPreferencesPresenter().viewInput.preferences::set);
    }

    private Disposable subscribeForRoutes() {
        return presenters.getRoutesPresenter().getResults()
            .filter(Result::isSuccess)
            .map(Result::getData)
            .distinctUntilChanged()
            .subscribe(mapWrapperView::setRoutes);
    }

    private Disposable subscribeForVehicles() {
        return new CompositeDisposable(
            Observable
                .merge(
                    presenters.getAllVehiclesPresenter().getResults()
                        .filter(Result::isSuccess)
                        .map(Result::getData),
                    presenters.getPreferencesPresenter().getSelectedRoutes()
                        .filter(Collection::isEmpty)
                        .map(o -> Collections.<Vehicle>emptyList())
                )
                .distinctUntilChanged()
                .subscribe(mapWrapperView::setVehicles),
            Observable
                .combineLatest(
                    presenters.getAllVehiclesPresenter().getResults()
                        .filter(Result::isSuccess)
                        .map(Result::getData),
                    presenters.getPreferencesPresenter().getSelectedRoutes(),
                    (vehicles, routeIds) -> vehicles.isEmpty() && !routeIds.isEmpty()
                )
                .compose(commonFunctions.toMainThread())
                .subscribe(isEmpty -> noVehiclesView.setVisibility(isEmpty ? VISIBLE : GONE))
        );
    }

    private Disposable subscribeForPaths() {
        Observable<Collection<Path>> common =
            Observable
                .merge(
                    presenters.getPathsPresenter().getResults()
                        .filter(Result::isSuccess)
                        .map(Result::getData),
                    presenters.getPreferencesPresenter().getSelectedRoutes()
                        .filter(Collection::isEmpty)
                        .map(o -> Collections.<Path>emptyList())
                )
                .distinctUntilChanged();
        return new CompositeDisposable(
            common
                .compose(commonFunctions.toMainThread())
                .subscribe(mapWrapperView::setPaths),
            common
                .map(paths -> {
                    Collection<Station> stations = new HashSet<>();
                    for (Path path: paths) {
                        stations.addAll(path.stations());
                    }
                    return stations;
                })
                .compose(commonFunctions.toMainThread())
                .subscribe(mapWrapperView::setStations)
        );
    }
}
