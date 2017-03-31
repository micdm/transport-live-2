package micdm.transportlive.models;

import org.immutables.value.Value;

@Value.Immutable
public interface Vehicle {

    String id();
    String route();
    Point position();
    float direction();
}
