package micdm.transportlive2.misc;

import timber.log.Timber;

public class TimberTree extends Timber.Tree {

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        // Nothing to do here
    }
}
