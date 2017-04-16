package micdm.transportlive2.models;

import org.immutables.value.Value;
import org.joda.time.Duration;

import java.util.List;

import micdm.transportlive2.misc.Id;

@Value.Immutable
@Value.Enclosing
public interface Forecast {

    @Value.Immutable
    interface Vehicle {

        Id id();
        Id routeId();
        Duration estimatedTime();
    }

    String name();
    String description();
    List<Vehicle> vehicles();
}
