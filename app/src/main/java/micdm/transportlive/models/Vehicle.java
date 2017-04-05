package micdm.transportlive.models;

import org.immutables.value.Value;

import micdm.transportlive.misc.Id;

@Value.Immutable
public interface Vehicle {

    Id id();
    Id routeId();
    Point position();
    float direction();
}
