package micdm.transportlive.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

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
import micdm.transportlive.models.Path;
import micdm.transportlive.models.Point;
import micdm.transportlive.models.Route;
import micdm.transportlive.models.RouteGroup;
import micdm.transportlive.models.Vehicle;
import micdm.transportlive.ui.misc.ActivityLifecycleWatcher;
import micdm.transportlive.ui.misc.ActivityLifecycleWatcher.Stage;
import micdm.transportlive.ui.misc.ColorConstructor;
import micdm.transportlive.ui.misc.MarkerIconBuilder;
import micdm.transportlive.utils.ObservableCache;

public class CustomMapView extends BaseView {

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
    protected Disposable subscribeForEvents() {
        return new CompositeDisposable(
            subscribeForWindowEvents(),
            subscribeForMap(),
            subscribeForVehicles(),
            subscribeForPaths()
        );
    }

    private Disposable subscribeForWindowEvents() {
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
        return getMap()
            .switchMap(map ->
                Observable.combineLatest(
                    groups,
                    vehicles,
                    Pair::with
                )
                .<Map<Vehicle, Marker>>scan(new HashMap<>(), (accumulated, pair) -> {
                    Map<Vehicle, Marker> markers = new HashMap<>(accumulated);
                    Map<Vehicle, Marker> outdated = new HashMap<>(accumulated);
                    for (Vehicle vehicle: pair.getValue1()) {
                        Marker marker = markers.get(vehicle);
                        if (marker == null) {
                            MarkerOptions options = new MarkerOptions()
                                .anchor(0.5f, 0.5f)
                                .flat(true)
                                .position(new LatLng(vehicle.position().latitude(), vehicle.position().longitude()));
                            marker = map.addMarker(options);
                            markers.put(vehicle, marker);
                        } else {
                            outdated.remove(vehicle);
                            marker.setPosition(new LatLng(vehicle.position().latitude(), vehicle.position().longitude()));
                        }
                        MarkerIconBuilder.BitmapWrapper wrapper = (MarkerIconBuilder.BitmapWrapper) marker.getTag();
                        if (wrapper != null) {
                            wrapper.recycle();
                        }
                        Route route = getRouteById(pair.getValue0(), vehicle.route());
                        wrapper = markerIconBuilder.build(route.id(), route.number(), vehicle.direction());
                        marker.setTag(wrapper);
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(wrapper.getBitmap()));
                    }
                    for (Marker marker: outdated.values()) {
                        marker.remove();
                    }
                    return markers;
                })
            )
            .subscribe();
    }

    private Route getRouteById(Collection<RouteGroup> groups, String routeId) {
        for (RouteGroup group: groups) {
            for (Route route: group.routes()) {
                if (route.id().equals(routeId)) {
                    return route;
                }
            }
        }
        throw new IllegalStateException(String.format("cannot find route %s", routeId));
    }

    private Disposable subscribeForPaths() {
        return getMap()
            .switchMap(map ->
                paths.<Map<Path, Polyline>>scan(new HashMap<>(), (accumulated, paths) -> {
                    Map<Path, Polyline> polylines = new HashMap<>(accumulated);
                    Map<Path, Polyline> outdated = new HashMap<>(accumulated);
                    for (Path path: paths) {
                        Polyline polyline = polylines.get(path);
                        if (polyline == null) {
                            PolylineOptions options = new PolylineOptions()
                                .color(colorConstructor.getByString(path.route()) & 0x55FFFFFF)
                                .width(4);
                            for (Point point : path.points()) {
                                options.add(new LatLng(point.latitude(), point.longitude()));
                            }
                            polylines.put(path, map.addPolyline(options));
                        } else {
                            outdated.remove(path);
                        }
                    }
                    for (Polyline polyline : outdated.values()) {
                        polyline.remove();
                    }
                    return polylines;
                })
            )
            .subscribe();
    }

    private Observable<GoogleMap> getMap() {
        return observableCache.get(this, "getMap",
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

    @Override
    protected void cleanup() {
        observableCache.clear(this);
    }
}
