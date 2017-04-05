package micdm.transportlive.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.javatuples.Pair;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive.ComponentHolder;
import micdm.transportlive.R;
import micdm.transportlive.misc.CommonFunctions;
import micdm.transportlive.misc.Id;
import micdm.transportlive.misc.ObservableCache;
import micdm.transportlive.models.Path;
import micdm.transportlive.models.Point;
import micdm.transportlive.models.Route;
import micdm.transportlive.models.RouteGroup;
import micdm.transportlive.models.Vehicle;
import micdm.transportlive.ui.misc.ActivityLifecycleWatcher;
import micdm.transportlive.ui.misc.ActivityLifecycleWatcher.Stage;
import micdm.transportlive.ui.misc.ColorConstructor;
import micdm.transportlive.ui.misc.MarkerIconBuilder;

public class CustomMapView extends BaseView {

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

    @BindView(R.id.v__custom_map__no_vehicles)
    View noVehiclesView;
    @BindView(R.id.v__custom_map__map)
    MapView mapView;

    private final Subject<Collection<RouteGroup>> groups = BehaviorSubject.create();
    private final Subject<Collection<Vehicle>> vehicles = BehaviorSubject.create();
    private final Subject<Collection<Path>> paths = BehaviorSubject.create();

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
        return getMap().subscribe(map ->
            map.moveCamera(
                CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(new LatLng(CAMERA_LATITUDE, CAMERA_LONGITUDE), CAMERA_ZOOM))
            )
        );
    }

    private Disposable subscribeForVehicles() {
        return new CompositeDisposable(
            getMap()
                .switchMap(map ->
                    Observable.combineLatest(
                        groups.distinctUntilChanged(),
                        vehicles.distinctUntilChanged(),
                        Pair::with
                    )
                    .scan(new VehicleMarkerHandler(markerIconBuilder, map), (accumulated, pair) -> {
                        accumulated.handle(pair.getValue0(), pair.getValue1());
                        return accumulated;
                    })
                )
                .subscribe(),
            vehicles
                .map(Collection::isEmpty)
                .subscribe(isEmpty -> noVehiclesView.setVisibility(isEmpty ? VISIBLE : GONE))
        );
    }

    private Disposable subscribeForPaths() {
        return getMap()
            .switchMap(map ->
                paths
                    .distinctUntilChanged()
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

    public void setRoutes(Collection<RouteGroup> groups) {
        this.groups.onNext(groups);
    }

    public void setVehicles(Collection<Vehicle> vehicles) {
        this.vehicles.onNext(vehicles);
    }

    public void setPaths(Collection<Path> paths) {
        this.paths.onNext(paths);
    }
}
