package micdm.transportlive2.ui.views;

import android.Manifest;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.javatuples.Pair;
import org.joda.time.Duration;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.misc.Irrelevant;
import micdm.transportlive2.misc.ObservableCache;
import micdm.transportlive2.models.Path;
import micdm.transportlive2.models.Point;
import micdm.transportlive2.models.Route;
import micdm.transportlive2.models.RouteGroup;
import micdm.transportlive2.models.Vehicle;
import micdm.transportlive2.ui.PathsPresenter;
import micdm.transportlive2.ui.RoutesPresenter;
import micdm.transportlive2.ui.SelectedRoutesPresenter;
import micdm.transportlive2.ui.VehiclesPresenter;
import micdm.transportlive2.ui.misc.ActivityLifecycleWatcher;
import micdm.transportlive2.ui.misc.ActivityLifecycleWatcher.Stage;
import micdm.transportlive2.ui.misc.ColorConstructor;
import micdm.transportlive2.ui.misc.MarkerIconBuilder;
import micdm.transportlive2.ui.misc.PermissionChecker;

public class CustomMapView extends PresentedView implements RoutesPresenter.View, PathsPresenter.View, VehiclesPresenter.View {

    private static class VehicleMarkerHandler {

        private static class VehicleMarker {

            Vehicle vehicle;
            Marker marker;
            MarkerIconBuilder.BitmapWrapper bitmapWrapper;

            private VehicleMarker(Vehicle vehicle, Marker marker) {
                this.vehicle = vehicle;
                this.marker = marker;
            }
        }

        private final MarkerIconBuilder markerIconBuilder;
        private final GoogleMap map;
        private Map<Id, VehicleMarker> markers = new HashMap<>();

        VehicleMarkerHandler(MarkerIconBuilder markerIconBuilder, GoogleMap map) {
            this.markerIconBuilder = markerIconBuilder;
            this.map = map;
        }

        void handle(Collection<RouteGroup> groups, Collection<Vehicle> vehicles) {
            Collection<Id> outdated = new HashSet<>(markers.keySet());
            for (Vehicle vehicle: vehicles) {
                VehicleMarker vehicleMarker = markers.get(vehicle.id());
                if (vehicleMarker == null) {
                    MarkerOptions options = new MarkerOptions()
                        .anchor(0.5f, 0.5f)
                        .flat(true)
                        .position(new LatLng(vehicle.position().latitude(), vehicle.position().longitude()));
                    vehicleMarker = new VehicleMarker(vehicle, map.addMarker(options));
                    markers.put(vehicle.id(), vehicleMarker);
                } else {
                    outdated.remove(vehicle.id());
                    if (vehicleMarker.vehicle.equals(vehicle)) {
                        continue;
                    }
                    vehicleMarker.vehicle = vehicle;
                    vehicleMarker.marker.setPosition(new LatLng(vehicle.position().latitude(), vehicle.position().longitude()));
                }
                if (vehicleMarker.bitmapWrapper != null) {
                    vehicleMarker.bitmapWrapper.recycle();
                    vehicleMarker.bitmapWrapper = null;
                }
                Route route = getRouteById(groups, vehicle.routeId());
                vehicleMarker.bitmapWrapper = markerIconBuilder.build(route.id().getOriginal(), route.number(), vehicle.direction());
                vehicleMarker.marker.setIcon(BitmapDescriptorFactory.fromBitmap(vehicleMarker.bitmapWrapper.getBitmap()));
            }
            for (Id vehicleId: outdated) {
                markers.get(vehicleId).marker.remove();
                markers.remove(vehicleId);
            }
        }

        private Route getRouteById(Collection<RouteGroup> groups, Id routeId) {
            for (RouteGroup group: groups) {
                for (Route route: group.routes()) {
                    if (route.id().equals(routeId)) {
                        return route;
                    }
                }
            }
            throw new IllegalStateException(String.format("cannot find routeId %s", routeId));
        }
    }

    private static class PathPolylineHandler {

