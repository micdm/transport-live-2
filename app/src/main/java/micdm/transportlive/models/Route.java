package micdm.transportlive.models;

import org.immutables.value.Value;

import java.util.Collection;
import java.util.Map;

@Value.Immutable
public abstract class Route {

    @Value.Parameter
    public abstract String id();
    @Value.Parameter
    public abstract String source();
    @Value.Parameter
    public abstract String destination();
    @Value.Parameter
    public abstract String number();
    @Value.Parameter
    public abstract Map<String, Vehicle> vehicles();

    public Collection<Vehicle> getVehicles() {
        return vehicles().values();
    }
}
