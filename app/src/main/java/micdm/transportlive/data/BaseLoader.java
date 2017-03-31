package micdm.transportlive.data;

import io.reactivex.Observable;
import micdm.transportlive.ComponentHolder;

public abstract class BaseLoader<Client, Data> {

    private enum State {
        LOADING,
        SUCCESS,
        FAIL,
        CANCELED,
    }

    public static class Result<T> {

        public static <T> Result<T> newLoading() {
            return new Result<>(State.LOADING);
        }

        public static <T> Result<T> newSuccess(T data) {
            return new Result<>(State.SUCCESS, data);
        }

        public static <T> Result<T> newFail() {
            return new Result<>(State.FAIL);
        }

        public static <T> Result<T> newCanceled() {
            return new Result<>(State.CANCELED);
        }

        private final State state;
        public final T data;

        Result(State state) {
            this(state, null);
        }

        Result(State state, T data) {
            this.state = state;
            this.data = data;
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

        public boolean isCanceled() {
            return state == State.CANCELED;
        }

        public T getData() {
            return data;
        }
    }

    final Clients<Client> clients = new Clients<>(ComponentHolder.getAppComponent().getCommonFunctions());

    public void init() {

    }

    public void attach(Client client) {
        clients.attach(client);
    }

    public void detach(Client client) {
        clients.detach(client);
    }

    public abstract Observable<Result<Data>> getData();
}
