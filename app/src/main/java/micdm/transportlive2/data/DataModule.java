package micdm.transportlive2.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

import dagger.Module;
import dagger.Provides;
import micdm.transportlive2.AppScope;
import micdm.transportlive2.data.loaders.LoaderModule;
import micdm.transportlive2.data.stores.StoreModule;
import micdm.transportlive2.misc.Id;
import micdm.transportlive2.misc.IdFactory;
import micdm.transportlive2.models.ImmutablePath;
import micdm.transportlive2.models.ImmutablePoint;
import micdm.transportlive2.models.ImmutablePreferences;
import micdm.transportlive2.models.ImmutableRoute;
import micdm.transportlive2.models.ImmutableRouteGroup;
import micdm.transportlive2.models.ImmutableStation;
import micdm.transportlive2.models.Path;
import micdm.transportlive2.models.Point;
import micdm.transportlive2.models.Preferences;
import micdm.transportlive2.models.Route;
import micdm.transportlive2.models.RouteGroup;
import micdm.transportlive2.models.Station;

@Module(includes = {LoaderModule.class, StoreModule.class})
public class DataModule {

    static class IdTypeAdapter extends TypeAdapter<Id> {

        private final IdFactory idFactory;

        private IdTypeAdapter(IdFactory idFactory) {
            this.idFactory = idFactory;
        }

        @Override
        public void write(JsonWriter out, Id id) throws IOException {
            out.value(id.getOriginal());
        }

        @Override
        public Id read(JsonReader in) throws IOException {
            return idFactory.newInstance(in.nextString());
        }
    }

    static class PointTypeAdapter extends TypeAdapter<Point> {

        @Override
        public void write(JsonWriter out, Point point) throws IOException {
            out.beginObject();
            out.name("lat").value(point.latitude());
            out.name("lon").value(point.longitude());
            out.endObject();
        }

