package micdm.transportlive2.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;

import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import micdm.transportlive2.ComponentHolder;
import micdm.transportlive2.R;
import micdm.transportlive2.misc.CommonFunctions;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.misc.Irrelevant;
import micdm.transportlive2.models.Forecast;
import micdm.transportlive2.models.Route;
import micdm.transportlive2.models.RouteGroup;
import micdm.transportlive2.ui.ForecastPresenter;
import micdm.transportlive2.ui.Presenters;
import micdm.transportlive2.ui.RoutesPresenter;
import micdm.transportlive2.ui.misc.MiscFunctions;
import micdm.transportlive2.ui.misc.ResultWatcher2;

public class ForecastView extends PresentedView implements RoutesPresenter.View, ForecastPresenter.View {

    static class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        // TODO: нужен один общий класс
        static class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.v__forecast__item__route)
            TextView routeView;
            @BindView(R.id.v__forecast__item__time)
            TextView timeView;

            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        private final LayoutInflater layoutInflater;
        private final MiscFunctions miscFunctions;
        private final Resources resources;

        private List<VehicleInfo> vehicles = Collections.emptyList();

        Adapter(LayoutInflater layoutInflater, MiscFunctions miscFunctions, Resources resources) {
            this.layoutInflater = layoutInflater;
            this.miscFunctions = miscFunctions;
            this.resources = resources;
            setHasStableIds(true);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(layoutInflater.inflate(R.layout.v__forecast__item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            VehicleInfo info = vehicles.get(position);
            holder.routeView.setText(resources.getString(R.string.v__forecast__item__route, miscFunctions.getRouteGroupName(info.group),
                                                         info.route.number()));
            holder.timeView.setText(resources.getString(R.string.v__forecast__item__time, getMinutes(info.vehicle.estimatedTime())));
        }

        private int getMinutes(Duration duration) {
            int minutes = (int) duration.getStandardMinutes();
            return minutes <= 0 ? 1 : minutes;
        }

        @Override
        public int getItemCount() {
            return vehicles.size();
        }

        @Override
        public long getItemId(int position) {
            return vehicles.get(position).vehicle.id().getNumeric();
        }

        void setVehicles(List<VehicleInfo> vehicles) {
            this.vehicles = vehicles;
            notifyDataSetChanged();
        }
    }

    private static final Duration LOAD_FORECAST_INTERVAL = Duration.standardSeconds(10);
    private static final Duration MAX_ARRIVING_TIME = Duration.standardMinutes(30);

    @Inject
    CommonFunctions commonFunctions;
    @Inject
    Context context;
    @Inject
    LayoutInflater layoutInflater;
    @Inject
    MiscFunctions miscFunctions;
    @Inject
    Presenters presenters;
    @Inject
    Resources resources;
    @Inject
    RoutesPresenter routesPresenter;

    @BindView(R.id.v__forecast__name)
    TextView nameView;
    @BindView(R.id.v__forecast__description)
    TextView descriptionView;
    @BindView(R.id.v__forecast__close)
    View closeView;
    @BindView(R.id.v__forecast__vehicles)
    RecyclerView vehiclesView;
    @BindView(R.id.v__forecast__loading)
    View loadingView;
    @BindView(R.id.v__forecast__cannot_load)
    CannotLoadView cannotLoadView;

    private Id stationId;

    public ForecastView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ComponentHolder.getActivityComponent().inject(this);
        }
    }

    public void setStationId(Id stationId) {
        this.stationId = stationId;
    }

    @Override
    void inflateContent(LayoutInflater layoutInflater) {
        layoutInflater.inflate(R.layout.v__forecast, this);
    }

    @Override
    void setupViews() {
        loadingView.setVisibility(GONE);
        descriptionView.setVisibility(GONE);
        vehiclesView.setLayoutManager(new LinearLayoutManager(context));
        vehiclesView.setAdapter(new Adapter(layoutInflater, miscFunctions, resources));
        vehiclesView.setVisibility(GONE);
        cannotLoadView.setVisibility(GONE);
    }

    @Override
    void attachToPresenters() {
        presenters.getForecastPresenter(stationId).attach(this);
    }

    @Override
    void detachFromPresenters() {
        presenters.getForecastPresenter(stationId).detach(this);
    }

    @Override
    Disposable subscribeForEvents() {
        return subscribeForForecast();
    }

    private Disposable subscribeForForecast() {
        ResultWatcher2<Collection<RouteGroup>, Forecast> watcher = new ResultWatcher2<>(
            commonFunctions,
            routesPresenter.getResults(),
            presenters.getForecastPresenter(stationId).getResults()
        );
        return new CompositeDisposable(
            watcher.getLoading()
                .compose(commonFunctions.toMainThread())
                .subscribe(o -> {
                    loadingView.setVisibility(VISIBLE);
                    cannotLoadView.setVisibility(GONE);
                }),
            watcher.getSuccess()
                .compose(commonFunctions.toMainThread())
                .subscribe(product -> {
                    loadingView.setVisibility(GONE);
                    nameView.setText(product.second.name());
                    if (product.second.description().isEmpty()) {
                        descriptionView.setVisibility(GONE);
                    } else {
                        descriptionView.setText(product.second.description());
                        descriptionView.setVisibility(VISIBLE);
                    }
                    ((Adapter) vehiclesView.getAdapter()).setVehicles(getArrivingVehicles(product.first, product.second));
                    vehiclesView.setVisibility(VISIBLE);
                    cannotLoadView.setVisibility(GONE);
                }),
            watcher.getFail()
                .compose(commonFunctions.toMainThread())
                .subscribe(o -> {
                    loadingView.setVisibility(GONE);
                    cannotLoadView.setVisibility(VISIBLE);
                })
        );
    }

    private List<VehicleInfo> getArrivingVehicles(Collection<RouteGroup> groups, Forecast forecast) {
        List<VehicleInfo> result = new ArrayList<>(forecast.vehicles().size());
        for (Forecast.Vehicle vehicle: forecast.vehicles()) {
            if (!vehicle.estimatedTime().isShorterThan(MAX_ARRIVING_TIME)) {
                continue;
            }
            for (RouteGroup group: groups) {
                for (Route route: group.routes()) {
                    if (route.id().equals(vehicle.routeId())) {
                        result.add(new VehicleInfo(group, route, vehicle));
                    }
                }
            }
        }
        Collections.sort(result, (a, b) -> a.vehicle.estimatedTime().compareTo(b.vehicle.estimatedTime()));
        return result;
    }

    @Override
    public Observable<Object> getLoadRoutesRequests() {
        return Observable.just(Irrelevant.INSTANCE);
    }

    @Override
    public Observable<Object> getLoadForecastRequests() {
        return cannotLoadView.getRetryRequest()
            .startWith(Irrelevant.INSTANCE)
            .switchMap(o -> Observable.interval(0, LOAD_FORECAST_INTERVAL.getStandardSeconds(), TimeUnit.SECONDS));
    }

    public Observable<Object> getCloseRequests() {
        return RxView.clicks(closeView);
    }
}
