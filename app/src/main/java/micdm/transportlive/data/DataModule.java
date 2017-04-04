package micdm.transportlive.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

import dagger.Module;
import dagger.Provides;
import micdm.transportlive.AppScope;
import micdm.transportlive.data.loaders.LoaderModule;
import micdm.transportlive.data.stores.StoreModule;
import micdm.transportlive.models.ImmutablePath;
import micdm.transportlive.models.ImmutablePoint;
import micdm.transportlive.models.ImmutableRoute;
import micdm.transportlive.models.ImmutableRouteGroup;
import micdm.transportlive.models.Path;
import micdm.transportlive.models.Point;
import micdm.transportlive.models.Route;
import micdm.transportlive.models.RouteGroup;

@Module(includes = {LoaderModule.class, StoreModule.class})
public class DataModule {

    private static class RouteGroupTypeAdapter extends TypeAdapter<RouteGroup> {

        @Override
        public void write(JsonWriter out, RouteGroup group) throws IOException {
            out.beginObject();
            out.name("id").value(group.id());
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
            out.name("id").value(route.id());
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
                    builder.id(in.nextString());
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
                        throw new IllegalStateException(String.format("unknown route group type %s", type));
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
                    builder.id(in.nextString());
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

        @Override
        public void write(JsonWriter out, Path path) throws IOException {
            out.beginObject();
            out.name("route").value(path.route());
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
                if (name.equals("route")) {
                    builder.route(in.nextString());
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
    Gson provideGson() {
        return new GsonBuilder()
            .registerTypeAdapter(RouteGroup.class, new RouteGroupTypeAdapter())
            .registerTypeAdapter(Path.class, new PathTypeAdapter())
            .create();
    }
}

