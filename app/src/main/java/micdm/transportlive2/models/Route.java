package micdm.transportlive2.models;

import org.immutables.value.Value;

import micdm.transportlive2.misc.Id;

@Value.Immutable
public interface Route {

    Id id();
    String number();
    String source();
    String destination();
}
