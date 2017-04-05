package micdm.transportlive.misc;

public class Id {

    private final String original;
    private final int numeric;

    Id(String original, int numeric) {
        this.original = original;
        this.numeric = numeric;
    }

    @Override
    public int hashCode() {
        return numeric;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Id && ((Id) o).numeric == numeric;
    }

    @Override
    public String toString() {
        return String.format("Id(%s)", original);
    }

    public String getOriginal() {
        return original;
    }

    public int getNumeric() {
        return numeric;
    }
}
