package micdm.transportlive2.models;

import org.immutables.value.Value;

import java.util.List;

import micdm.transportlive2.misc.Id;

@Value.Immutable
public interface Path {

    Id routeId();
    List<Point> points();
}
