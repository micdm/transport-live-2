package micdm.transportlive.data;

import com.google.gson.Gson;

import java.util.Set;

import javax.inject.Inject;

import io.reactivex.Single;

public class ServerConnector {

    @Inject
    ApiService apiService;
    @Inject
    Gson gson;

    Single<Set<GetRoutesResponse>> getRoutes() {
        return apiService.getRoutes();
    }

    Single<Set<GetVehiclesResponse>> getVehicles(String routeId) {
        return apiService.getVehicles(routeId);
    }
}