        @Override
        public Point read(JsonReader in) throws IOException {
            ImmutablePoint.Builder builder = ImmutablePoint.builder();
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                if (name.equals("lat")) {
                    builder.latitude(in.nextDouble());
                }
                if (name.equals("lon")) {
                    builder.longitude(in.nextDouble());
                }
            }
            in.endObject();
            return builder.build();
        }
    }

    static class RouteGroupTypeAdapter extends TypeAdapter<RouteGroup> {

        private final IdTypeAdapter idTypeAdapter;

        private RouteGroupTypeAdapter(IdTypeAdapter idTypeAdapter) {
            this.idTypeAdapter = idTypeAdapter;
        }

        @Override
        public void write(JsonWriter out, RouteGroup group) throws IOException {
            out.beginObject();
            out.name("id");
            idTypeAdapter.write(out, group.id());
            out.name("type").value(group.type().toString());
            out.name("routes");
            out.beginArray();
            for (Route route: group.routes()) {
                writeRoute(out, route);
            }
            out.endArray();
            out.endObject();
        }

        private void writeRoute(JsonWriter out, Route route) throws IOException {
            out.beginObject();
            out.name("id");
            idTypeAdapter.write(out, route.id());
            out.name("number").value(route.number());
            out.name("source").value(route.source());
            out.name("destination").value(route.destination());
            out.endObject();
        }

        @Override
        public RouteGroup read(JsonReader in) throws IOException {
            in.beginObject();
            ImmutableRouteGroup.Builder builder = ImmutableRouteGroup.builder();
            while (in.hasNext()) {
                String name = in.nextName();
                if (name.equals("id")) {
                    builder.id(idTypeAdapter.read(in));
                }
                if (name.equals("type")) {
                    String type = in.nextString();
                    if (type.equals("TROLLEYBUS")) {
                        builder.type(RouteGroup.Type.TROLLEYBUS);
                    } else if (type.equals("TRAM")) {
                        builder.type(RouteGroup.Type.TRAM);
                    } else if (type.equals("BUS")) {
                        builder.type(RouteGroup.Type.BUS);
                    } else {
                        throw new IllegalStateException(String.format("unknown routeId group type %s", type));
                    }
                }
                if (name.equals("routes")) {
                    in.beginArray();
                    while (in.hasNext()) {
                        builder.addRoutes(readRoute(in));
                    }
                    in.endArray();
                }
            }
            in.endObject();
            return builder.build();
        }

        private Route readRoute(JsonReader in) throws IOException {
            in.beginObject();
            ImmutableRoute.Builder builder = ImmutableRoute.builder();
            while (in.hasNext()) {
                String name = in.nextName();
                if (name.equals("id")) {
                    builder.id(idTypeAdapter.read(in));
                }
                if (name.equals("number")) {
                    builder.number(in.nextString());
                }
                if (name.equals("source")) {
                    builder.source(in.nextString());
                }
                if (name.equals("destination")) {
                    builder.destination(in.nextString());
                }
            }
            in.endObject();
            return builder.build();
        }
    }

    static class PathTypeAdapter extends TypeAdapter<Path> {

        private final IdTypeAdapter idTypeAdapter;
        private final PointTypeAdapter pointTypeAdapter;

        private PathTypeAdapter(IdTypeAdapter idTypeAdapter, PointTypeAdapter pointTypeAdapter) {
            this.idTypeAdapter = idTypeAdapter;
            this.pointTypeAdapter = pointTypeAdapter;
        }

        @Override
        public void write(JsonWriter out, Path path) throws IOException {
            out.beginObject();
            out.name("routeId");
            idTypeAdapter.write(out, path.routeId());
            out.name("points").beginArray();
            for (Point point: path.points()) {
                pointTypeAdapter.write(out, point);
            }
            out.endArray();
            out.name("stations").beginArray();
            for (Station station: path.stations()) {
                writeStation(out, station);
            }
            out.endArray();
            out.endObject();
        }

        private void writeStation(JsonWriter out, Station station) throws IOException {
            out.beginObject().name("id");
            idTypeAdapter.write(out, station.id());
            out.name("name").value(station.name());
            out.name("location");
            pointTypeAdapter.write(out, station.location());
            out.endObject();
        }

        @Override
        public Path read(JsonReader in) throws IOException {
            in.beginObject();
            ImmutablePath.Builder builder = ImmutablePath.builder();
            while (in.hasNext()) {
                String name = in.nextName();
                if (name.equals("routeId")) {
                    builder.routeId(idTypeAdapter.read(in));
                }
                if (name.equals("points")) {
                    in.beginArray();
                    while (in.hasNext()) {
                        builder.addPoints(pointTypeAdapter.read(in));
                    }
                    in.endArray();
                }
                if (name.equals("stations")) {
                    in.beginArray();
                    while (in.hasNext()) {
                        builder.addStations(readStation(in));
                    }
                    in.endArray();
                }
            }
            in.endObject();
            return builder.build();
        }

        private Station readStation(JsonReader in) throws IOException {
            in.beginObject();
            ImmutableStation.Builder builder = ImmutableStation.builder();
            while (in.hasNext()) {
                String name = in.nextName();
                if (name.equals("id")) {
                    builder.id(idTypeAdapter.read(in));
                }
                if (name.equals("name")) {
                    builder.name(in.nextString());
                }
                if (name.equals("location")) {
                    builder.location(pointTypeAdapter.read(in));
                }
            }
            in.endObject();
            return builder.build();
        }
    }

    static class PreferencesTypeAdapter extends TypeAdapter<Preferences> {

        private final IdTypeAdapter idTypeAdapter;
        private final PreferencesCameraPositionTypeAdapter preferencesCameraPositionTypeAdapter;

        private PreferencesTypeAdapter(IdTypeAdapter idTypeAdapter, PreferencesCameraPositionTypeAdapter preferencesCameraPositionTypeAdapter) {
            this.idTypeAdapter = idTypeAdapter;
            this.preferencesCameraPositionTypeAdapter = preferencesCameraPositionTypeAdapter;
        }

        @Override
        public void write(JsonWriter out, Preferences preferences) throws IOException {
            out.beginObject();
            out.name("selectedRoutes").beginArray();
            for (Id id: preferences.selectedRoutes()) {
                idTypeAdapter.write(out, id);
            }
            out.endArray();
            out.name("needShowStations").value(preferences.needShowStations());
            out.name("cameraPosition");
            preferencesCameraPositionTypeAdapter.write(out, preferences.cameraPosition());
            out.endObject();
        }

        @Override
        public Preferences read(JsonReader in) throws IOException {
            ImmutablePreferences.Builder builder = ImmutablePreferences.builder();
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                if (name.equals("selectedRoutes")) {
                    in.beginArray();
                    while (in.hasNext()) {
                        builder.addSelectedRoutes(idTypeAdapter.read(in));
                    }
                    in.endArray();
                }
                if (name.equals("needShowStations")) {
                    builder.needShowStations(in.nextBoolean());
                }
                if (name.equals("cameraPosition")) {
                    builder.cameraPosition(preferencesCameraPositionTypeAdapter.read(in));
                }
            }
            in.endObject();
            return builder.build();
        }
    }

    static class PreferencesCameraPositionTypeAdapter extends TypeAdapter<Preferences.CameraPosition> {

        private final PointTypeAdapter pointTypeAdapter;

        private PreferencesCameraPositionTypeAdapter(PointTypeAdapter pointTypeAdapter) {
            this.pointTypeAdapter = pointTypeAdapter;
        }

        @Override
        public void write(JsonWriter out, Preferences.CameraPosition cameraPosition) throws IOException {
            out.beginObject();
            out.name("position");
            pointTypeAdapter.write(out, cameraPosition.position());
            out.name("zoom").value(cameraPosition.zoom());
            out.endObject();
        }

        @Override
        public Preferences.CameraPosition read(JsonReader in) throws IOException {
            ImmutablePreferences.CameraPosition.Builder builder = ImmutablePreferences.CameraPosition.builder();
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                if (name.equals("position")) {
                    builder.position(pointTypeAdapter.read(in));
                }
                if (name.equals("zoom")) {
                    builder.zoom(in.nextDouble());
                }
            }
            in.endObject();
            return builder.build();
        }
    }

    @Provides
    @AppScope
    Gson provideGson(RouteGroupTypeAdapter routeGroupTypeAdapter, PathTypeAdapter pathTypeAdapter, PreferencesTypeAdapter preferencesTypeAdapter) {
        return new GsonBuilder()
            .registerTypeAdapter(RouteGroup.class, routeGroupTypeAdapter)
            .registerTypeAdapter(Path.class, pathTypeAdapter)
            .registerTypeAdapter(Preferences.class, preferencesTypeAdapter)
            .create();
    }

    @Provides
    @AppScope
    IdTypeAdapter provideIdTypeAdapter(IdFactory idFactory) {
        return new IdTypeAdapter(idFactory);
    }

    @Provides
    @AppScope
    PointTypeAdapter providePointTypeAdapter() {
        return new PointTypeAdapter();
    }

    @Provides
    @AppScope
    RouteGroupTypeAdapter provideRouteGroupTypeAdapter(IdTypeAdapter idTypeAdapter) {
        return new RouteGroupTypeAdapter(idTypeAdapter);
    }

    @Provides
    @AppScope
    PathTypeAdapter providePathTypeAdapter(IdTypeAdapter idTypeAdapter, PointTypeAdapter pointTypeAdapter) {
        return new PathTypeAdapter(idTypeAdapter, pointTypeAdapter);
    }

    @Provides
    @AppScope
    PreferencesTypeAdapter providePreferencesTypeAdapter(IdTypeAdapter idTypeAdapter, PreferencesCameraPositionTypeAdapter preferencesCameraPositionTypeAdapter) {
        return new PreferencesTypeAdapter(idTypeAdapter, preferencesCameraPositionTypeAdapter);
    }

    @Provides
    @AppScope
    PreferencesCameraPositionTypeAdapter providePreferencesCameraPositionTypeAdapter(PointTypeAdapter pointTypeAdapter) {
        return new PreferencesCameraPositionTypeAdapter(pointTypeAdapter);
    }
}
