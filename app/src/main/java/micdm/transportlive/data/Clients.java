package micdm.transportlive.data;

import io.reactivex.Observable;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;

class Clients<Client> {

    private final Subject<Client> clients = ReplaySubject.create();

    Observable<Client> get() {
        return clients;
    }

    void attach(Client client) {
        clients.onNext(client);
    }
}