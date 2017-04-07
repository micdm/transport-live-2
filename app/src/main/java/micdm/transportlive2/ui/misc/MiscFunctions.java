package micdm.transportlive2.ui.misc;

import android.content.res.Resources;

import javax.inject.Inject;

import micdm.transportlive2.R;
import micdm.transportlive2.models.RouteGroup;

public class MiscFunctions {

    @Inject
    Resources resources;

    public CharSequence getRouteGroupName(RouteGroup group) {
        CharSequence[] names = resources.getStringArray(R.array.route_group_types);
        if (group.type() == RouteGroup.Type.TROLLEYBUS) {
            return names[0];
        }
        if (group.type() == RouteGroup.Type.TRAM) {
            return names[1];
        }
        if (group.type() == RouteGroup.Type.BUS) {
            return names[2];
        }
        throw new IllegalStateException(String.format("unknown group type %s", group.type()));
    }
}