        private final ColorConstructor colorConstructor;
        private final GoogleMap map;
        private Map<Id, Polyline> polylines = new HashMap<>();

        PathPolylineHandler(ColorConstructor colorConstructor, GoogleMap map) {
            this.colorConstructor = colorConstructor;
            this.map = map;
        }

        void handle(Collection<Path> paths) {
            Collection<Id> outdated = new HashSet<>(polylines.keySet());
            for (Path path: paths) {
                Polyline polyline = polylines.get(path.routeId());
                if (polyline == null) {
                    PolylineOptions options = new PolylineOptions()
                        .color(colorConstructor.getByString(path.routeId().getOriginal()) & 0x55FFFFFF)
                        .width(4);
                    for (Point point : path.points()) {
                        options.add(new LatLng(point.latitude(), point.longitude()));
                    }
                    polylines.put(path.routeId(), map.addPolyline(options));
                } else {
                    outdated.remove(path.routeId());
                }
            }
            for (Id routeId: outdated) {
                polylines.get(routeId).remove();
                polylines.remove(routeId);
            }
        }
    }

    private static final Duration LOAD_VEHICLES_INTERVAL = Duration.standardSeconds(10);
    private static final int MAX_ROUTE_COUNT_WITH_NO_PENALTY = 3;
    private static final Duration LOAD_VEHICLES_PENALTY_INTERVAL = Duration.standardSeconds(5);

    private static final double CAMERA_LATITUDE = 56.488881;
    private static final double CAMERA_LONGITUDE = 84.987703;
    private static final int CAMERA_ZOOM = 12;

    @Inject
    ActivityLifecycleWatcher activityLifecycleWatcher;
    @Inject
    ColorConstructor colorConstructor;
    @Inject
    CommonFunctions commonFunctions;
    @Inject
    MarkerIconBuilder markerIconBuilder;
    @Inject
    ObservableCache observableCache;
    @Inject
    PathsPresenter pathsPresenter;
    @Inject
    PermissionChecker permissionChecker;
    @Inject
    Resources resources;
    @Inject
    RoutesPresenter routesPresenter;
    @Inject
    SelectedRoutesPresenter selectedRoutesPresenter;
    @Inject
    VehiclesPresenter vehiclesPresenter;

    @BindView(R.id.v__custom_map__no_vehicles)
    View noVehiclesView;
    @BindView(R.id.v__custom_map__map)
    MapView mapView;

