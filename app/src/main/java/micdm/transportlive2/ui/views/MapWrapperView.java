package micdm.transportlive2.ui.views;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive2.ComponentHolder;
import micdm.transportlive2.R;
import micdm.transportlive2.misc.CommonFunctions;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.misc.ObservableCache;
import micdm.transportlive2.models.ImmutablePoint;
import micdm.transportlive2.models.ImmutablePreferences;
import micdm.transportlive2.models.Path;
import micdm.transportlive2.models.Point;
import micdm.transportlive2.models.Preferences;
import micdm.transportlive2.models.RouteGroup;
import micdm.transportlive2.models.Station;
import micdm.transportlive2.models.Vehicle;
import micdm.transportlive2.ui.misc.ActivityLifecycleWatcher;
import micdm.transportlive2.ui.misc.ColorConstructor;
import micdm.transportlive2.ui.misc.PaintConstructor;
import micdm.transportlive2.ui.misc.PermissionChecker;
import micdm.transportlive2.ui.misc.VehicleMarkerIconBuilder;
import micdm.transportlive2.ui.presenters.Presenters;
import ru.yandex.yandexmapkit.MapController;
import ru.yandex.yandexmapkit.MapView;
import ru.yandex.yandexmapkit.OverlayManager;
import ru.yandex.yandexmapkit.map.MapEvent;
import ru.yandex.yandexmapkit.map.OnMapListener;
import ru.yandex.yandexmapkit.overlay.IRender;
import ru.yandex.yandexmapkit.overlay.Overlay;
import ru.yandex.yandexmapkit.overlay.OverlayItem;
import ru.yandex.yandexmapkit.utils.GeoPoint;
import ru.yandex.yandexmapkit.utils.ScreenPoint;

public class MapWrapperView extends BaseView {

//    private static class VehicleHandler {
//
//        private static class VehicleMarker {
//
//            Vehicle vehicle;
//            final Marker marker;
//            VehicleMarkerIconBuilder.BitmapWrapper bitmapWrapper;
//            final ValueAnimator animator;
//
//            private VehicleMarker(Vehicle vehicle, Marker marker, ValueAnimator animator) {
//                this.vehicle = vehicle;
//                this.marker = marker;
//                this.animator = animator;
//                animator.addUpdateListener(animation -> marker.setPosition((LatLng) animation.getAnimatedValue()));
//            }
//
//            void cleanup() {
//                marker.remove();
//                if (bitmapWrapper != null) {
//                    bitmapWrapper.recycle();
//                }
//                animator.removeAllUpdateListeners();
//                animator.cancel();
//            }
//        }
//
//        private final Provider<ValueAnimator> vehicleMarkerAnimatorProvider;
//        private final VehicleMarkerIconBuilder vehicleMarkerIconBuilder;
//        private final GoogleMap map;
//        private final Map<Id, VehicleMarker> markers = new HashMap<>();
//
//        VehicleHandler(Provider<ValueAnimator> vehicleMarkerAnimatorProvider, VehicleMarkerIconBuilder vehicleMarkerIconBuilder, GoogleMap map) {
//            this.vehicleMarkerAnimatorProvider = vehicleMarkerAnimatorProvider;
//            this.vehicleMarkerIconBuilder = vehicleMarkerIconBuilder;
//            this.map = map;
//        }
//
//        void handle(Collection<RouteGroup> groups, Collection<Vehicle> vehicles) {
//            Collection<Id> outdated = new HashSet<>(markers.keySet());
//            for (Vehicle vehicle: vehicles) {
//                VehicleMarker vehicleMarker = markers.get(vehicle.id());
//                if (vehicleMarker == null) {
//                    vehicleMarker = newMarker(vehicle);
//                    markers.put(vehicle.id(), vehicleMarker);
//                } else {
//                    outdated.remove(vehicle.id());
//                    if (vehicleMarker.vehicle.equals(vehicle)) {
//                        continue;
//                    }
//                    vehicleMarker.vehicle = vehicle;
//                    updatePosition(vehicleMarker.marker, vehicle.position(), vehicleMarker.animator);
//                }
//                updateIcon(vehicleMarker, groups, vehicle);
//            }
//            for (Id id: outdated) {
//                markers.remove(id).cleanup();
//            }
//        }
//
//        private VehicleMarker newMarker(Vehicle vehicle) {
//            MarkerOptions options = new MarkerOptions()
//                .anchor(0.5f, 0.5f)
//                .flat(true)
//                .position(new LatLng(vehicle.position().latitude(), vehicle.position().longitude()))
//                .zIndex(1);
//            return new VehicleMarker(vehicle, map.addMarker(options), vehicleMarkerAnimatorProvider.get());
//        }
//
//        private void updatePosition(Marker marker, Point position, ValueAnimator animator) {
//            animator.cancel();
//            animator.setObjectValues(marker.getPosition(), new LatLng(position.latitude(), position.longitude()));
//            animator.start();
//        }
//
//        private void updateIcon(VehicleMarker vehicleMarker, Collection<RouteGroup> groups, Vehicle vehicle) {
//            if (vehicleMarker.bitmapWrapper != null) {
//                vehicleMarker.bitmapWrapper.recycle();
//                vehicleMarker.bitmapWrapper = null;
//            }
//            Route route = getRouteById(groups, vehicle.routeId());
//            vehicleMarker.bitmapWrapper = vehicleMarkerIconBuilder.build(route.id().getOriginal(), route.number(), vehicle.direction());
//            vehicleMarker.marker.setIcon(BitmapDescriptorFactory.fromBitmap(vehicleMarker.bitmapWrapper.getBitmap()));
//        }
//
//        private Route getRouteById(Collection<RouteGroup> groups, Id routeId) {
//            for (RouteGroup group: groups) {
//                for (Route route: group.routes()) {
//                    if (route.id().equals(routeId)) {
//                        return route;
//                    }
//                }
//            }
//            throw new IllegalStateException(String.format("cannot find routeId %s", routeId));
//        }
//    }
//

