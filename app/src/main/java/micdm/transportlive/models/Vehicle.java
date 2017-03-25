package micdm.transportlive.models;

import org.immutables.value.Value;

@Value.Immutable
public interface Vehicle {

    String id();
    float latitude();
    float longitude();
    float direction();
}
