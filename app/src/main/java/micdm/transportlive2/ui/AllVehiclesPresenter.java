package micdm.transportlive2.ui;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive2.data.loaders.Loaders;
import micdm.transportlive2.data.loaders.Result;
import micdm.transportlive2.misc.CommonFunctions;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.models.Vehicle;
import micdm.transportlive2.ui.misc.ResultWatcherN;

public class AllVehiclesPresenter extends BasePresenter {

    public static class ViewInput {

        private final Subject<Collection<Id>> loadVehiclesRequests = PublishSubject.create();

        Observable<Collection<Id>> getLoadVehiclesRequests() {
            return loadVehiclesRequests;
        }

        public void loadVehicles(Collection<Id> ids) {
            loadVehiclesRequests.onNext(ids);
        }
    }

    @Inject
    CommonFunctions commonFunctions;
    @Inject
    Loaders loaders;

    public final ViewInput viewInput = new ViewInput();
    private final Subject<Result<Collection<Vehicle>>> results = BehaviorSubject.create();

    @Override
    Disposable subscribeForEvents() {
        return new CompositeDisposable(
            subscribeForInput(),
            subscribeForResults()
        );
    }

    private Disposable subscribeForInput() {
        return viewInput.getLoadVehiclesRequests().subscribe(ids -> {
            for (Id id: ids) {
                loaders.getVehiclesLoader(id).load();
            }
        });
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

    public Observable<Result<Collection<Vehicle>>> getResults() {
        return results;
    }
}
