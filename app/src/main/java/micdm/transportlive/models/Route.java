package micdm.transportlive.models;

import org.immutables.value.Value;

@Value.Immutable
public interface Route {

    String id();
    String number();
    String source();
    String destination();
}
