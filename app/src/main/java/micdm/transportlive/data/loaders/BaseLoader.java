package micdm.transportlive.data.loaders;

import io.reactivex.Observable;
import micdm.transportlive.ComponentHolder;
import micdm.transportlive.data.Clients;

public abstract class BaseLoader<Client, Data> {

    final Clients<Client> clients = new Clients<>(ComponentHolder.getAppComponent().getCommonFunctions());

    void init() {

    }

    public void attach(Client client) {
        clients.attach(client);
    }

    public void detach(Client client) {
        clients.detach(client);
    }

    public abstract Observable<Result<Data>> getData();
}
