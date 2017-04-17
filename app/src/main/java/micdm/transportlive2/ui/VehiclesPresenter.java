package micdm.transportlive2.ui;

import java.util.Collection;

import javax.inject.Inject;

import io.reactivex.Observable;
import micdm.transportlive2.data.loaders.Loaders;
import micdm.transportlive2.data.loaders.Result;
import micdm.transportlive2.data.loaders.VehiclesLoader;
import micdm.transportlive2.misc.CommonFunctions;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.models.Vehicle;

public class VehiclesPresenter extends BasePresenter<VehiclesPresenter.View> implements VehiclesLoader.Client {

    public interface View {

        Observable<Object> getLoadVehiclesRequest();
    }

    @Inject
    CommonFunctions commonFunctions;
    @Inject
    Loaders loaders;

    private final Id routeId;

    VehiclesPresenter(Id routeId) {
        this.routeId = routeId;
    }

    @Override
    void initMore() {
        loaders.getVehiclesLoader(routeId).attach(this);
    }

    @Override
    public Observable<Id> getLoadVehiclesRequests() {
        return getViewInput(View::getLoadVehiclesRequest).compose(commonFunctions.toConst(routeId));
    }

    public Observable<Result<Collection<Vehicle>>> getResults() {
        return loaders.getVehiclesLoader(routeId).getData();
    }
}
