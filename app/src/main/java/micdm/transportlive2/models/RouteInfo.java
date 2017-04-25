package micdm.transportlive2.models;

import org.immutables.value.Value;

@Value.Immutable
public interface RouteInfo {

    RouteGroup group();
    Route route();
}
