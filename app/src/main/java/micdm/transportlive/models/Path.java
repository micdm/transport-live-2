package micdm.transportlive.models;

import org.immutables.value.Value;

import java.util.List;

import micdm.transportlive.misc.Id;

@Value.Immutable
public interface Path {

    Id routeId();
    List<Point> points();
}
