package micdm.transportlive.data;

import io.reactivex.Observable;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive.misc.CommonFunctions;

class Clients<Client> {

    private final CommonFunctions commonFunctions;

    private final Subject<Client> attaches = ReplaySubject.create();
    private final Subject<Client> detaches = ReplaySubject.create();

    Clients(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    Observable<Client> get() {
        return attaches.flatMap(client ->
            Observable.just(client)
                .takeUntil(detaches.filter(commonFunctions.isEqual(client)))
        );
    }

    void attach(Client client) {
        attaches.onNext(client);
    }

    void detach(Client client) {
        detaches.onNext(client);
    }
}
