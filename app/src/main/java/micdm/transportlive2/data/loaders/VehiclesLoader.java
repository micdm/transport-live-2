package micdm.transportlive2.data.loaders;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import io.reactivex.Observable;
import micdm.transportlive2.data.loaders.remote.GetVehiclesResponse;
import micdm.transportlive2.data.loaders.remote.ServerConnector;
import micdm.transportlive2.data.stores.PathsStore;
import micdm.transportlive2.misc.CommonFunctions;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.misc.IdFactory;
import micdm.transportlive2.misc.Irrelevant;
import micdm.transportlive2.models.ImmutablePoint;
import micdm.transportlive2.models.ImmutableVehicle;
import micdm.transportlive2.models.Vehicle;

public class VehiclesLoader extends DefaultLoader<VehiclesLoader.Client, Collection<Vehicle>> {

    public interface Client {

        Observable<Id> getLoadVehiclesRequests();
    }

    @Inject
    CommonFunctions commonFunctions;
    @Inject
    IdFactory idFactory;
    @Inject
    PathsStore pathsStore;
    @Inject
    ServerConnector serverConnector;

    private final Id routeId;

    VehiclesLoader(Id routeId) {
        this.routeId = routeId;
    }

    @Override
    public String toString() {
        return String.format("VehiclesLoader(routeId=%s)", routeId);
    }

    @Override
    Observable<Object> getLoadRequests() {
        return clients.get()
            .flatMap(Client::getLoadVehiclesRequests)
            .filter(commonFunctions.isEqual(routeId))
            .compose(commonFunctions.toConst(Irrelevant.INSTANCE));
    }

    @Override
    Observable<Collection<Vehicle>> loadFromCache() {
        return Observable.empty();
    }

    @Override
    Observable<Collection<Vehicle>> loadFromServer() {
        return serverConnector.getVehicles(routeId)
            .toObservable()
            .map(response -> {
                Collection<Vehicle> result = new ArrayList<>();
                for (GetVehiclesResponse item: response) {
                    result.add(
                        ImmutableVehicle.builder()
                            .id(idFactory.newInstance(item.Auto.AutoId))
                            .routeId(idFactory.newInstance(item.Auto.PathwayId))
                            .position(
                                ImmutablePoint.builder()
                                    .latitude(item.Point.Lat)
                                    .longitude(item.Point.Lon)
                                    .build()
                            )
                            .direction(item.Point.Dir)
                            .build()
                    );
                }
                return result;
            });
    }
}
