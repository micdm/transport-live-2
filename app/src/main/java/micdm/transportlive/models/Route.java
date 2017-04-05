package micdm.transportlive.models;

import org.immutables.value.Value;

import micdm.transportlive.misc.Id;

@Value.Immutable
public interface Route {

    Id id();
    String number();
    String source();
    String destination();
}
