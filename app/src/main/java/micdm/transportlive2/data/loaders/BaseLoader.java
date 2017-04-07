package micdm.transportlive2.data.loaders;

import io.reactivex.Observable;
import micdm.transportlive2.misc.Clients;

public abstract class BaseLoader<Client, Data> {

    final Clients<Client> clients = new Clients<>();

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
