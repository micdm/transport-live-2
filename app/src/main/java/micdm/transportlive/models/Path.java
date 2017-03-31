package micdm.transportlive.models;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public interface Path {

    String route();
    List<Point> points();
}
