package micdm.transportlive2.data.loaders;

import java.util.ArrayList;
import java.util.Collection;

import io.reactivex.Single;
import micdm.transportlive2.data.loaders.remote.GetVehiclesResponse;
import micdm.transportlive2.data.loaders.remote.ServerConnector;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.misc.IdFactory;
import micdm.transportlive2.models.ImmutablePoint;
import micdm.transportlive2.models.ImmutableVehicle;
import micdm.transportlive2.models.Vehicle;

public class VehiclesLoader extends BaseLoader<Collection<Vehicle>> {

    static class VehiclesServerLoader implements ServerLoader<Collection<Vehicle>> {

        private final IdFactory idFactory;
        private final ServerConnector serverConnector;
        private final Id routeId;

        VehiclesServerLoader(IdFactory idFactory, ServerConnector serverConnector, Id routeId) {
            this.idFactory = idFactory;
            this.serverConnector = serverConnector;
            this.routeId = routeId;
        }

        @Override
        public Single<Collection<Vehicle>> load() {
            return serverConnector.getVehicles(routeId).map(this::convert);
        }

        private Collection<Vehicle> convert(Collection<GetVehiclesResponse> response) {
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
        }
    }

    VehiclesLoader(CacheClient<Collection<Vehicle>> cacheClient, ServerLoader<Collection<Vehicle>> serverLoader) {
        super(cacheClient, serverLoader);
    }
}
