package micdm.transportlive2.ui.misc.properties;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

abstract class Property<Data> {

    private final Subject<Data> values = PublishSubject.create();

    public Observable<Data> get() {
        return values;
    }

    void setInternal(Data data) {
        values.onNext(data);
    }
}
