package micdm.transportlive2.misc;

public class Optional<T> {

    public static <T> Optional<T> of(T value) {
        return new Optional<>(value);
    }

    public static <T> Optional<T> empty() {
        return new Optional<>(null);
    }

    private final T value;

    private Optional(T value) {
        this.value = value;
    }

    public boolean isEmpty() {
        return value == null;
    }

    public boolean isNonEmpty() {
        return value != null;
    }

    public T get() {
        return value;
    }
}
