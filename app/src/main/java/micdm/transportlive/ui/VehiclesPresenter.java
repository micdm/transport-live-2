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
import micdm.transportlive.ui.misc.ResultWatcherN;

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
        return getViewInput(View::getReloadVehiclesRequests);
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
                    .interval(10, TimeUnit.SECONDS)
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
            ResultWatcherN<Collection<Vehicle>> watcher = new ResultWatcherN<>(commonFunctions, observables);
            return Observable.merge(
                watcher.getLoading().compose(commonFunctions.toConst(Result.newLoading())),
                watcher.getSuccess().map(datas -> {
                    Collection<Vehicle> vehicles = new ArrayList<>();
                    for (Collection<Vehicle> data: datas) {
                        vehicles.addAll(data);
                    }
                    return Result.newSuccess(vehicles);
                }),
                watcher.getFail().compose(commonFunctions.toConst(Result.newFail()))
            );
        });
    }
}
