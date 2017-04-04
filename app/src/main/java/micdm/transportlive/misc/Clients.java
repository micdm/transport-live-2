package micdm.transportlive.misc;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import timber.log.Timber;

public class Clients<Client> {

    private final BehaviorSubject<Collection<Client>> clients = BehaviorSubject.createDefault(Collections.emptySet());

    public Observable<Client> get() {
        return clients.switchMap(Observable::fromIterable);
    }

    public boolean has(Client client) {
        return clients.getValue().contains(client);
    }

    public void attach(Client client) {
        Collection<Client> clients = this.clients.getValue();
        if (!clients.contains(client)) {
            clients = new HashSet<>(clients);
            clients.add(client);
            Timber.d("Client %s attached to %s", client, this);
            this.clients.onNext(clients);
        }
    }

    public void detach(Client client) {
        Collection<Client> clients = this.clients.getValue();
        if (clients.contains(client)) {
            clients = new HashSet<>(clients);
            clients.remove(client);
            Timber.d("Client %s detached from %s", client, this);
            this.clients.onNext(clients);
        }
    }
}