    private static class PathsOverlay extends Overlay {

        PathsOverlay(MapController mapController) {
            super(mapController);
        }

        @Override
        public List prepareDraw() {
            for (Object item: getOverlayItems()) {
                ((Polyline) item).prepare(getMapController());
            }
            return getOverlayItems();
        }
    }

    private static class Polyline extends OverlayItem {

        private static final GeoPoint DUMMY = new GeoPoint(0, 0);

        private final List<GeoPoint> geoPoints = new ArrayList<>();
        private final List<ScreenPoint> screenPoints = new ArrayList<>();
        private final Id routeId;

        Polyline(Id routeId) {
            super(DUMMY, null);
            this.routeId = routeId;
        }

        void addPoint(GeoPoint point) {
            geoPoints.add(point);
        }

        Id getRouteId() {
            return routeId;
        }

        android.graphics.Path getPath() {
            android.graphics.Path path = new android.graphics.Path();
            path.moveTo(screenPoints.get(0).getX(), screenPoints.get(0).getY());
            for (int i = 1; i < screenPoints.size(); i += 1) {
                path.lineTo(screenPoints.get(i).getX(), screenPoints.get(i).getY());
            }
            return path;
        }

        void prepare(MapController mapController) {
            screenPoints.clear();
            for (GeoPoint point: geoPoints) {
                screenPoints.add(mapController.getScreenPoint(point));
            }
        }
    }

    private static class PathsRenderer implements IRender {

        private final PaintConstructor paintConstructor;

        PathsRenderer(PaintConstructor paintConstructor) {
            this.paintConstructor = paintConstructor;
        }

        @Override
        public void draw(Canvas canvas, OverlayItem item) {
            canvas.drawPath(((Polyline) item).getPath(), paintConstructor.getByString(((Polyline) item).getRouteId().getOriginal()));
        }
    }

    private static class PathHandler {

        private final Overlay overlay;
        private final Map<Id, Polyline> polylines = new HashMap<>();

        PathHandler(Overlay overlay) {
            this.overlay = overlay;
        }

        void handle(Collection<Path> paths) {
            Collection<Id> outdated = new HashSet<>(polylines.keySet());
            for (Path path: paths) {
                if (!polylines.containsKey(path.routeId())) {
                    polylines.put(path.routeId(), newPolyline(path));
                } else {
                    outdated.remove(path.routeId());
                }
            }
            for (Id id: outdated) {
                Polyline polyline = polylines.remove(id);
                overlay.removeOverlayItem(polyline);
            }
        }

        private Polyline newPolyline(Path path) {
            Polyline polyline = new Polyline(path.routeId());
            for (Point point: path.points()) {
                polyline.addPoint(new GeoPoint(point.latitude(), point.longitude()));
            }
            overlay.addOverlayItem(polyline);
            return polyline;
        }
    }

    private static class StationHandler {

        private static class VehicleMarker {

            final OverlayItem marker;
            final Id stationId;

            private VehicleMarker(OverlayItem marker, Id stationId) {
                this.marker = marker;
                this.stationId = stationId;
            }

            void cleanup(Overlay overlay) {
                overlay.removeOverlayItem(marker);
            }
        }

