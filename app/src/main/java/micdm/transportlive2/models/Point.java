package micdm.transportlive2.models;

import org.immutables.value.Value;

@Value.Immutable
public interface Point {

    double latitude();
    double longitude();
}
