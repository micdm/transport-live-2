package micdm.transportlive.models;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public interface RouteGroup {

    enum Type {
        TROLLEYBUS,
        TRAM,
        BUS,
    }

    String id();
    Type type();
    List<Route> routes();
}
