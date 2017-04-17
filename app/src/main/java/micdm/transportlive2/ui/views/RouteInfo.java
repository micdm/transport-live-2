package micdm.transportlive2.ui.views;

import micdm.transportlive2.models.Route;
import micdm.transportlive2.models.RouteGroup;

class RouteInfo {

    final RouteGroup group;
    final Route route;

    RouteInfo(RouteGroup group, Route route) {
        this.group = group;
        this.route = route;
    }
}
