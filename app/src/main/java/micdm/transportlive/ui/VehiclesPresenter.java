package micdm.transportlive.ui;

import java.util.Collection;

import javax.inject.Inject;

import io.reactivex.Observable;
import micdm.transportlive.data.BaseLoader;
import micdm.transportlive.data.VehiclesLoader;

public class VehiclesPresenter extends BasePresenter<VehiclesPresenter.View> implements VehiclesLoader.Client {

    interface View extends BasePresenter.View {

        Observable<Collection<String>> getLoadVehiclesRequests();
        Observable<Collection<String>> getReloadVehiclesRequests();
    }

    @Inject
    VehiclesLoader vehiclesLoader;

    @Override
    void initMore() {
        vehiclesLoader.attach(this);
    }

    @Override
    public Observable<Collection<String>> getLoadVehiclesRequests() {
        return getViewInput(View::getLoadVehiclesRequests);
    }

    @Override
    public Observable<Collection<String>> getReloadVehiclesRequests() {
        return getViewInput(View::getReloadVehiclesRequests);
    }

    Observable<BaseLoader.State> getLoaderStates() {
        return vehiclesLoader.getData();
    }
}
