package micdm.transportlive.data;

import java.util.Collection;

import javax.inject.Inject;

import io.reactivex.Single;

public class ServerConnector {

    @Inject
    ApiService apiService;

    Single<Collection<GetRoutesResponse>> getRoutes() {
        return apiService.getRoutes();
    }

    Single<Collection<GetVehiclesResponse>> getVehicles(String routeId) {
        return apiService.getVehicles(routeId);
    }

    Single<GetPathResponse> getPath(String routeId) {
        return apiService.getPath(routeId);
    }
}
