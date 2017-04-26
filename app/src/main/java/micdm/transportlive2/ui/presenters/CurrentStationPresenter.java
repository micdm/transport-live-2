package micdm.transportlive2.ui.presenters;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive2.misc.CommonFunctions;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.ui.misc.properties.NoValueProperty;
import micdm.transportlive2.ui.misc.properties.ValueProperty;

public class CurrentStationPresenter extends BasePresenter {

    public static class ViewInput {

        public final ValueProperty<Id> currentStation = new ValueProperty<>();
        public final NoValueProperty reset = new NoValueProperty();
    }

    @Inject
    CommonFunctions commonFunctions;

    public final ViewInput viewInput = new ViewInput();
    private final Subject<Id> currentStation = BehaviorSubject.create();
    private final Subject<Object> resetRequests = PublishSubject.create();

    @Override
    Disposable subscribeForEvents() {
        return subscribeForInput();
    }

    private Disposable subscribeForInput() {
        return new CompositeDisposable(
            viewInput.currentStation.get()
                .subscribe(currentStation::onNext),
            viewInput.reset.get()
                .subscribe(resetRequests::onNext)
        );
    }

    public Observable<Id> getCurrentStation() {
        return currentStation;
    }

    public Observable<Object> getResetRequests() {
        return resetRequests;
    }
}
