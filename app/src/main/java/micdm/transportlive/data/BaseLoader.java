package micdm.transportlive.data;

import java.util.Map;

import io.reactivex.Observable;
import micdm.transportlive.models.RouteGroup;

public abstract class BaseLoader<Client> {

    public interface State {}

    public static class StateLoading implements State {}

    public static class StateSuccess implements State {

        public final Map<String, RouteGroup> routeGroups;

        StateSuccess(Map<String, RouteGroup> routeGroups) {
            this.routeGroups = routeGroups;
        }
    }

    public static class StateFail implements State {}

    final Clients<Client> clients = new Clients<>();

    public void init() {

    }

    public void attach(Client client) {
        clients.attach(client);
    }

    public abstract Observable<State> getData();
}
