package micdm.transportlive2.ui.presenters;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive2.data.loaders.Loaders;
import micdm.transportlive2.data.loaders.Result;
import micdm.transportlive2.misc.CommonFunctions;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.models.Vehicle;
import micdm.transportlive2.ui.misc.ResultWatcherN;
import micdm.transportlive2.ui.misc.properties.ValueProperty;

public class AllVehiclesPresenter extends BasePresenter {

    public static class ViewInput {

        public final ValueProperty<Collection<Id>> routes = new ValueProperty<>();
    }

    @Inject
    CommonFunctions commonFunctions;
    @Inject
    Loaders loaders;

    public final ViewInput viewInput = new ViewInput();
    private final Subject<Result<Collection<Vehicle>>> results = BehaviorSubject.create();

    @Override
    Disposable subscribeForEvents() {
        return subscribeForInput();
    }

    private Disposable subscribeForInput() {
        return viewInput.routes.get()
            .map(routeIds -> {
                Collection<Observable<Result<Collection<Vehicle>>>> observables = new ArrayList<>();
                for (Id routeId: routeIds) {
                    observables.add(loaders.getVehiclesLoader(routeId).load());
                }
                return ResultWatcherN.newInstance(observables);
            })
            .switchMap(watcher ->
                Observable.<Result<Collection<Vehicle>>>merge(
                    watcher.getLoading().map(o -> Result.newLoading()),
                    watcher.getSuccess().map(datas -> {
                        Collection<Vehicle> vehicles = new ArrayList<>();
                        for (Collection<Vehicle> data: datas) {
                            vehicles.addAll(data);
                        }
                        return Result.newSuccess(vehicles);
                    }),
                    watcher.getFail().map(o -> Result.newFail())
                )
            )
            .subscribe(results::onNext);
    }

    public Observable<Result<Collection<Vehicle>>> getResults() {
        return results;
    }
}
