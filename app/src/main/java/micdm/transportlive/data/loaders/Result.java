package micdm.transportlive.data.loaders;

public class Result<T> {

    private enum State {
        LOADING,
        SUCCESS,
        FAIL,
    }

    public static <T> Result<T> newLoading() {
        return new Result<>(State.LOADING);
    }

    public static <T> Result<T> newSuccess(T data) {
        return new Result<>(State.SUCCESS, data);
    }

    public static <T> Result<T> newFail() {
        return new Result<>(State.FAIL);
    }

    private final State state;
    public final T data;

    private Result(State state) {
        this(state, null);
    }

    private Result(State state, T data) {
        this.state = state;
        this.data = data;
    }

    @Override
    public String toString() {
        return String.format("Result(state=%s)", state);
    }

    public boolean isLoading() {
        return state == State.LOADING;
    }

    public boolean isSuccess() {
        return state == State.SUCCESS;
    }

    public boolean isFail() {
        return state == State.FAIL;
    }

    public T getData() {
        return data;
    }
}
