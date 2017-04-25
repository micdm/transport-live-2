package micdm.transportlive2.ui;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive2.misc.CommonFunctions;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.misc.Optional;

public class CurrentStationPresenter extends BasePresenter {

    public static class ViewInput {

        private final Subject<Optional<Id>> setCurrentStationRequests = PublishSubject.create();

        Observable<Optional<Id>> getSetCurrentStationRequests() {
            return setCurrentStationRequests;
        }

        public void setCurrentStation(Id stationId) {
            setCurrentStationRequests.onNext(Optional.of(stationId));
        }

        public void resetCurrentStation() {
            setCurrentStationRequests.onNext(Optional.empty());
        }
    }

    @Inject
    CommonFunctions commonFunctions;

    public final ViewInput viewInput = new ViewInput();
    private final Subject<Optional<Id>> results = BehaviorSubject.create();

    @Override
    Disposable subscribeForEvents() {
        return subscribeForInput();
    }

    private Disposable subscribeForInput() {
        return viewInput.getSetCurrentStationRequests().subscribe(results::onNext);
    }

    public Observable<Id> getCurrentStation() {
        return results
            .filter(Optional::isNonEmpty)
            .map(Optional::get);
    }

    public Observable<Object> getNoCurrentStation() {
        return results
            .filter(Optional::isEmpty)
            .compose(commonFunctions.toNothing());
    }
}
