package micdm.transportlive2.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import micdm.transportlive2.data.loaders.Loaders;
import micdm.transportlive2.data.loaders.Result;
import micdm.transportlive2.data.loaders.VehiclesLoader;
import micdm.transportlive2.misc.CommonFunctions;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.models.Vehicle;
import micdm.transportlive2.ui.misc.ResultWatcherN;

public class AllVehiclesPresenter extends BasePresenter<AllVehiclesPresenter.View> implements VehiclesLoader.Client {

    public interface View {

        Observable<Collection<Id>> getLoadVehiclesRequests();
    }

    @Inject
    CommonFunctions commonFunctions;
    @Inject
    Loaders loaders;

    @Override
    Disposable subscribeForEvents() {
        return new CompositeDisposable(
            getVehiclesLoadersToAttach().subscribe(loader -> loader.attach(this)),
            getVehiclesLoadersToDetach().subscribe(loader -> loader.detach(this))
        );
    }

    private Observable<VehiclesLoader> getVehiclesLoadersToAttach() {
        Observable<Collection<Id>> common = getViewInput(View::getLoadVehiclesRequests);
        return commonFunctions
            .getDelta(
                common
                    .compose(commonFunctions.getPrevious())
                    .startWith(Collections.<Id>emptyList()),
                common
            )
            .switchMap(Observable::fromIterable)
            .map(loaders::getVehiclesLoader);
    }

    private Observable<VehiclesLoader> getVehiclesLoadersToDetach() {
        Observable<Collection<Id>> common = getViewInput(View::getLoadVehiclesRequests);
        return commonFunctions
            .getDelta(
                common.skip(1),
                common.compose(commonFunctions.getPrevious())
            )
            .switchMap(Observable::fromIterable)
            .map(loaders::getVehiclesLoader);
    }

    @Override
    public Observable<Id> getLoadVehiclesRequests() {
        return getViewInput(View::getLoadVehiclesRequests).switchMap(Observable::fromIterable);
    }

    public Observable<Result<Collection<Vehicle>>> getResults() {
        return Observable
            .<Consumer<Collection<VehiclesLoader>>>merge(
                getVehiclesLoadersToAttach().map(loader -> accumulated -> accumulated.add(loader)),
                getVehiclesLoadersToDetach().map(loader -> accumulated -> accumulated.remove(loader))
            )
            .scan(new ArrayList<VehiclesLoader>(), (accumulated, handler) -> {
                handler.accept(accumulated);
                return accumulated;
            })
            .skip(1)
            .switchMap(loaders -> {
                Collection<Observable<Result<Collection<Vehicle>>>> observables = new ArrayList<>(loaders.size());
                for (VehiclesLoader loader: loaders) {
                    observables.add(loader.getData());
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
