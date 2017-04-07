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
import micdm.transportlive2.misc.IdFactory;
import micdm.transportlive2.models.ImmutablePath;
import micdm.transportlive2.models.ImmutablePoint;
import micdm.transportlive2.models.ImmutableRoute;
import micdm.transportlive2.models.ImmutableRouteGroup;
import micdm.transportlive2.models.Path;
import micdm.transportlive2.models.Point;
import micdm.transportlive2.models.Route;
import micdm.transportlive2.models.RouteGroup;

@Module(includes = {LoaderModule.class, StoreModule.class})
public class DataModule {

    private static class RouteGroupTypeAdapter extends TypeAdapter<RouteGroup> {

        private final IdFactory idFactory;

        private RouteGroupTypeAdapter(IdFactory idFactory) {
            this.idFactory = idFactory;
        }

        @Override
        public void write(JsonWriter out, RouteGroup group) throws IOException {
            out.beginObject();
            out.name("id").value(group.id().getOriginal());
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
            out.name("id").value(route.id().getOriginal());
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
                    builder.id(idFactory.newInstance(in.nextString()));
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
                    builder.id(idFactory.newInstance(in.nextString()));
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

    private static class PathTypeAdapter extends TypeAdapter<Path> {

        private final IdFactory idFactory;

        private PathTypeAdapter(IdFactory idFactory) {
            this.idFactory = idFactory;
        }

        @Override
        public void write(JsonWriter out, Path path) throws IOException {
            out.beginObject();
            out.name("routeId").value(path.routeId().getOriginal());
            out.name("points").beginArray();
            for (Point point: path.points()) {
                out
                    .beginArray()
                    .value(point.latitude())
                    .value(point.longitude())
                    .endArray();
            }
            out.endArray();
            out.endObject();
        }

        @Override
        public Path read(JsonReader in) throws IOException {
            in.beginObject();
            ImmutablePath.Builder builder = ImmutablePath.builder();
            while (in.hasNext()) {
                String name = in.nextName();
                if (name.equals("routeId")) {
                    builder.routeId(idFactory.newInstance(in.nextString()));
                }
                if (name.equals("points")) {
                    in.beginArray();
                    while (in.hasNext()) {
                        in.beginArray();
                        builder.addPoints(
                            ImmutablePoint.builder()
                                .latitude((float) in.nextDouble())
                                .longitude((float) in.nextDouble())
                                .build()
                        );
                        in.endArray();
                    }
                    in.endArray();
                }
            }
            in.endObject();
            return builder.build();
        }
    }

    @Provides
    @AppScope
    Gson provideGson(IdFactory idFactory) {
        return new GsonBuilder()
            .registerTypeAdapter(RouteGroup.class, new RouteGroupTypeAdapter(idFactory))
            .registerTypeAdapter(Path.class, new PathTypeAdapter(idFactory))
            .create();
    }
}

