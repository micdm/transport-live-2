package micdm.transportlive.data;

import java.util.Set;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

interface ApiService {

    @GET("Pathway/")
    Single<Set<GetRoutesResponse>> getRoutes();

    @GET("Navigation/GetAutoOnPathway")
    Single<Set<GetVehiclesResponse>> getVehicles(@Query("pathwayId") String routeId);
}
