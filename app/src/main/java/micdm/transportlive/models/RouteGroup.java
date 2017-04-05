package micdm.transportlive.models;

import org.immutables.value.Value;

import java.util.List;

import micdm.transportlive.misc.Id;

@Value.Immutable
public interface RouteGroup {

    enum Type {
        TROLLEYBUS,
        TRAM,
        BUS,
    }

    Id id();
    Type type();
    List<Route> routes();
}
