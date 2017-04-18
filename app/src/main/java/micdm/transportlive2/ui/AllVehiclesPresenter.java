package micdm.transportlive2.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive2.data.loaders.Loaders;
import micdm.transportlive2.data.loaders.Result;
import micdm.transportlive2.data.loaders.VehiclesLoader;
import micdm.transportlive2.misc.CommonFunctions;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.models.Vehicle;
import micdm.transportlive2.ui.misc.ResultWatcherN;

public class AllVehiclesPresenter extends BasePresenter<AllVehiclesPresenter.View, AllVehiclesPresenter.ViewInput> implements VehiclesLoader.Client {

    public interface View {

        Observable<Collection<Id>> getLoadVehiclesRequests();
    }

    static class ViewInput extends BasePresenter.ViewInput<View> {

        private final Subject<Collection<Id>> loadVehiclesRequests = BehaviorSubject.create();

        Observable<Collection<Id>> getLoadVehiclesRequests() {
            return loadVehiclesRequests;
        }

        @Override
        Disposable subscribeForInput(View view) {
            return view.getLoadVehiclesRequests().subscribe(loadVehiclesRequests::onNext);
        }
    }

    @Inject
    CommonFunctions commonFunctions;
    @Inject
    Loaders loaders;

    private final Subject<Result<Collection<Vehicle>>> results = BehaviorSubject.create();

    @Override
    ViewInput newViewInput() {
        return new ViewInput();
    }

    @Override
    Disposable subscribeForEvents() {
        return new CompositeDisposable(
            getVehiclesLoadersToAttach().subscribe(loader -> loader.attach(this)),
            getVehiclesLoadersToDetach().subscribe(loader -> loader.detach(this)),
            subscribeForResults()
        );
    }

    private Observable<VehiclesLoader> getVehiclesLoadersToAttach() {
        return commonFunctions
            .getDelta(
                viewInput.getLoadVehiclesRequests()
                    .compose(commonFunctions.getPrevious())
                    .startWith(Collections.<Id>emptyList()),
                viewInput.getLoadVehiclesRequests()
            )
            .switchMap(Observable::fromIterable)
            .map(loaders::getVehiclesLoader);
    }

    private Observable<VehiclesLoader> getVehiclesLoadersToDetach() {
        return commonFunctions
            .getDelta(
                viewInput.getLoadVehiclesRequests().skip(1),
                viewInput.getLoadVehiclesRequests().compose(commonFunctions.getPrevious())
            )
            .switchMap(Observable::fromIterable)
            .map(loaders::getVehiclesLoader);
    }

    private Disposable subscribeForResults() {
        return viewInput.getLoadVehiclesRequests()
            .map(routeIds -> {
                Collection<Observable<Result<Collection<Vehicle>>>> observables = new ArrayList<>();
                for (Id routeId: routeIds) {
                    observables.add(loaders.getVehiclesLoader(routeId).getData());
                }
                return new ResultWatcherN<>(commonFunctions, observables);
            })
            .<Result<Collection<Vehicle>>>switchMap(watcher ->
                Observable.merge(
                    watcher.getLoading().compose(commonFunctions.toConst(Result.newLoading())),
                    watcher.getSuccess().map(datas -> {
                        Collection<Vehicle> vehicles = new ArrayList<>();
                        for (Collection<Vehicle> data: datas) {
                            vehicles.addAll(data);
                        }
                        return Result.newSuccess(vehicles);
                    }),
                    watcher.getFail().compose(commonFunctions.toConst(Result.newFail()))
                )
            )
            .subscribe(results::onNext);
    }

    @Override
    public Observable<Id> getLoadVehiclesRequests() {
        return viewInput.getLoadVehiclesRequests().switchMap(Observable::fromIterable);
    }

    public Observable<Result<Collection<Vehicle>>> getResults() {
        return results;
    }
}
