package micdm.transportlive.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import micdm.transportlive.data.loaders.Loaders;
import micdm.transportlive.data.loaders.Result;
import micdm.transportlive.data.loaders.VehiclesLoader;
import micdm.transportlive.misc.CommonFunctions;
import micdm.transportlive.models.Vehicle;

public class VehiclesPresenter extends BasePresenter<VehiclesPresenter.View> implements VehiclesLoader.Client {

    interface View extends BasePresenter.View {

        Observable<Collection<String>> getLoadVehiclesRequests();
        Observable<Collection<String>> getReloadVehiclesRequests();
    }

    @Inject
    CommonFunctions commonFunctions;
    @Inject
    Loaders loaders;

    @Override
    Disposable subscribeForEvents() {
        return new CompositeDisposable(
            getVehiclesToLoad().subscribe(routeIds -> {
                for (String routeId: routeIds) {
                    loaders.getVehiclesLoader(routeId).attach(this);
                }
            }),
            getVehiclesToCancelLoad().subscribe(routeIds -> {
                for (String routeId: routeIds) {
                    loaders.getVehiclesLoader(routeId).detach(this);
                }
            })
        );
    }

    private Observable<Collection<String>> getVehiclesToLoad() {
        return getViewInput(View::getLoadVehiclesRequests);
    }

    private Observable<Collection<String>> getVehiclesToReload() {
        return getViewInput(View::getLoadVehiclesRequests);
    }

    private Observable<Collection<String>> getVehiclesToCancelLoad() {
        return commonFunctions.getDelta(
            getViewInput(View::getLoadVehiclesRequests).skip(1),
            getViewInput(View::getLoadVehiclesRequests).compose(commonFunctions.getPrevious())
        );
    }

    @Override
    public Observable<String> getLoadVehiclesRequests() {
        return getVehiclesToLoad()
            .switchMap(routeIds ->
                Observable
                    .interval(5, TimeUnit.SECONDS)
                    .switchMap(o -> Observable.fromIterable(routeIds))
            );
    }

    @Override
    public Observable<String> getReloadVehiclesRequests() {
        return getVehiclesToReload().switchMap(Observable::fromIterable);
    }

    @Override
    public Observable<String> getCancelVehiclesLoadingRequests() {
        return getVehiclesToCancelLoad().switchMap(Observable::fromIterable);
    }

    Observable<Result<Collection<Vehicle>>> getResults() {
        return getVehiclesToLoad().switchMap(routeIds -> {
            Collection<Observable<Result<Collection<Vehicle>>>> observables = new ArrayList<>(routeIds.size());
            for (String routeId: routeIds) {
                observables.add(loaders.getVehiclesLoader(routeId).getData());
            }
            return Observable.combineLatest(observables, objects -> {
                Collection<Result<Collection<Vehicle>>> results = new ArrayList<>(objects.length);
                for (Object result: objects) {
                    results.add((Result<Collection<Vehicle>>) result);
                }
                for (Result<Collection<Vehicle>> result: results) {
                    if (result.isFail()) {
                        return Result.newFail();
                    }
                }
                for (Result<Collection<Vehicle>> result: results) {
                    if (result.isLoading()) {
                        return Result.newLoading();
                    }
                }
                Collection<Vehicle> vehicles = new ArrayList<>();
                for (Result<Collection<Vehicle>> result: results) {
                    vehicles.addAll(result.getData());
                }
                return Result.newSuccess(vehicles);
            });
        });
    }
}
