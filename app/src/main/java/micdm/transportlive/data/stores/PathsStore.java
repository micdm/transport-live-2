package micdm.transportlive.data.stores;

import com.google.gson.Gson;

import javax.inject.Inject;

import io.reactivex.Observable;
import micdm.transportlive.misc.Id;
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
    Id getEntityId(Path path) {
        return path.routeId();
    }

    @Override
    String getKey(Id entityId) {
        return String.format("path_%s", entityId.getOriginal());
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
