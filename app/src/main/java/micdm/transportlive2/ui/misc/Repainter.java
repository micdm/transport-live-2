package micdm.transportlive2.ui.misc;

import org.joda.time.Duration;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive2.misc.Irrelevant;

public class Repainter {

    private final static Duration MIN_REPAINT_INTERVAL = Duration.millis(50);

    private final Subject<Object> requests = PublishSubject.create();

    public Observable<Object> getRepaintRequests() {
        return requests.throttleLast(MIN_REPAINT_INTERVAL.getMillis(), TimeUnit.MILLISECONDS);
    }

    public void requestRepaint() {
        requests.onNext(Irrelevant.INSTANCE);
    }
}
