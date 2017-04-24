package micdm.transportlive2.ui;

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
import micdm.transportlive2.misc.Irrelevant;
import micdm.transportlive2.models.Vehicle;

public class VehiclesPresenter extends BasePresenter {

    static class ViewInput {

        private final Subject<Object> loadVehiclesRequests = PublishSubject.create();

        Observable<Object> getLoadVehiclesRequests() {
            return loadVehiclesRequests;
        }

        public void loadVehicles() {
            loadVehiclesRequests.onNext(Irrelevant.INSTANCE);
        }
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
        return new CompositeDisposable(
            subscribeForInput(),
            subscribeForResults()
        );
    }

    private Disposable subscribeForInput() {
        return viewInput.getLoadVehiclesRequests().subscribe(o -> loaders.getVehiclesLoader(routeId).load());
    }

    private Disposable subscribeForResults() {
        return loaders.getVehiclesLoader(routeId).getData().subscribe(results::onNext);
    }

    public Observable<Result<Collection<Vehicle>>> getResults() {
        return results;
    }
}
