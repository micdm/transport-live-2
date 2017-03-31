package micdm.transportlive.models;

import org.immutables.value.Value;

@Value.Immutable
public interface Point {

    float latitude();
    float longitude();
}
