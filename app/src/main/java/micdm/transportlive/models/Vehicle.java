package micdm.transportlive.models;

import org.immutables.value.Value;

@Value.Immutable
public interface Vehicle {

    String id();
    double latitude();
    double longitude();
    double direction();
}
