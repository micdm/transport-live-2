package micdm.transportlive2.models;

import org.immutables.value.Value;

import micdm.transportlive2.misc.Id;

@Value.Immutable
public interface Vehicle {

    Id id();
    Id routeId();
    Point position();
    double direction();
}
