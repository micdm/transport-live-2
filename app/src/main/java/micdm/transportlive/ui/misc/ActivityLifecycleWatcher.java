package micdm.transportlive.ui.misc;

import android.os.Bundle;

import io.reactivex.Observable;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;

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

    private final Subject<State> states = ReplaySubject.create();

    public Observable<State> getState(Stage stage) {
        return states.filter(state -> state.stage == stage);
    }

    public void setState(Stage stage) {
        setState(stage, null);
    }

    public void setState(Stage stage, Bundle extra) {
        states.onNext(new State(stage, extra));
    }
}
