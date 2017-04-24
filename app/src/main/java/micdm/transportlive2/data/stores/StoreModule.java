package micdm.transportlive2.data.stores;

import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import dagger.Module;
import dagger.Provides;
import micdm.transportlive2.AppScope;
import micdm.transportlive2.ComponentHolder;
import micdm.transportlive2.misc.Cache;
import micdm.transportlive2.models.ImmutablePoint;
import micdm.transportlive2.models.ImmutablePreferences;
import micdm.transportlive2.models.Preferences;
import micdm.transportlive2.models.RouteGroup;

@Module
public class StoreModule {

    @Provides
    @AppScope
    Stores provideStores() {
        Stores instance = new Stores();
        ComponentHolder.getAppComponent().inject(instance);
        return instance;
    }

    @Provides
    @AppScope
    PreferencesStore providePreferencesStore(Gson gson, SharedPreferences sharedPreferences) {
        PreferencesStore instance = new PreferencesStore(
            new BaseStore.Adapter<Preferences>() {
                @Override
                public String serialize(Preferences preferences) {
                    return gson.toJson(preferences, Preferences.class);
                }
                @Override
                public Preferences deserialize(String serialized) {
                    return gson.fromJson(serialized, Preferences.class);
                }
            },
            new SharedPreferencesStorage(sharedPreferences, "preferences"),
            ImmutablePreferences.builder()
                .selectedRoutes(Collections.emptySet())
                .selectedStations(Collections.emptySet())
                .needShowStations(true)
                .cameraPosition(
                    ImmutablePreferences.CameraPosition.builder()
                        .position(
                            ImmutablePoint.builder()
                                .latitude(56.488881)
                                .longitude(84.987703)
                                .build()
                        )
                        .zoom(12)
                        .build()
                )
                .build()
        );
        instance.init();
        return instance;
    }

    @Provides
    @AppScope
    RoutesStore provideRoutesStore(Cache cache, Gson gson) {
        RoutesStore instance = new RoutesStore(
            new BaseStore.Adapter<Collection<RouteGroup>>() {
                @Override
                public String serialize(Collection<RouteGroup> groups) {
                    return gson.toJson(groups.toArray(), RouteGroup[].class);
                }
                @Override
                public Collection<RouteGroup> deserialize(String serialized) {
                    return Arrays.asList(gson.fromJson(serialized, RouteGroup[].class));
                }
            },
            new CacheStorage(cache, "routes")
        );
        instance.init();
        return instance;
    }
}
