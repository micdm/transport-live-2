package micdm.transportlive.misc;

import java.util.HashMap;
import java.util.Map;

public class IdFactory {

    private final Map<String, Id> ids = new HashMap<>();
    private int lastValue = 0;

    public synchronized Id newInstance(String original) {
        Id id = ids.get(original);
        if (id == null) {
            id = new Id(original, lastValue);
            ids.put(original, id);
            lastValue += 1;
        }
        return id;
    }
}
