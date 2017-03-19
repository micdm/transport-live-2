package micdm.transportlive.misc;

import org.javatuples.Pair;

import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;

public class CommonFunctions {

    public <T1, T2> Pair<T1, T2> wrap(T1 first, T2 second) {
        return Pair.with(first, second);
    }

    public <T1, T2> Consumer<Pair<T1, T2>> unwrap(BiConsumer<T1, T2> handler) {
        return pair -> handler.accept(pair.getValue0(), pair.getValue1());
    }
}
