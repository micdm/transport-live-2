package micdm.transportlive2.data.loaders.remote;

import java.util.Collection;

import javax.inject.Inject;

import io.reactivex.Single;
import micdm.transportlive2.misc.Id;

public class ServerConnector {

    @Inject
    ApiService apiService;

    public Single<Collection<GetRoutesResponse>> getRoutes() {
        return apiService.getRoutes();
    }

    public Single<Collection<GetVehiclesResponse>> getVehicles(Id routeId) {
        return apiService.getVehicles(routeId.getOriginal());
    }

    public Single<GetPathResponse> getPath(Id routeId) {
        return apiService.getPath(routeId.getOriginal());
    }
}
