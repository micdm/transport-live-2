package micdm.transportlive.models;

import org.immutables.value.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Value.Immutable
public abstract class RouteGroup {

    @Value.Parameter
    public abstract String id();
    @Value.Parameter
    public abstract String name();
    @Value.Parameter
    public abstract Map<String, Route> routes();

    public Collection<Vehicle> getVehicles() {
        List<Vehicle> result = new ArrayList<>();
        for (Route route: routes().values()) {
            result.addAll(route.getVehicles());
        }
        return result;
    }
}
