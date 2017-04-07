package micdm.transportlive2.models;

import org.immutables.value.Value;

import java.util.List;

import micdm.transportlive2.misc.Id;

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
