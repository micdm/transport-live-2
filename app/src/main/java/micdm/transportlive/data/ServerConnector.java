package micdm.transportlive.data;

import com.google.gson.Gson;

import java.util.Collection;

import javax.inject.Inject;

import io.reactivex.Single;

public class ServerConnector {

    @Inject
    ApiService apiService;
    @Inject
    Gson gson;

    Single<Collection<GetRoutesResponse>> getRoutes() {
        return apiService.getRoutes();
    }

    Single<Collection<GetVehiclesResponse>> getVehicles(String routeId) {
        return apiService.getVehicles(routeId);
    }
}
