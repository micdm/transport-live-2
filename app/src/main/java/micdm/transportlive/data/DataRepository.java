package micdm.transportlive.data;

import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive.models.RouteGroup;

public class DataRepository {

    private final Subject<Map<String, RouteGroup>> routeGroups = BehaviorSubject.create();
    private final Subject<Set<String>> selectedRoutes = BehaviorSubject.create();

    public Observable<Map<String, RouteGroup>> getRouteGroups() {
        return routeGroups;
    }

    public void putRouteGroups(Map<String, RouteGroup> items) {
        this.routeGroups.onNext(items);
    }

    public Observable<Set<String>> getSelectedRoutes() {
        return selectedRoutes;
    }

    public void putSelectedRoutes(Set<String> routes) {
        selectedRoutes.onNext(routes);
    }
}
