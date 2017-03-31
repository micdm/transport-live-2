package micdm.transportlive.data;

import com.google.gson.Gson;

import javax.inject.Inject;

import io.reactivex.Observable;
import micdm.transportlive.models.Path;

public class PathsStore extends DefaultStore<PathsStore.Client, Path> {

    public interface Client {

        Observable<Path> getStorePathRequests();
    }

    @Inject
    Gson gson;

    @Override
    Observable<Path> getStoreRequests() {
        return clients.get().flatMap(Client::getStorePathRequests);
    }

    @Override
    String getEntityId(Path path) {
        return path.route();
    }

    @Override
    String getKey(String entityId) {
        return String.format("path_%s", entityId);
    }

    @Override
    String serialize(Path path) {
        return gson.toJson(path, Path.class);
    }

    @Override
    Path deserialize(String data) {
        return gson.fromJson(data, Path.class);
    }
}
