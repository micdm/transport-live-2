package micdm.transportlive2.ui.views;

import micdm.transportlive2.models.Forecast;
import micdm.transportlive2.models.Route;
import micdm.transportlive2.models.RouteGroup;

class VehicleInfo {

    final RouteGroup group;
    final Route route;
    final Forecast.Vehicle vehicle;

    VehicleInfo(RouteGroup group, Route route, Forecast.Vehicle vehicle) {
        this.group = group;
        this.route = route;
        this.vehicle = vehicle;
    }
}
