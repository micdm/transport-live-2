package micdm.transportlive2.misc;

import timber.log.Timber;

public class TimberTree extends Timber.DebugTree {

    @Override
    protected String createStackElementTag(StackTraceElement element) {
        return String.format(":o) %s", super.createStackElementTag(element));
    }
}
