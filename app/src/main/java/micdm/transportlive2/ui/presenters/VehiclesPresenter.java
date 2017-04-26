package micdm.transportlive2.ui.presenters;

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
import micdm.transportlive2.ui.misc.properties.NoValueProperty;

public class VehiclesPresenter extends BasePresenter {

    static class ViewInput {

        public final NoValueProperty loadVehicles = new NoValueProperty();
    }

    @Inject
    CommonFunctions commonFunctions;
    @Inject
    Loaders loaders;

    public final ViewInput viewInput = new ViewInput();
    private final Subject<Result<Collection<Vehicle>>> results = BehaviorSubject.create();

    private final Id routeId;

    VehiclesPresenter(Id routeId) {
        this.routeId = routeId;
    }

    @Override
    Disposable subscribeForEvents() {
        return subscribeForInput();
    }

    private Disposable subscribeForInput() {
        return viewInput.loadVehicles.get()
            .switchMap(o -> loaders.getVehiclesLoader(routeId).load())
            .subscribe(results::onNext);
    }

    public Observable<Result<Collection<Vehicle>>> getResults() {
        return results;
    }
}
