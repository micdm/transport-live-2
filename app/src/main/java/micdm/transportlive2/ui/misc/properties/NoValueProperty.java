package micdm.transportlive2.ui.misc.properties;

import micdm.transportlive2.misc.Irrelevant;

public class NoValueProperty extends Property<Object> {

    public void call() {
        setInternal(Irrelevant.INSTANCE);
    }
}
