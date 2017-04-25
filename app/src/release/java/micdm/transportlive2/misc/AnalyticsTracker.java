package micdm.transportlive2.misc;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import micdm.transportlive2.App;
import micdm.transportlive2.R;

public class AnalyticsTracker {

    private static final String SCREEN_NAME = "main";

    private final Tracker tracker;

    AnalyticsTracker(App app) {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(app);
        tracker = analytics.newTracker(R.xml.global_tracker);
    }

    public void trackActivityStart() {
        tracker.setScreenName(SCREEN_NAME);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void trackRouteSelection(String routeId) {
        tracker.send(
            new HitBuilders.EventBuilder()
                .setCategory("route")
                .setAction("select")
                .setLabel(routeId)
                .build()
        );
    }

    public void trackStationSelection(String stationId) {
        tracker.send(
            new HitBuilders.EventBuilder()
                .setCategory("station")
                .setAction("select")
                .setLabel(stationId)
                .build()
        );
    }
}
