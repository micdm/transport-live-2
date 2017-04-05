package micdm.transportlive.data.loaders;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import io.reactivex.Observable;
import micdm.transportlive.data.loaders.remote.GetVehiclesResponse;
import micdm.transportlive.data.loaders.remote.ServerConnector;
import micdm.transportlive.data.stores.PathsStore;
import micdm.transportlive.misc.CommonFunctions;
import micdm.transportlive.misc.Id;
import micdm.transportlive.misc.IdFactory;
import micdm.transportlive.misc.Irrelevant;
import micdm.transportlive.models.ImmutablePoint;
import micdm.transportlive.models.ImmutableVehicle;
import micdm.transportlive.models.Vehicle;

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
