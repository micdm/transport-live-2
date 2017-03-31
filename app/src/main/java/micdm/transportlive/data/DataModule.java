package micdm.transportlive.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import org.joda.time.LocalDateTime;
import org.joda.time.Period;

import java.io.IOException;

import dagger.Module;
import dagger.Provides;
import micdm.transportlive.AppScope;
import micdm.transportlive.ComponentHolder;
import micdm.transportlive.misc.Cache;
import micdm.transportlive.models.ImmutablePath;
import micdm.transportlive.models.ImmutablePoint;
import micdm.transportlive.models.ImmutableRoute;
import micdm.transportlive.models.ImmutableRouteGroup;
import micdm.transportlive.models.Path;
import micdm.transportlive.models.Point;
import micdm.transportlive.models.Route;
import micdm.transportlive.models.RouteGroup;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

@Module
public class DataModule {

    private static class RouteGroupTypeAdapter extends TypeAdapter<RouteGroup> {

        @Override
        public void write(JsonWriter out, RouteGroup group) throws IOException {
            out.beginObject();
            out.name("id").value(group.id());
            out.name("name").value(group.name());
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
                if (name.equals("name")) {
                    builder.name(in.nextString());
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
    Loaders provideLoaders() {
        return new Loaders();
    }

    @Provides
    @AppScope
    ServerConnector provideServerConnector() {
        ServerConnector instance = new ServerConnector();
        ComponentHolder.getAppComponent().inject(instance);
        return instance;
    }

    @Provides
    @AppScope
    ApiService provideApiService(Retrofit retrofit) {
        return retrofit.create(ApiService.class);
    }

    @Provides
    @AppScope
    Retrofit provideRetrofit(OkHttpClient okHttpClient, Converter.Factory converterFactory, CallAdapter.Factory callAdapterFactory) {
        return new Retrofit.Builder()
            .baseUrl("http://gpt.incom.tomsk.ru/api/")
            .client(okHttpClient)
            .addConverterFactory(converterFactory)
            .addCallAdapterFactory(callAdapterFactory)
            .build();
    }

    @Provides
    @AppScope
    OkHttpClient provideOkHttpClient() {
        return new OkHttpClient.Builder().addInterceptor(chain -> {
            LocalDateTime now = LocalDateTime.now();
            Response response = chain.proceed(chain.request());
            Timber.d("Request %s completed with status %s in %sms", response.request().url(), response.code(), new Period(now, LocalDateTime.now()).getMillis());
            return response;
        }).build();
    }

    @Provides
    @AppScope
    Converter.Factory provideConverterFactory(Gson gson) {
        return GsonConverterFactory.create(gson);
    }

    @Provides
    @AppScope
    Gson provideGson() {
        return new GsonBuilder()
            .registerTypeAdapter(RouteGroup.class, new RouteGroupTypeAdapter())
            .registerTypeAdapter(Path.class, new PathTypeAdapter())
            .create();
    }

    @Provides
    @AppScope
    CallAdapter.Factory provideCallAdapterFactory() {
        return RxJava2CallAdapterFactory.create();
    }

    @Provides
    @AppScope
    SelectedRoutesStore provideSelectedRoutesStore() {
        SelectedRoutesStore instance = new SelectedRoutesStore();
        ComponentHolder.getAppComponent().inject(instance);
        instance.init();
        return instance;
    }

    @Provides
    @AppScope
    RoutesStore provideRoutesStore() {
        RoutesStore instance = new RoutesStore();
        ComponentHolder.getAppComponent().inject(instance);
        instance.init();
        return instance;
    }

    @Provides
    @AppScope
    PathsStore providePathsStore() {
        PathsStore instance = new PathsStore();
        ComponentHolder.getAppComponent().inject(instance);
        instance.init();
        return instance;
    }

    @Provides
    @AppScope
    Cache provideCache() {
        Cache instance = new Cache();
        ComponentHolder.getAppComponent().inject(instance);
        instance.init();
        return instance;
    }
}