        private final Bitmap icon;
        private final Resources resources;
        private final Overlay overlay;
        private final Map<Id, VehicleMarker> markers = new HashMap<>();

        StationHandler(Bitmap icon, Resources resources, Overlay overlay) {
            this.icon = icon;
            this.resources = resources;
            this.overlay = overlay;
        }

        void handle(Collection<Station> stations, boolean needShowStations) {
            Collection<Id> outdated = new HashSet<>(markers.keySet());
            for (Station station: stations) {
                VehicleMarker vehicleMarker = markers.get(station.id());
                if (vehicleMarker == null) {
                    vehicleMarker = newMarker(station, needShowStations);
                    markers.put(station.id(), vehicleMarker);
                } else {
                    outdated.remove(station.id());
                    vehicleMarker.marker.setVisible(needShowStations);
                }
            }
            for (Id id: outdated) {
                VehicleMarker vehicleMarker = markers.remove(id);
                vehicleMarker.cleanup(overlay);
            }
        }

        private VehicleMarker newMarker(Station station, boolean needShow) {
            OverlayItem marker = new OverlayItem(new GeoPoint(station.location().latitude(), station.location().longitude()),
                                                 new BitmapDrawable(resources, icon));
            marker.setVisible(needShow);
            overlay.addOverlayItem(marker);
            return new VehicleMarker(marker, station.id());
        }
    }

    private static final int CAMERA_ZOOM_TO_SHOW_STATIONS = 12;

    @Inject
    ActivityLifecycleWatcher activityLifecycleWatcher;
    @Inject
    @Named("stationIcon")
    Bitmap stationIcon;
    @Inject
    ColorConstructor colorConstructor;
    @Inject
    CommonFunctions commonFunctions;
    @Inject
    ObservableCache observableCache;
    @Inject
    @Named("pathLine")
    PaintConstructor pathLinePaintConstructor;
    @Inject
    PermissionChecker permissionChecker;
    @Inject
    Presenters presenters;
    @Inject
    Resources resources;
    @Inject
    Provider<ValueAnimator> vehicleMarkerAnimatorProvider;
    @Inject
    VehicleMarkerIconBuilder vehicleMarkerIconBuilder;

    @BindView(R.id.v__map_wrapper__map)
    MapView mapView;

    private final Subject<Collection<RouteGroup>> groups = BehaviorSubject.create();
    private final Subject<Collection<Path>> paths = BehaviorSubject.create();
    private final Subject<Collection<Station>> stations = BehaviorSubject.create();
    private final Subject<Collection<Vehicle>> vehicles = BehaviorSubject.create();

    public MapWrapperView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ComponentHolder.getActivityComponent().inject(this);
        }
    }

    @Override
    void inflateContent(LayoutInflater layoutInflater) {
        layoutInflater.inflate(R.layout.v__map_wrapper, this);
    }

    @Override
    void setupViews() {
        mapView.showBuiltInScreenButtons(true);
    }

    @Override
    Disposable subscribeForEvents() {
        return new CompositeDisposable(
            subscribeForMap(),
//            subscribeForRoutesAndVehicles(),
            subscribeForPaths(),
            subscribeForStations()
        );
    }

    private Observable<MapController> getMapController() {
        return observableCache.get("getMapController", () ->
            Observable.<MapController>create(source -> source.onNext(mapView.getMapController()))
                .replay()
                .refCount()
        );
    }

    private Observable<OverlayManager> getOverlayManager() {
        return observableCache.get("getOverlayManager", () ->
            getMapController()
                .switchMap(mapController -> Observable.<OverlayManager>create(source -> source.onNext(mapController.getOverlayManager())))
                .replay()
                .refCount()
        );
    }

    private Observable<Preferences.CameraPosition> getCameraPosition() {
        return observableCache.get("getCameraPosition", () ->
            getMapController()
                .switchMap(mapController ->
                    Observable.<Preferences.CameraPosition>create(source -> {
                        OnMapListener listener = event -> {
                            int code = event.getMsg();
                            if (code == MapEvent.MSG_SCALE_END || code == MapEvent.MSG_SCROLL_END || code == MapEvent.MSG_ZOOM_END) {
                                source.onNext(
                                    ImmutablePreferences.CameraPosition.builder()
                                        .position(
                                            ImmutablePoint.builder()
                                                .latitude(mapController.getMapCenter().getLat())
                                                .longitude(mapController.getMapCenter().getLon())
                                                .build()
                                        )
                                        .zoom(mapController.getZoomCurrent())
                                        .build()
                                );
                            }
                        };
                        mapController.addMapListener(listener);
                        source.setCancellable(() -> mapController.removeMapListener(listener));
                    })
                )
                .replay()
                .refCount()
        );
    }

    private Disposable subscribeForMap() {
        return new CompositeDisposable(
            getMapController()
                .withLatestFrom(presenters.getPreferencesPresenter().getCameraPosition(), commonFunctions::wrap)
                .subscribe(commonFunctions.unwrap((mapController, camera) -> {
                    mapController.setPositionNoAnimationTo(new GeoPoint(camera.position().latitude(), camera.position().longitude()));
                    mapController.setZoomCurrent((float) camera.zoom());
                })),
            Observable
                .combineLatest(
                    getOverlayManager(),
                    permissionChecker.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION),
                    commonFunctions::wrap
                )
                .subscribe(commonFunctions.unwrap((overlayManager, isLocationAccessAllowed) ->
                    overlayManager.getMyLocation().setEnabled(isLocationAccessAllowed)
                ))
        );
    }

