package micdm.transportlive.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
import micdm.transportlive.models.Route;
import micdm.transportlive.models.RouteGroup;
import micdm.transportlive.models.Vehicle;
import micdm.transportlive.ui.misc.ActivityLifecycleWatcher;
import micdm.transportlive.ui.misc.ActivityLifecycleWatcher.Stage;

public class CustomMapView extends BaseView {

    private static final double CAMERA_LATITUDE = 56.488881;
    private static final double CAMERA_LONGITUDE = 84.987703;
    private static final int CAMERA_ZOOM = 12;

    @Inject
    ActivityLifecycleWatcher activityLifecycleWatcher;
    @Inject
    CommonFunctions commonFunctions;

    @BindView(R.id.v__custom_map__map)
    MapView mapView;

    private final Subject<Map<String, RouteGroup>> groups = BehaviorSubject.create();

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
            subscribeForVehicles()
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
                groups.<Map<Vehicle, Marker>>scan(new HashMap<>(), (accumulated, routeGroups) -> {
                    Map<Vehicle, Marker> markers = new HashMap<>(accumulated);
                    Map<Vehicle, Marker> outdated = new HashMap<>(accumulated);
                    for (RouteGroup routeGroup: routeGroups.values()) {
                        for (Route route: routeGroup.routes().values()) {
                            for (Vehicle vehicle: route.vehicles().values()) {
                                Marker marker = markers.get(vehicle);
                                if (marker == null) {
                                    MarkerOptions options = new MarkerOptions();
                                    options.position(new LatLng(vehicle.latitude(), vehicle.longitude()));
                                    markers.put(vehicle, map.addMarker(options));
                                } else {
                                    outdated.remove(vehicle);
                                    marker.setPosition(new LatLng(vehicle.latitude(), vehicle.longitude()));
                                }
                            }
                        }
                    }
                    for (Marker marker: outdated.values()) {
                        marker.remove();
                    }
                    return markers;
                })
            )
            .subscribe();
    }

    private Observable<GoogleMap> map;

    private Observable<GoogleMap> getMap() {
        if (map == null) {
            map = Observable.<GoogleMap>create(source -> mapView.getMapAsync(source::onNext)).replay().refCount();
        }
        return map;
    }

    public void setVehicles(Map<String, RouteGroup> groups) {
        this.groups.onNext(groups);
    }
}
