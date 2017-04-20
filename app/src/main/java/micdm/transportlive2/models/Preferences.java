package micdm.transportlive2.models;

import org.immutables.value.Value;

import java.util.List;

import micdm.transportlive2.misc.Id;

@Value.Immutable
@Value.Enclosing
public interface Preferences {

    @Value.Immutable
    interface CameraPosition {

        Point position();
        double zoom();
    }

    List<Id> selectedRoutes();
    List<Id> selectedStations();
    boolean needShowStations();
    CameraPosition cameraPosition();
}