//    private Disposable subscribeForRoutesAndVehicles() {
//        return getMap()
//            .switchMap(map ->
//                Observable.combineLatest(
//                    Observable.just(new VehicleHandler(vehicleMarkerAnimatorProvider, vehicleMarkerIconBuilder, map)),
//                    groups,
//                    vehicles,
//                    commonFunctions::wrap
//                )
//            )
//            .compose(commonFunctions.toMainThread())
//            .subscribe(commonFunctions.unwrap(VehicleHandler::handle));
//    }
//
    private Disposable subscribeForPaths() {
        Observable<PathsOverlay> common = getMapController()
            .map(mapController -> {
                PathsOverlay overlay = new PathsOverlay(mapController);
                overlay.setIRender(new PathsRenderer(pathLinePaintConstructor));
                return overlay;
            })
            .replay()
            .refCount();
        return new CompositeDisposable(
            Observable
                .combineLatest(getOverlayManager(), common, commonFunctions::wrap)
                .subscribe(commonFunctions.unwrap(OverlayManager::addOverlay)),
            common
                .switchMap(overlay ->
                    Observable.combineLatest(
                        Observable.just(new PathHandler(overlay)),
                        paths,
                        commonFunctions::wrap
                    )
                )
                .compose(commonFunctions.toMainThread())
                .subscribe(commonFunctions.unwrap(PathHandler::handle))
        );
    }

    private Disposable subscribeForStations() {
        Observable<Overlay> common = getMapController()
            .map(Overlay::new)
            .replay()
            .refCount();
        return new CompositeDisposable(
            Observable
                .combineLatest(getOverlayManager(), common, commonFunctions::wrap)
                .subscribe(commonFunctions.unwrap(OverlayManager::addOverlay)),
            common
                .switchMap(overlay ->
                    Observable.combineLatest(
                        Observable.just(new StationHandler(stationIcon, resources, overlay)),
                        stations,
                        Observable.combineLatest(
                            presenters.getPreferencesPresenter().getNeedShowStations(),
                            getCameraPosition().map(cameraPosition -> cameraPosition.zoom() >= CAMERA_ZOOM_TO_SHOW_STATIONS),
                            (a, b) -> a && b
                        ),
                        commonFunctions::wrap
                    )
                )
                .compose(commonFunctions.toMainThread())
                .subscribe(commonFunctions.unwrap(StationHandler::handle))
        );
    }

    public Observable<Preferences> getPreferences() {
        return getCameraPosition().withLatestFrom(
            presenters.getPreferencesPresenter().getPreferences(),
            (cameraPosition, preferences) ->
                ImmutablePreferences.builder()
                    .from(preferences)
                    .cameraPosition(cameraPosition)
                    .build()
        );
    }

    public Observable<Id> getCurrentStation() {
        return Observable.empty();
//        return getMap().switchMap(map ->
//            Observable.<Id>create(source -> {
//                map.setOnMarkerClickListener(marker -> {
//                    Object tag = marker.getTag();
//                    if (tag != null && tag instanceof Id) {
//                        source.onNext((Id) tag);
//                        return true;
//                    }
//                    return false;
//                });
//                source.setCancellable(() -> map.setOnMarkerClickListener(null));
//            })
//        );
    }

    public void setRoutes(Collection<RouteGroup> groups) {
        this.groups.onNext(groups);
    }

    public void setPaths(Collection<Path> paths) {
        this.paths.onNext(paths);
    }

    public void setStations(Collection<Station> stations) {
        this.stations.onNext(stations);
    }

    public void setVehicles(Collection<Vehicle> vehicles) {
        this.vehicles.onNext(vehicles);
    }
}
