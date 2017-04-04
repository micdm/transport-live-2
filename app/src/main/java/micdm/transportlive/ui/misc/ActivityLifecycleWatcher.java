package micdm.transportlive.ui.misc;

import android.os.Bundle;

import io.reactivex.Observable;
import io.reactivex.subjects.ReplaySubject;
import timber.log.Timber;

public class ActivityLifecycleWatcher {

    public enum Stage {
        CREATE,
        START,
        RESUME,
        PAUSE,
        STOP,
        DESTROY,
        SAVE_STATE,
        LOW_MEMORY,
    }

    public static class State {

        public final Stage stage;
        public final Bundle extra;

        private State(Stage stage, Bundle extra) {
            this.stage = stage;
            this.extra = extra;
        }
    }

    private final ReplaySubject<State> states = ReplaySubject.create();

    public Observable<State> getState(Stage stage) {
        return getState(stage, false);
    }

    public Observable<State> getState(Stage stage, boolean skipPrevious) {
        Observable<State> states = this.states;
        if (skipPrevious) {
            states = states.skip(this.states.getValues().length - 1);
        }
        return states.filter(state -> state.stage == stage);
    }

    public void setState(Stage stage) {
        setState(stage, null);
    }

    public void setState(Stage stage, Bundle extra) {
        Timber.d("Current activity stage: %s", stage);
        states.onNext(new State(stage, extra));
    }
}
