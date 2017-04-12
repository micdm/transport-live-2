package micdm.transportlive2.models;

import org.immutables.value.Value;

import micdm.transportlive2.misc.Id;

@Value.Immutable
public interface Station {

    Id id();
    String name();
    Point location();
}
