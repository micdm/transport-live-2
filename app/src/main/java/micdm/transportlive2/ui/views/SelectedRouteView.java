package micdm.transportlive2.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;

import java.util.Collection;
import java.util.HashSet;

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
import micdm.transportlive2.misc.ObservableCache;
import micdm.transportlive2.models.ImmutablePreferences;
import micdm.transportlive2.models.Preferences;
import micdm.transportlive2.models.Route;
import micdm.transportlive2.models.RouteGroup;
import micdm.transportlive2.models.Vehicle;
import micdm.transportlive2.ui.PreferencesPresenter;
import micdm.transportlive2.ui.Presenters;
import micdm.transportlive2.ui.RoutesPresenter;
import micdm.transportlive2.ui.misc.ColorConstructor;

public class SelectedRouteView extends PresentedView implements RoutesPresenter.View, PreferencesPresenter.View {

    @Inject
    ColorConstructor colorConstructor;
    @Inject
    CommonFunctions commonFunctions;
    @Inject
    ObservableCache observableCache;
    @Inject
    Presenters presenters;
    @Inject
    Resources resources;

    @BindView(R.id.v__selected_route__icon)
    ImageView iconView;
    @BindView(R.id.v__selected_route__loading)
    View loadingView;
    @BindView(R.id.v__selected_route__cannot_load)
    View cannotLoad;
    @BindView(R.id.v__selected_route__number)
    TextView numberView;
    @BindView(R.id.v__selected_route__extra)
    View extraView;
    @BindView(R.id.v__selected_route__remove)
    View removeView;

    private Id routeId;

    public SelectedRouteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ComponentHolder.getActivityComponent().inject(this);
        }
    }

    void setRouteId(Id id) {
        routeId = id;
    }

    @Override
    void inflateContent(LayoutInflater layoutInflater) {
        layoutInflater.inflate(R.layout.v__selected_route, this);
    }

    @Override
    void attachToPresenters() {
        presenters.getRoutesPresenter().attach(this);
        presenters.getPreferencesPresenter().attach(this);
    }

    @Override
    void detachFromPresenters() {
        presenters.getRoutesPresenter().detach(this);
        presenters.getPreferencesPresenter().detach(this);
    }

    @Override
    void setupViews() {
        loadingView.setVisibility(GONE);
        cannotLoad.setVisibility(GONE);
    }

    @Override
    Disposable subscribeForEvents() {
        return new CompositeDisposable(
            subscribeForRoute(),
            subscribeForVehicles()
        );
    }

    private Disposable subscribeForRoute() {
        return presenters.getRoutesPresenter().getResults()
            .filter(Result::isSuccess)
            .map(Result::getData)
            .map(groups -> {
                for (RouteGroup group: groups) {
                    for (Route route: group.routes()) {
                        if (route.id().equals(routeId)) {
                            return new RouteInfo(group, route);
                        }
                    }
                }
                throw new IllegalStateException(String.format("unknown route %s", routeId));
            })
            .compose(commonFunctions.toMainThread())
            .subscribe(info -> {
                setBackgroundColor(colorConstructor.getByString(info.route.id().getOriginal()));
                if (info.group.type() == RouteGroup.Type.TROLLEYBUS) {
                    iconView.setImageDrawable(resources.getDrawable(R.drawable.ic_trolleybus));
                }
                if (info.group.type() == RouteGroup.Type.TRAM) {
                    iconView.setImageDrawable(resources.getDrawable(R.drawable.ic_tram));
                }
                if (info.group.type() == RouteGroup.Type.BUS) {
                    iconView.setImageDrawable(resources.getDrawable(R.drawable.ic_bus));
                }
                numberView.setText(info.route.number());
            });
    }

    private Disposable subscribeForVehicles() {
        Observable<Result<Collection<Vehicle>>> common = presenters.getVehiclesPresenter(routeId).getResults()
            .compose(commonFunctions.minDelay())
            .compose(commonFunctions.toMainThread())
            .share();
        return new CompositeDisposable(
            common
                .filter(Result::isLoading)
                .subscribe(o -> {
                    loadingView.setVisibility(VISIBLE);
                    cannotLoad.setVisibility(GONE);
                }),
            common
                .filter(Result::isSuccess)
                .subscribe(o -> {
                    loadingView.setVisibility(GONE);
                    cannotLoad.setVisibility(GONE);
                }),
            common
                .filter(Result::isFail)
                .subscribe(o -> {
                    loadingView.setVisibility(GONE);
                    cannotLoad.setVisibility(VISIBLE);
                })
        );
    }

    @Override
    public Observable<Object> getLoadRoutesRequests() {
        return Observable.just(Irrelevant.INSTANCE);
    }

    @Override
    public Observable<Preferences> getChangePreferencesRequests() {
        return observableCache.get("getChangePreferencesRequests", () ->
            RxView.clicks(removeView)
                .map(o -> routeId)
                .withLatestFrom(presenters.getPreferencesPresenter().getPreferences(), (routeId, preferences) -> {
                    Collection<Id> selectedRoutes = new HashSet<>(preferences.selectedRoutes());
                    selectedRoutes.remove(routeId);
                    return (Preferences) ImmutablePreferences.builder()
                        .from(preferences)
                        .selectedRoutes(selectedRoutes)
                        .build();
                })
                .share()
        );
    }
}
