package micdm.transportlive2.ui;

import java.util.Collection;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive2.data.loaders.Loaders;
import micdm.transportlive2.data.loaders.Result;
import micdm.transportlive2.data.loaders.VehiclesLoader;
import micdm.transportlive2.misc.CommonFunctions;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.models.Vehicle;

public class VehiclesPresenter extends BasePresenter<VehiclesPresenter.View, VehiclesPresenter.ViewInput> implements VehiclesLoader.Client {

    public interface View {

        Observable<Object> getLoadVehiclesRequests();
    }

    static class ViewInput extends BasePresenter.ViewInput<View> {

        private final Subject<Object> loadVehiclesRequests = PublishSubject.create();

        Observable<Object> getLoadVehiclesRequests() {
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

    private final Id routeId;

    private final Subject<Result<Collection<Vehicle>>> results = BehaviorSubject.create();

    VehiclesPresenter(Id routeId) {
        this.routeId = routeId;
    }

    @Override
    ViewInput newViewInput() {
        return new ViewInput();
    }

    @Override
    void attachToLoaders() {
        loaders.getVehiclesLoader(routeId).attach(this);
    }

    @Override
    Disposable subscribeForEvents() {
        return subscribeForResults();
    }

    private Disposable subscribeForResults() {
        return loaders.getVehiclesLoader(routeId).getData().subscribe(results::onNext);
    }

    @Override
    public Observable<Id> getLoadVehiclesRequests() {
        return viewInput.getLoadVehiclesRequests().compose(commonFunctions.toConst(routeId));
    }

    public Observable<Result<Collection<Vehicle>>> getResults() {
        return results;
    }
}
