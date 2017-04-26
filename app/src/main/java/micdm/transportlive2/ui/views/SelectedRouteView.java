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
import micdm.transportlive2.models.ImmutableRouteInfo;
import micdm.transportlive2.models.Preferences;
import micdm.transportlive2.models.Route;
import micdm.transportlive2.models.RouteGroup;
import micdm.transportlive2.models.Vehicle;
import micdm.transportlive2.ui.presenters.Presenters;
import micdm.transportlive2.ui.misc.ColorConstructor;
import micdm.transportlive2.ui.misc.MiscFunctions;

public class SelectedRouteView extends PresentedView {

    @Inject
    ColorConstructor colorConstructor;
    @Inject
    CommonFunctions commonFunctions;
    @Inject
    MiscFunctions miscFunctions;
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
    @BindView(R.id.v__selected_route__name)
    TextView nameView;
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
    void setupViews() {
        loadingView.setVisibility(GONE);
        cannotLoad.setVisibility(GONE);
    }

    @Override
    Disposable subscribeForEvents() {
        return new CompositeDisposable(
            subscribeForLoadRoutesRequests(),
            subscribeForChangePreferencesRequests(),
            subscribeForRoute(),
            subscribeForVehicles()
        );
    }

    private Disposable subscribeForLoadRoutesRequests() {
        return Observable.just(Irrelevant.INSTANCE)
            .subscribe(o -> presenters.getRoutesPresenter().viewInput.loadRoutes());
    }

    private Disposable subscribeForChangePreferencesRequests() {
        return RxView.clicks(removeView)
            .map(o -> routeId)
            .withLatestFrom(presenters.getPreferencesPresenter().getPreferences(), (routeId, preferences) -> {
                Collection<Id> selectedRoutes = new HashSet<>(preferences.selectedRoutes());
                selectedRoutes.remove(routeId);
                return (Preferences) ImmutablePreferences.builder()
                    .from(preferences)
                    .selectedRoutes(selectedRoutes)
                    .build();
            })
            .subscribe(presenters.getPreferencesPresenter().viewInput::changePreferences);
    }

    private Disposable subscribeForRoute() {
        return presenters.getRoutesPresenter().getResults()
            .filter(Result::isSuccess)
            .map(Result::getData)
            .map(groups -> {
                for (RouteGroup group: groups) {
                    for (Route route: group.routes()) {
                        if (route.id().equals(routeId)) {
                            return ImmutableRouteInfo.builder()
                                .group(group)
                                .route(route)
                                .build();
                        }
                    }
                }
                throw new IllegalStateException(String.format("unknown route %s", routeId));
            })
            .compose(commonFunctions.toMainThread())
            .subscribe(info -> {
                iconView.setColorFilter(colorConstructor.getByString(info.route().id().getOriginal()));
                nameView.setText(resources.getString(R.string.v__selected_route__name, miscFunctions.getRouteGroupName(info.group()), info.route().number()));
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
                    iconView.setVisibility(GONE);
                    loadingView.setVisibility(VISIBLE);
                    cannotLoad.setVisibility(GONE);
                }),
            common
                .filter(Result::isSuccess)
                .subscribe(o -> {
                    iconView.setVisibility(VISIBLE);
                    loadingView.setVisibility(GONE);
                    cannotLoad.setVisibility(GONE);
                }),
            common
                .filter(Result::isFail)
                .subscribe(o -> {
                    iconView.setVisibility(GONE);
                    loadingView.setVisibility(GONE);
                    cannotLoad.setVisibility(VISIBLE);
                })
        );
    }
}