    public CustomMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ComponentHolder.getActivityComponent().inject(this);
        }
    }

    @Override
    protected void inflateContent(LayoutInflater layoutInflater) {
        layoutInflater.inflate(R.layout.v__custom_map, this);
    }

    @Override
    void setupViews() {
        noVehiclesView.setVisibility(GONE);
    }

    @Override
    protected Disposable subscribeForEvents() {
        return new CompositeDisposable(
            subscribeForActivityEvents(),
            subscribeForMap(),
            subscribeForVehicles(),
            subscribeForPaths()
        );
    }

    private Disposable subscribeForActivityEvents() {
        return new CompositeDisposable(
            activityLifecycleWatcher.getState(Stage.CREATE)
                .subscribe(state -> mapView.onCreate(state.extra)),
            activityLifecycleWatcher.getState(Stage.START)
                .subscribe(state -> mapView.onStart()),
            activityLifecycleWatcher.getState(Stage.RESUME)
                .subscribe(state -> mapView.onResume()),
            activityLifecycleWatcher.getState(Stage.PAUSE)
                .subscribe(state -> mapView.onPause()),
            activityLifecycleWatcher.getState(Stage.STOP)
                .subscribe(state -> mapView.onStop()),
            activityLifecycleWatcher.getState(Stage.DESTROY)
                .subscribe(state -> mapView.onDestroy()),
            activityLifecycleWatcher.getState(Stage.SAVE_STATE)
                .subscribe(state -> mapView.onSaveInstanceState(state.extra)),
            activityLifecycleWatcher.getState(Stage.LOW_MEMORY)
                .subscribe(state -> mapView.onLowMemory())
        );
    }

    private Disposable subscribeForMap() {
        return new CompositeDisposable(
            getMap().subscribe(map -> {
                UiSettings uiSettings = map.getUiSettings();
                uiSettings.setMapToolbarEnabled(false);
                uiSettings.setZoomControlsEnabled(true);
                map.setPadding(0, (int) resources.getDimension(R.dimen.map_top_padding), 0, 0);
                map.moveCamera(
                    CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(new LatLng(CAMERA_LATITUDE, CAMERA_LONGITUDE), CAMERA_ZOOM))
                );
            }),
            Observable
                .combineLatest(
                    getMap(),
                    permissionChecker.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION),
                    commonFunctions::wrap
                )
                .subscribe(commonFunctions.unwrap(GoogleMap::setMyLocationEnabled))
        );
    }

    private Disposable subscribeForVehicles() {
        return new CompositeDisposable(
            getMap()
                .switchMap(map ->
                    Observable.combineLatest(
                        routesPresenter.getResults()
                            .filter(Result::isSuccess)
                            .map(Result::getData)
                            .distinctUntilChanged(),
                        Observable.merge(
                            vehiclesPresenter.getResults()
                                .filter(Result::isSuccess)
                                .map(Result::getData),
                            selectedRoutesPresenter.getSelectedRoutes()
                                .filter(Collection::isEmpty)
                                .map(o -> Collections.<Vehicle>emptyList())
                        ).distinctUntilChanged(),
                        Pair::with
                    )
                    .compose(commonFunctions.toMainThread())
                    .scan(new VehicleMarkerHandler(markerIconBuilder, map), (accumulated, pair) -> {
                        accumulated.handle(pair.getValue0(), pair.getValue1());
                        return accumulated;
                    })
                )
                .subscribe(),
            Observable
                .combineLatest(
                    vehiclesPresenter.getResults()
                        .filter(Result::isSuccess)
                        .map(Result::getData),
                    selectedRoutesPresenter.getSelectedRoutes(),
                    (vehicles, routeIds) -> vehicles.isEmpty() && !routeIds.isEmpty()
                )
                .compose(commonFunctions.toMainThread())
                .subscribe(isEmpty -> noVehiclesView.setVisibility(isEmpty ? VISIBLE : GONE))
        );
    }

    private Disposable subscribeForPaths() {
        return getMap()
            .switchMap(map ->
                pathsPresenter.getResults()
                    .filter(Result::isSuccess)
                    .map(Result::getData)
                    .distinctUntilChanged()
                    .compose(commonFunctions.toMainThread())
                    .scan(new PathPolylineHandler(colorConstructor, map), (accumulated, paths) -> {
                        accumulated.handle(paths);
                        return accumulated;
                    })
            )
            .subscribe();
    }

    private Observable<GoogleMap> getMap() {
        return observableCache.get("getMap", () ->
            Observable
                .<GoogleMap>create(source -> mapView.getMapAsync(source::onNext))
                .replay()
                .refCount()
        );
    }

    @Override
    void attachToPresenters() {
        routesPresenter.attach(this);
        pathsPresenter.attach(this);
        vehiclesPresenter.attach(this);
    }

    @Override
    void detachFromPresenters() {
        routesPresenter.detach(this);
        pathsPresenter.detach(this);
        vehiclesPresenter.detach(this);
    }

    @Override
    public Observable<Object> getLoadRoutesRequests() {
        return Observable.just(Irrelevant.INSTANCE);
    }

    @Override
    public Observable<Collection<Id>> getLoadPathsRequests() {
        return selectedRoutesPresenter.getSelectedRoutes();
    }

    @Override
    public Observable<Collection<Id>> getLoadVehiclesRequests() {
        return activityLifecycleWatcher.getState(Stage.RESUME, true)
            .switchMap(o ->
                selectedRoutesPresenter.getSelectedRoutes()
            )
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
            });
    }
}