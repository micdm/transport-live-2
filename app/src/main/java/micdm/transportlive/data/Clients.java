package micdm.transportlive.data;

import io.reactivex.Observable;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive.misc.CommonFunctions;

public class Clients<Client> {

    private final CommonFunctions commonFunctions;

    private final Subject<Client> attaches = ReplaySubject.create();
    private final Subject<Client> detaches = ReplaySubject.create();

    public Clients(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    public Observable<Client> get() {
        return attaches.flatMap(client ->
            Observable.just(client)
                .takeUntil(detaches.filter(commonFunctions.isEqual(client)))
        );
    }

    public void attach(Client client) {
        attaches.onNext(client);
    }

    public void detach(Client client) {
        detaches.onNext(client);
    }
}
