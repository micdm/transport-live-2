package micdm.transportlive.data.loaders.remote;

import java.util.Collection;

import javax.inject.Inject;

import io.reactivex.Single;

public class ServerConnector {

    @Inject
    ApiService apiService;

    public Single<Collection<GetRoutesResponse>> getRoutes() {
        return apiService.getRoutes();
    }

    public Single<Collection<GetVehiclesResponse>> getVehicles(String routeId) {
        return apiService.getVehicles(routeId);
    }

    public Single<GetPathResponse> getPath(String routeId) {
        return apiService.getPath(routeId);
    }
}
